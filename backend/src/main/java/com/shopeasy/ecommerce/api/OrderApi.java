package com.shopeasy.ecommerce.api;

import com.shopeasy.ecommerce.dto.request.OrderRequest;
import com.shopeasy.ecommerce.dto.response.OrderResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.shopeasy.ecommerce.dto.response.ErrorResponse;
import com.shopeasy.ecommerce.model.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Pedidos", description = "Operações de gerenciamento de pedidos")
@RequestMapping("/api/orders")
public interface OrderApi {

        @Operation(summary = "Listar todos os pedidos", description = "Retorna a lista de pedidos cadastrados")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Pedidos retornados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping
        List<OrderResponse> list();

        @Operation(summary = "Listar pedidos do usuário autenticado com paginação")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Pedidos retornados com sucesso"),
                        @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
        })
        @GetMapping("/me")
        ResponseEntity<Page<OrderResponse>> findMyOrders(
                        @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String direction);

        @Operation(summary = "Criar um novo pedido", description = "Recebe um OrderRequest e cria um pedido")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autorizado")
        })
        @PostMapping
        ResponseEntity<OrderResponse> create(@AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody OrderRequest request);

        @Operation(summary = "Buscar pedido por ID", description = "Retorna os detalhes de um pedido específico")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Pedido encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autorizado")
        })
        @GetMapping("/{id}")
        OrderResponse findById(@PathVariable Long id);

        @Operation(summary = "Listar pedidos por status", description = "Retorna uma página de pedidos filtrados pelo status informado. Somente ADMIN.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Pedidos retornados com sucesso", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class), arraySchema = @Schema(description = "Lista de pedidos")))),
                        @ApiResponse(responseCode = "401", description = "Não autorizado –> token ausente ou inválido"),
                        @ApiResponse(responseCode = "403", description = "Acesso negado –> papel insuficiente")
        })
        @GetMapping("/status/{status}")
        public ResponseEntity<Page<OrderResponse>> findByStatus(
                        @PathVariable OrderStatus status,
                        @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(schema = @Schema(allowableValues = { "asc",
                                        "desc" })) @RequestParam(defaultValue = "desc") String direction);

        @Operation(summary = "Atualizar pedido", description = "Atualiza um pedido existente pelo ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autorizado")
        })
        @PutMapping("/{id}")
        ResponseEntity<OrderResponse> update(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable Long id,
                        @Valid @RequestBody OrderRequest request);

        @Operation(summary = "Cancelar/excluir pedido", description = "Exclui um pedido pelo ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "204", description = "Pedido excluído com sucesso"),
                        @ApiResponse(responseCode = "404", description = "Pedido não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Não autorizado")
        })
        @DeleteMapping("/{id}")
        ResponseEntity<Void> delete(@PathVariable Long id);
}
