package com.bank.ayrton.auth_service.controller;

import com.bank.ayrton.auth_service.api.auth.AuthService;
import com.bank.ayrton.auth_service.dto.LoginRequest;
import com.bank.ayrton.auth_service.dto.RegisterRequest;
import com.bank.ayrton.auth_service.dto.UserDto;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@RequestBody RegisterRequest request) {
        return authService.register(request)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error.getMessage())));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<String>> login(@RequestBody LoginRequest request) {
        return Mono.from((Publisher<String>) authService.login(request))
                .map(token -> ResponseEntity.ok("Bearer " + token))
                .onErrorResume(error ->
                        Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error.getMessage()))
                );
    }
}