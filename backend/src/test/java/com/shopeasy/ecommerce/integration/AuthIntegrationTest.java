package com.shopeasy.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopeasy.ecommerce.dto.request.LoginRequest;
import com.shopeasy.ecommerce.dto.request.RegisterRequest;
import com.shopeasy.ecommerce.model.Role;
import com.shopeasy.ecommerce.model.User;
import com.shopeasy.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();

                User admin = User.builder()
                                .username("adminUser")
                                .password(passwordEncoder.encode("1234"))
                                .role(Role.ADMIN)
                                .build();
                userRepository.save(admin);
        }

        @Test
        void shouldRegisterAndLoginSuccessfully() throws Exception {

                RegisterRequest register = new RegisterRequest();
                register.setUsername("joao");
                register.setPassword("senha123");
                register.setRole("USER");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(register)))
                                .andExpect(status().isOk());

                LoginRequest login = new LoginRequest();
                login.setUsername("joao");
                login.setPassword("senha123");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andExpect(jsonPath("$.refreshToken").exists());
        }

        @Test
        void shouldFailLoginWithInvalidCredentials() throws Exception {

                LoginRequest login = new LoginRequest();
                login.setUsername("invalido");
                login.setPassword("naoExiste");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnLoggedInUserDetails() throws Exception {

                User u = new User();
                u.setUsername("maria");
                u.setPassword(passwordEncoder.encode("abc123"));
                u.setRole(Role.USER);
                userRepository.save(u);

                LoginRequest login = new LoginRequest();
                login.setUsername("maria");
                login.setPassword("abc123");
                String loginJson = objectMapper.writeValueAsString(login);

                String resp = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                String accessToken = objectMapper.readTree(resp).get("accessToken").asText();

                mockMvc.perform(get("/api/users/me")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value("maria"))
                                .andExpect(jsonPath("$.role").value("USER"));
        }
}
