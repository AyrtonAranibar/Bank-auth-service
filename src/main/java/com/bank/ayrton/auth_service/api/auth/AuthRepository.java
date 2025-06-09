package com.bank.ayrton.auth_service.api.auth;

import com.bank.ayrton.auth_service.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AuthRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUsername(String username);
}