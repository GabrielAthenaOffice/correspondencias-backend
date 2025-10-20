package com.recepcao.correspondencia.config.security;

import com.recepcao.correspondencia.repositories.UserRepository;
import com.recepcao.correspondencia.services.security.AuthorizationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final AuthorizationService authorizationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {

            String token = tokenService.getJwtFromCookies(request);

            String cookieValue = tokenService.getJwtFromCookies(request);
            System.out.println("🧩 Cookie recebido no backend: " + cookieValue);
            System.out.println("🔍 Headers: " + request.getHeaderNames().asIterator().toString());


            if (token != null) {
                String subject = tokenService.validateToken(token);


                if (subject != null && !"NULL".equals(subject)) {
                    UserDetails user = authorizationService.loadUserByUsername(subject);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao autenticar usuário: {}", e);
        }

        // 🔥 Continua a cadeia normalmente
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();


        return path.startsWith("/auth/login")
                || path.startsWith("/auth/register")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/ws")
                || path.startsWith("/app");
    }


}
