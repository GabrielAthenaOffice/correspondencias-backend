package com.recepcao.correspondencia.dto.security;

import com.recepcao.correspondencia.entities.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String nome;
    private String email;
    private String senha;
    private String cargo;
    private UserRole role;
}
