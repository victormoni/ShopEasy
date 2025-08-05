package com.victormoni.ecommerce.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Schema(example = "fulano", description = "Nome de usuário")
    @NotBlank
    private String username;

    @Schema(example = "1234", description = "Senha do usuário")
    @NotBlank
    private String password;
}
