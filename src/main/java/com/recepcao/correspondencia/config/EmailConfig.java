package com.recepcao.correspondencia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email")
@Getter
@Setter
public class EmailConfig {
    private String remetente;
}

