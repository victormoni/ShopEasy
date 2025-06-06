package com.victormoni.ecommerce.service;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.victormoni.ecommerce.exception.UnauthorizedException;
import com.victormoni.ecommerce.service.impl.AuthServiceImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 *
 * @author Victor Moni
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    void shouldAuthenticateSuccessfully() {
        String username = "fulano";
        String password = "1234";

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authResponse = mock(Authentication.class);

        when(authenticationManager.authenticate(authRequest)).thenReturn(authResponse);

        Authentication result = authService.authenticate(username, password);

        assertEquals(authResponse, result);
        verify(authenticationManager).authenticate(authRequest);
    }

    @Test
    void shouldThrowUnauthorizedException_WhenAuthenticationFails() {
        String username = "fulano";
        String password = "wrongpass";

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        when(authenticationManager.authenticate(authRequest))
                .thenThrow(new BadCredentialsException("Credenciais inválidas"));

        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> authService.authenticate(username, password)
        );

        assertEquals("Falha na autenticação", ex.getMessage());
    }
}
