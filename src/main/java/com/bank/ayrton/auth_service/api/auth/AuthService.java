package com.bank.ayrton.auth_service.api.auth;

import com.bank.ayrton.auth_service.dto.LoginRequest;
import com.bank.ayrton.auth_service.dto.RegisterRequest;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<Object> register(RegisterRequest request);
    Mono<String> login(LoginRequest request);
}