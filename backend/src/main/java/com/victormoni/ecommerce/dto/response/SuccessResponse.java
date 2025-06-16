package com.victormoni.ecommerce.dto.response;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * @author Victor Moni
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse {

    @Schema(example = "Usuário registrado com sucesso")
    private String message;
}
