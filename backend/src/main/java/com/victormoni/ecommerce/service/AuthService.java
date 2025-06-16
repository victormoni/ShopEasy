package com.victormoni.ecommerce.service;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

import com.victormoni.ecommerce.dto.request.LoginRequest;
import com.victormoni.ecommerce.dto.request.RefreshRequest;
import com.victormoni.ecommerce.dto.request.RegisterRequest;
import com.victormoni.ecommerce.dto.response.AuthResponse;
import com.victormoni.ecommerce.dto.response.SuccessResponse;

/*
 * @author Victor Moni
 */

public interface AuthService {

    public AuthResponse login(LoginRequest request);

    public AuthResponse refresh(RefreshRequest request);

    public SuccessResponse register(RegisterRequest request);

}
