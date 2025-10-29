package com.recepcao.correspondencia.feign;

import com.recepcao.correspondencia.config.exceptions.AditivoErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "aditivo-api", url = "${api.aditivo.url}", configuration = AditivoFeignConfig.class)
public interface AditivoClient {

    @PostMapping("/api/aditivos/internal/contratual")
    ResponseEntity<AditivoResponseDTO> criarAditivoContratual(@RequestBody AditivoRequestDTO aditivoRequestDTO);

    @PostMapping("/api/aditivos/internal/contratual/dois-fiadores")
    ResponseEntity<AditivoResponseDTO> criarAditivoDoisFiadores(@RequestBody AditivoRequestDTO aditivoRequestDTO);

    // Adicione no AditivoFeignConfig
    @Bean
    default AditivoErrorDecoder errorDecoder() {
        return new AditivoErrorDecoder();
    }
}
