package com.victormoni.ecommerce.service.impl;

import com.victormoni.ecommerce.dto.request.LoginRequest;
import com.victormoni.ecommerce.dto.request.RefreshRequest;
import com.victormoni.ecommerce.dto.request.RegisterRequest;
import com.victormoni.ecommerce.dto.response.AuthResponse;
import com.victormoni.ecommerce.dto.response.SuccessResponse;
import com.victormoni.ecommerce.exception.BusinessException;
import com.victormoni.ecommerce.exception.UnauthorizedException;
import com.victormoni.ecommerce.model.Role;
import com.victormoni.ecommerce.model.User;
import com.victormoni.ecommerce.security.CustomUserDetailsService;
import com.victormoni.ecommerce.security.JwtUtil;
import com.victormoni.ecommerce.service.AuthService;
import com.victormoni.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("🔐 Tentando autenticar {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String access = jwtUtil.generateToken(userDetails);
            String refresh = jwtUtil.generateRefreshToken(userDetails);

            log.info("✅ Login bem-sucedido para {}", request.getUsername());
            return new AuthResponse(access, refresh);

        } catch (Exception e) {
            log.warn("❌ Falha na autenticação para {}", request.getUsername());
            throw new UnauthorizedException("Usuário ou senha inválidos");
        }
    }

    @Override
    public AuthResponse refresh(RefreshRequest request) {
        log.info("♻️ Requisição de refresh token");

        if (!jwtUtil.isTokenValid(request.getRefreshToken())) {
            log.warn("❌ Token de refresh inválido");
            throw new UnauthorizedException("Token inválido ou expirado");
        }

        String username = jwtUtil.getUsernameFromToken(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccess = jwtUtil.generateToken(userDetails);
        log.info("🔁 Novo access token gerado para {}", username);

        return new AuthResponse(newAccess, request.getRefreshToken());
    }

    @Override
    @Transactional
    public SuccessResponse register(RegisterRequest request) {
        log.info("📝 Registrando novo usuário: {}", request.getUsername());

        String username = request.getUsername();
        String password = request.getPassword();
        Role role = Role.valueOf(request.getRole().toUpperCase());

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
        log.info("✅ Usuário {} registrado com sucesso", username);

        return new SuccessResponse("Usuário registrado com sucesso");
    }
}
