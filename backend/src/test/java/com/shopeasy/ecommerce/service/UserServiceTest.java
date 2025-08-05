package com.shopeasy.ecommerce.service;

import com.shopeasy.ecommerce.dto.request.RegisterRequest;
import com.shopeasy.ecommerce.dto.request.UpdateUserRequest;
import com.shopeasy.ecommerce.exception.BusinessException;
import com.shopeasy.ecommerce.model.Role;
import com.shopeasy.ecommerce.model.User;
import com.shopeasy.ecommerce.repository.UserRepository;
import com.shopeasy.ecommerce.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldRegisterNewUserSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("fulano");
        request.setPassword("1234");
        request.setRole("USER");

        when(userRepository.existsByUsername("fulano")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.create(request);

        assertEquals("fulano", user.getUsername());
        assertEquals("encoded", user.getPassword());
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("fulano");
        request.setPassword("1234");

        when(userRepository.existsByUsername("fulano")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.create(request));
    }

    @Test
    void shouldUpdateUserRoleAndPassword() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("novaSenha");
        request.setRole("ADMIN");

        User user = User.builder()
                .id(1L)
                .username("fulano")
                .password("old")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("fulano")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("novaSenha")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.update("fulano", request);

        assertEquals("encoded", updated.getPassword());
        assertEquals(Role.ADMIN, updated.getRole());
    }

    @Test
    void shouldThrowWhenNoDataToUpdate() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("");
        request.setRole(null);

        when(userRepository.findByUsername("fulano"))
                .thenReturn(Optional.of(User.builder().id(1L).username("fulano").build()));

        assertThrows(BusinessException.class, () -> userService.update("fulano", request));
    }
}
