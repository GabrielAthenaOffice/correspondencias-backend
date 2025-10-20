package com.recepcao.correspondencia.dto.security;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSimpleDTO {
    private Long id;
    private String nome;
    private String email;
}