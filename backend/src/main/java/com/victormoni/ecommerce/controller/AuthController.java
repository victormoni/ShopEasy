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
import com.victormoni.ecommerce.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Victor Moni
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints de login, registro e tokens JWT")
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        log.info("🔐 Iniciando login para usuário: {}", body.getUsername());
        ResponseEntity<AuthResponse> response = ResponseEntity.ok(authService.login(body));
        log.info("✅ Login finalizado para usuário: {}", body.getUsername());
        return response;
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest body) {
        log.info("♻️ Solicitado refresh de token");
        ResponseEntity<AuthResponse> response = ResponseEntity.ok(authService.refresh(body));
        log.info("✅ Refresh token concluído com sucesso");
        return response;
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<SuccessResponse> register(@Valid @RequestBody RegisterRequest body) {
        log.info("📝 Solicitado registro de novo usuário: {}", body.getUsername());
        ResponseEntity<SuccessResponse> response = ResponseEntity.ok(authService.register(body));
        log.info("✅ Registro concluído para usuário: {}", body.getUsername());
        return response;
    }
}