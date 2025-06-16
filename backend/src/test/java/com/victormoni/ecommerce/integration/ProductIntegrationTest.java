package com.victormoni.ecommerce.integration;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victormoni.ecommerce.dto.request.ProductRequest;
import com.victormoni.ecommerce.model.Role;
import com.victormoni.ecommerce.model.User;
import com.victormoni.ecommerce.repository.OrderRepository;
import com.victormoni.ecommerce.repository.ProductRepository;
import com.victormoni.ecommerce.repository.UserRepository;
import com.victormoni.ecommerce.security.CustomUserDetails;
import com.victormoni.ecommerce.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * @author Victor Moni
 */

@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("1234"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        var userDetails = new CustomUserDetails(admin, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        adminToken = jwtUtil.generateToken(userDetails);
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {

        ProductRequest request = new ProductRequest();
        request.setName("Camiseta Teste");
        request.setDescription("Produto de teste em integração");
        request.setPrice(BigDecimal.valueOf(29.99));
        request.setStock(50);

        mockMvc.perform(post("/api/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Camiseta Teste"))
                .andExpect(jsonPath("$.price").value(29.99))
                .andExpect(jsonPath("$.stock").value(50));
    }

    @Test
    void shouldReturnForbidden_WhenNoTokenProvided() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Outro Produto");
        request.setDescription("Sem token");
        request.setPrice(BigDecimal.valueOf(10.00));
        request.setStock(10);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
