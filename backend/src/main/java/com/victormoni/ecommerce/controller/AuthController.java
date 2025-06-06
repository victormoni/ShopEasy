package com.victormoni.ecommerce.controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.victormoni.ecommerce.api.AuthApi;
import com.victormoni.ecommerce.dto.response.AuthResponse;
import com.victormoni.ecommerce.dto.request.LoginRequest;
import com.victormoni.ecommerce.dto.request.RefreshRequest;
import com.victormoni.ecommerce.dto.request.RegisterRequest;
import com.victormoni.ecommerce.dto.response.SuccessResponse;
import com.victormoni.ecommerce.exception.BusinessException;
import com.victormoni.ecommerce.model.Role;
import com.victormoni.ecommerce.model.User;
import com.victormoni.ecommerce.security.JwtUtil;
import com.victormoni.ecommerce.security.CustomUserDetailsService;
import com.victormoni.ecommerce.service.AuthService;
import com.victormoni.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 *
 * @author Victor Moni
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de login, registro e tokens JWT")
public class AuthController implements AuthApi{

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        var auth = authService.authenticate(body.getUsername(), body.getPassword());
        var userDetails = (org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal();
        String access = jwtUtil.generateToken(userDetails);
        String refresh = jwtUtil.generateRefreshToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(access, refresh));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest body) {

        if (!jwtUtil.isTokenValid(body.getRefreshToken())) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtUtil.getUsernameFromToken(body.getRefreshToken());
        var userDetails = userDetailsService.loadUserByUsername(username);
        String newAccess = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(newAccess, body.getRefreshToken()));
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest body) {

        String username = body.getUsername();
        String password = body.getPassword();
        Role role = Role.valueOf(body.getRole().toUpperCase());

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException("Usuário e senha são obrigatórios");
        }

        if (userService.findByUsername(username).isPresent()) {
            throw new BusinessException("Usuário já existe");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole(role);
        userService.save(newUser);
        return ResponseEntity.ok(new SuccessResponse("Usuário registrado com sucesso"));
    }
}
