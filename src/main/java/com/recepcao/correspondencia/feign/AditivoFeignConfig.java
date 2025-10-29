package com.recepcao.correspondencia.feign;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AditivoFeignConfig {

    @Value("${api.aditivo.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return requestTemplate -> {
            // Adiciona a API Key automaticamente em TODAS as requisições para Aditivo
            requestTemplate.header("X-API-Key", apiKey);
        };
    }
}
