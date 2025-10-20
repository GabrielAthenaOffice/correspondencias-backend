package com.recepcao.correspondencia.config.security;


import com.recepcao.correspondencia.dto.security.UserSimpleDTO;
import org.springframework.http.ResponseCookie;

public record LoginResponseDTO (UserSimpleDTO userDTO, String cookie) {}