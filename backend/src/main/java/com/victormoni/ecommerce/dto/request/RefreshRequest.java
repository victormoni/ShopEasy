package com.victormoni.ecommerce.dto.request;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @author Victor Moni
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {

    @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9…", description = "Token de atualização JWT que expira em 7 dias")
    @NotBlank
    private String refreshToken;
}
