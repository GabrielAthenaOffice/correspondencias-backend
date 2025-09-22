package com.recepcao.correspondencia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnidadeDTO {
    private String unidadeNome;
    private String unidadeCnpj;
    private String unidadeEndereco;
}

