package com.recepcao.correspondencia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "conexa.api")
@Getter
@Setter
public class ConexaApiConfig {
    private String baseUrl;
    private String token;
}
