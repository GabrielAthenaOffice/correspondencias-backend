package com.recepcao.correspondencia.feign;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AditivoResponseDTO {
    private String status;
    private String mensagem;
    private String aditivoId;
}
