package com.victormoni.ecommerce.api;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import com.victormoni.ecommerce.dto.request.RegisterRequest;
import com.victormoni.ecommerce.dto.request.UpdateUserRequest;
import com.victormoni.ecommerce.dto.response.SuccessResponse;
import com.victormoni.ecommerce.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Victor Moni
 */
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
@RequestMapping("/api/users")
public interface UserApi {

        @Operation(summary = "Listar todos os usuários")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuários listados com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autorizado")
        })
        @GetMapping
        ResponseEntity<List<UserResponse>> findAll();

        @Operation(summary = "Buscar usuário por ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                        @ApiResponse(responseCode = "401", description = "Não autorizado")
        })
        @GetMapping("/{id}")
        ResponseEntity<UserResponse> findById(@PathVariable Long id);

        @Operation(summary = "Dados do usuário autenticado", description = "Retorna as informações do usuário atualmente logado.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Dados do usuário retornados com sucesso", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class), examples = @ExampleObject(value = "{\"id\": 42, \"username\": \"fulano\", \"role\": \"USER\"}"))),
                        @ApiResponse(responseCode = "401", description = "Não autorizado – token JWT ausente ou inválido"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        @GetMapping("/me")
        ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails);

        @Operation(summary = "Verificar se nome de usuário existe")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso")
        })
        @GetMapping("/exists/{username}")
        ResponseEntity<Boolean> checkUsername(@PathVariable String username);

        @Operation(summary = "Criar novo usuário")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                        @ApiResponse(responseCode = "409", description = "Usuário já existe")
        })
        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        ResponseEntity<UserResponse> create(
                        @Valid @RequestBody RegisterRequest dto);

        @Operation(summary = "Atualizar usuário existente")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
        })
        @PutMapping("/{username}")
        @PreAuthorize("hasRole('ADMIN')")
        ResponseEntity<UserResponse> update(
                        @PathVariable String username,
                        @Valid @RequestBody UpdateUserRequest dto);

        @Operation(summary = "Excluir um usuário por username")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuário excluído com sucesso"),
                        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
                        @ApiResponse(responseCode = "401", description = "Não autorizado")
        })
        @DeleteMapping("/delete/{username}")
        ResponseEntity<SuccessResponse> deleteByUsername(@PathVariable @NotBlank String username);
}
