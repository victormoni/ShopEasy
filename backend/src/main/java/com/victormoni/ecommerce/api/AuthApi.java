package com.victormoni.ecommerce.api;

import com.victormoni.ecommerce.dto.request.LoginRequest;
import com.victormoni.ecommerce.dto.request.RefreshRequest;
import com.victormoni.ecommerce.dto.request.RegisterRequest;
import com.victormoni.ecommerce.dto.response.AuthResponse;
import com.victormoni.ecommerce.dto.response.ErrorResponse;
import com.victormoni.ecommerce.dto.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Autenticação", description = "Endpoints de login, registro e tokens JWT")
public interface AuthApi {

        @Operation(summary = "Login", description = "Gera access e refresh tokens para usuário válido")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Login bem-sucedido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name = "OK", value = "{\"accessToken\":\"eyJ…\",\"refreshToken\":\"eyJ…\"}"))),
                        @ApiResponse(responseCode = "400", description = "Dados de login inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Bad Request", value = "{\"error\":\"Usuário e senha são obrigatórios\"}"))),
                        @ApiResponse(responseCode = "401", description = "Usuário ou senha incorretos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Unauthorized", value = "{\"error\":\"Usuário ou senha inválidos\"}")))
        })
        ResponseEntity<AuthResponse> login(@Valid LoginRequest body);

        @Operation(summary = "Refresh Token", description = "Renova o access token a partir do refresh token")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Token renovado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class), examples = @ExampleObject(name = "OK", value = "{\"accessToken\":\"eyJ…\",\"refreshToken\":\"eyJ…\"}"))),
                        @ApiResponse(responseCode = "400", description = "Refresh token não informado ou inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Bad Request", value = "{\"error\":\"Refresh token é obrigatório\"}"))),
                        @ApiResponse(responseCode = "401", description = "Refresh token expirado ou inválido", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class), examples = @ExampleObject(name = "Unauthorized", value = "{\"error\":\"Refresh token expirado ou inválido\"}")))
        })
        ResponseEntity<AuthResponse> refresh(@Valid RefreshRequest body);

        @Operation(summary = "Register", description = "Registra um novo usuário")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Dados de registro inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "409", description = "Usuário já existe", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        ResponseEntity<?> register(@Valid RegisterRequest body);
}
