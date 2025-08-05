package com.shopeasy.ecommerce.service;

import com.shopeasy.ecommerce.dto.request.LoginRequest;
import com.shopeasy.ecommerce.dto.request.RefreshRequest;
import com.shopeasy.ecommerce.dto.request.RegisterRequest;
import com.shopeasy.ecommerce.dto.response.AuthResponse;
import com.shopeasy.ecommerce.dto.response.SuccessResponse;

public interface AuthService {

    public AuthResponse login(LoginRequest request);

    public AuthResponse refresh(RefreshRequest request);

    public SuccessResponse register(RegisterRequest request);

}
