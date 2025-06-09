package com.bank.ayrton.auth_service.service.auth;

import com.bank.ayrton.auth_service.api.auth.AuthRepository;
import com.bank.ayrton.auth_service.api.auth.AuthService;
import com.bank.ayrton.auth_service.dto.LoginRequest;
import com.bank.ayrton.auth_service.dto.RegisterRequest;
import com.bank.ayrton.auth_service.dto.UserDto;
import com.bank.ayrton.auth_service.entity.User;
import com.bank.ayrton.auth_service.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String CACHE_PREFIX = "auth:user:";

    @Override
    public Mono<Object> register(RegisterRequest request) {
        return authRepository.findByUsername(request.getUsername())
                .flatMap(user -> Mono.error(new RuntimeException("El usuario ya existe")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = new User();
                    user.setUsername(request.getUsername());
                    user.setEmail(request.getEmail());
                    user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
                    user.setRole("ADMIN");

                    return authRepository.save(user)
                            .flatMap(saved -> {
                                String key = CACHE_PREFIX + saved.getUsername();
                                return redisTemplate.opsForValue()
                                        .set(key, serializeUser(saved), Duration.ofMinutes(10))
                                        .thenReturn(toDto(saved));
                            });
                }));
    }

    @Override
    public Mono<String> login(LoginRequest request) {
        String key = CACHE_PREFIX + request.getUsername();

        return redisTemplate.opsForValue().get(key)
                .flatMap(json -> {
                    User cached = deserializeUser(json);
                    if (BCrypt.checkpw(request.getPassword(), cached.getPassword())) {
                        return Mono.just(jwtUtil.generateToken(cached.getUsername(), cached.getRole()));
                    } else {
                        return Mono.error(new RuntimeException("Credenciales incorrectas (redis)"));
                    }
                })
                .switchIfEmpty(
                        authRepository.findByUsername(request.getUsername())
                                .flatMap(user -> {
                                    if (BCrypt.checkpw(request.getPassword(), user.getPassword())) {
                                        return redisTemplate.opsForValue()
                                                .set(key, serializeUser(user), Duration.ofMinutes(10))
                                                .thenReturn(jwtUtil.generateToken(user.getUsername(), user.getRole()));
                                    } else {
                                        return Mono.error(new RuntimeException("Credenciales incorrectas"));
                                    }
                                })
                                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                );
    }

    private String serializeUser(User user) {
        try {
            return mapper.writeValueAsString(user);
        } catch (Exception error) {
            throw new RuntimeException("Error serializando usuario", error);
        }
    }

    private User deserializeUser(String json) {
        try {
            return mapper.readValue(json, User.class);
        } catch (Exception error) {
            throw new RuntimeException("Error deserializando usuario", error);
        }
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}