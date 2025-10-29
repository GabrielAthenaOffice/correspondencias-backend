package com.recepcao.correspondencia.config.exceptions;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AditivoErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 403) {
            log.error("🔐 API Key rejeitada pela API de Aditivo");
            return new SecurityException("Acesso negado - API Key inválida ou expirada");
        }

        if (response.status() == 401) {
            log.error("🔐 Acesso não autorizado à API de Aditivo");
            return new SecurityException("Não autorizado - verifique as credenciais");
        }

        if (response.status() == 404) {
            log.error("🔍 Endpoint não encontrado na API de Aditivo");
            return new RuntimeException("Endpoint não encontrado - verifique a URL");
        }

        log.error("❌ Erro {} na comunicação com API de Aditivo: {}", response.status(), response.reason());
        return defaultErrorDecoder.decode(methodKey, response);
    }
}