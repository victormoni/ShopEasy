package com.shopeasy.ecommerce.service.impl;

import com.shopeasy.ecommerce.dto.request.RegisterRequest;
import com.shopeasy.ecommerce.dto.request.UpdateUserRequest;
import com.shopeasy.ecommerce.exception.BusinessException;
import com.shopeasy.ecommerce.exception.ResourceNotFoundException;
import com.shopeasy.ecommerce.model.Role;
import com.shopeasy.ecommerce.model.User;
import com.shopeasy.ecommerce.repository.UserRepository;
import com.shopeasy.ecommerce.service.UserService;
import java.util.List;
import org.springframework.stereotype.Service;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        log.info("📄 Listando todos os usuários");
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        log.info("🔍 Buscando usuário por ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        log.info("🔍 Buscando usuário por username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        log.info("❓ Verificando existência de username: {}", username);
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public User save(User user) {
        log.info("💾 Salvando usuário: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User create(RegisterRequest dto) {
        log.info("📝 Criando novo usuário: {}", dto.getUsername());
        if (userRepository.existsByUsername(dto.getUsername())) {
            log.warn("❌ Usuário já existe: {}", dto.getUsername());
            throw new BusinessException("Usuário já existe");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.valueOf(dto.getRole().toUpperCase()))
                .build();

        User saved = userRepository.save(user);
        log.info("✅ Usuário criado com sucesso: {}", saved.getUsername());
        return saved;
    }

    @Override
    @Transactional
    public User update(String username, UpdateUserRequest dto) {
        log.info("✏️ Atualizando usuário: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("❌ Usuário não encontrado: {}", username);
                    return new ResourceNotFoundException("Usuário não encontrado: " + username);
                });

        boolean altered = false;

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            altered = true;
            log.debug("🔐 Senha do usuário atualizada");
        }

        if (dto.getRole() != null) {
            user.setRole(Role.valueOf(dto.getRole().toUpperCase()));
            altered = true;
            log.debug("🛡️ Papel do usuário atualizado para: {}", dto.getRole().toUpperCase());
        }

        if (!altered) {
            log.warn("⚠️ Nenhuma informação fornecida para atualizar o usuário: {}", username);
            throw new BusinessException("Nenhum dado para atualizar");
        }

        User updated = userRepository.save(user);
        log.info("✅ Usuário atualizado com sucesso: {}", updated.getUsername());
        return updated;
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        log.info("🗑️ Deletando usuário: {}", username);
        userRepository.deleteByUsername(username);
        log.info("✅ Usuário deletado com sucesso: {}", username);
    }
}