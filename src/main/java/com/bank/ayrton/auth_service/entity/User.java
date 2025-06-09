package com.bank.ayrton.auth_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private String role; // digamos q todos son administradores (ADMIN, USER, etc.)
}