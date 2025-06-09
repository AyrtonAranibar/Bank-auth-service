package com.bank.ayrton.auth_service.dto;

import lombok.Data;

//usamos un dto para no exponer todos los datos del usuario
//sin password por seguridad
@Data
public class UserDto {
    private String username;
    private String email;
    private String role;
}
