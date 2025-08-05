package com.victormoni.ecommerce.service;

import com.victormoni.ecommerce.dto.request.LoginRequest;
import com.victormoni.ecommerce.dto.request.RefreshRequest;
import com.victormoni.ecommerce.dto.request.RegisterRequest;
import com.victormoni.ecommerce.dto.response.AuthResponse;
import com.victormoni.ecommerce.dto.response.SuccessResponse;

public interface AuthService {

    public AuthResponse login(LoginRequest request);

    public AuthResponse refresh(RefreshRequest request);

    public SuccessResponse register(RegisterRequest request);

}
