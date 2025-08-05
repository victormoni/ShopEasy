package com.victormoni.ecommerce.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
  
    @Schema(example = "novaSenha123", description = "Nova senha do usuário (deixe em branco para não alterar)")
    @Size(min = 4, message = "Senha deve ter ao menos 4 caracteres")
    private String password;

    @Schema(example = "ADMIN", description = "Novo papel do usuário (USER ou ADMIN)")
    @Pattern(regexp = "USER|ADMIN", message = "Role deve ser USER ou ADMIN")
    private String role;
}