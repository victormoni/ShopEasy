package com.victormoni.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        private static final String SECURITY_SCHEME_NAME = "bearerAuth";

        @Bean
        public OpenAPI customOpenAPI() {
                Info info = new Info()
                                .title("E-commerce Backend")
                                .version("1.0")
                                .description("API REST em Spring Boot para gerenciamento de produtos e pedidos")
                                .contact(new Contact()
                                                .name("Victor Moni")
                                                .url("https://github.com/victormoni"))
                                .license(new License()
                                                .name("MIT License")
                                                .url("https://opensource.org/licenses/MIT"));

                return new OpenAPI()
                                .info(info)
                                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                                .components(new Components()
                                                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                                                new SecurityScheme()
                                                                                .name(SECURITY_SCHEME_NAME)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")));
        }
}
