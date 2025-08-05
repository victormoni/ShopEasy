package com.victormoni.ecommerce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victormoni.ecommerce.dto.request.OrderItemRequest;
import com.victormoni.ecommerce.dto.request.OrderRequest;
import com.victormoni.ecommerce.kafka.dto.OrderEvent;
import com.victormoni.ecommerce.model.Product;
import com.victormoni.ecommerce.model.Role;
import com.victormoni.ecommerce.model.User;
import com.victormoni.ecommerce.repository.OrderRepository;
import com.victormoni.ecommerce.repository.ProductRepository;
import com.victormoni.ecommerce.repository.UserRepository;
import com.victormoni.ecommerce.security.CustomUserDetails;
import com.victormoni.ecommerce.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private ObjectMapper objectMapper;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private OrderRepository orderRepository;
        @Autowired
        private ProductRepository productRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private JwtUtil jwtUtil;

        private String token;

        @TestConfiguration
        static class KafkaStubConfig {

                @SuppressWarnings("unchecked")
                @Bean
                @Primary
                public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
                        return (KafkaTemplate<String, OrderEvent>) Mockito.mock(KafkaTemplate.class);

                }
        }

        @BeforeEach
        void setUp() {
                orderRepository.deleteAll();
                productRepository.deleteAll();
                userRepository.deleteAll();

                User user = User.builder()
                                .username("cliente1")
                                .password(passwordEncoder.encode("1234"))
                                .role(Role.USER)
                                .build();
                userRepository.save(user);

                Product product = Product.builder()
                                .name("Camiseta Preta")
                                .description("100% algodão")
                                .price(BigDecimal.valueOf(50.00))
                                .stock(100)
                                .build();
                productRepository.save(product);

                var userDetails = new CustomUserDetails(user, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                token = jwtUtil.generateToken(userDetails);
        }

        @Test
        void shouldCreateOrderSuccessfully() throws Exception {
                OrderItemRequest item = OrderItemRequest.builder()
                                .productId(productRepository.findAll().get(0).getId())
                                .quantity(2)
                                .build();

                OrderRequest orderRequest = OrderRequest.builder()
                                .items(List.of(item))
                                .build();

                mockMvc.perform(post("/api/orders")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderRequest)))
                                .andDo(MockMvcResultHandlers.print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.total").value(100.0))
                                .andExpect(jsonPath("$.items[0].quantity").value(2));
        }
}
