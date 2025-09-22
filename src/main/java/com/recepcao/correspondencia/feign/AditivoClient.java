package com.recepcao.correspondencia.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "aditivo_api", url = "${api.aditivo.url}")
public interface AditivoClient {

    @PostMapping("/aditivos")
    ResponseEntity<AditivoResponseDTO> criarAditivo(@RequestBody AditivoRequestDTO aditivoRequestDTO);
}
