package com.victormoni.ecommerce.controller;

import com.victormoni.ecommerce.api.UserApi;
import com.victormoni.ecommerce.dto.request.RegisterRequest;
import com.victormoni.ecommerce.dto.request.UpdateUserRequest;
import com.victormoni.ecommerce.dto.response.SuccessResponse;
import com.victormoni.ecommerce.dto.response.UserResponse;
import com.victormoni.ecommerce.exception.ResourceNotFoundException;
import com.victormoni.ecommerce.mapper.UserMapper;
import com.victormoni.ecommerce.model.User;
import com.victormoni.ecommerce.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("👤 Buscando informações do usuário logado: {}", userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return ResponseEntity.ok(UserMapper.toResponse(user));
    }

    @Override
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<UserResponse>> findAll() {
        log.info("📄 Listando todos os usuários");
        List<UserResponse> users = userService.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole().name())
                        .build())
                .toList();
        return ResponseEntity.ok(users);
    }

    @Override
    @GetMapping("/exists/{username}")
    @Transactional(readOnly = true)
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        log.info("🔍 Verificando existência do usuário: {}", username);
        return ResponseEntity.ok(userService.existsByUsername(username));
    }

    @Override
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        log.info("🔍 Buscando usuário com ID: {}", id);
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID " + id));
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody RegisterRequest dto) {
        log.info("🆕 Criando novo usuário: {}", dto.getUsername());
        User saved = userService.create(dto);
        return ResponseEntity.ok(UserMapper.toResponse(saved));
    }

    @Override
    @PutMapping
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(@RequestParam String username, @Valid @RequestBody UpdateUserRequest dto) {
        log.info("✏️ Atualizando usuário: {}", username);
        User updated = userService.update(username, dto);
        return ResponseEntity.ok(UserMapper.toResponse(updated));
    }

    @Override
    @DeleteMapping("/{username}")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse> deleteByUsername(@PathVariable String username) {
        log.warn("❌ Excluindo usuário: {}", username);
        if (!userService.existsByUsername(username)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + username);
        }
        userService.deleteByUsername(username);
        return ResponseEntity.ok(new SuccessResponse("Usuário excluído com sucesso"));
    }
}