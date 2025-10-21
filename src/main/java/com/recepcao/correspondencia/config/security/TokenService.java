package com.recepcao.correspondencia.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.recepcao.correspondencia.entities.UserAthena;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.cookies.secrets}")
    private String jwtCookie;

    public String generateToken(UserAthena user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getEmail()) // verificação através do email
                    .withExpiresAt(genExpirationDate())
                    .sign(algorithm);
        } catch(JWTCreationException exception){
            throw new RuntimeException("Erro para gerar o token", exception);
        }
    }

    public ResponseCookie generateCookie(UserAthena userPrincipal) {
        String jwt = generateToken(userPrincipal);

        return ResponseCookie.from(jwtCookie, jwt)
                .path("/") // ✅ cookie vale para toda a app
                .maxAge(24 * 60 * 60)
                .httpOnly(true) // ✅ protege contra acesso JS
                .secure(true) // ✅ em localhost, pode ser false
                .sameSite("None") // ✅ necessário para enviar entre origens
                .build();
    }


    public String getJwtFromCookies(HttpServletRequest httpServletRequest){
        Cookie cookie = WebUtils.getCookie(httpServletRequest, jwtCookie);

        if(cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }

    }

    public ResponseCookie getCleanCookie() {
        return ResponseCookie.from(jwtCookie, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
    }


    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "NULL";
        }
    }

    private Instant genExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
