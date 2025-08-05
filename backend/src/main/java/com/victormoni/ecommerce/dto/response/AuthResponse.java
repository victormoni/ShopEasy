package com.victormoni.ecommerce.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @Schema(description = "Token de acesso JWT. Usado para autorizar requisições.")
    private String accessToken;

    @Schema(description = "Token de atualização JWT. Usado para obter novo token de acesso.")
    private String refreshToken;
}
