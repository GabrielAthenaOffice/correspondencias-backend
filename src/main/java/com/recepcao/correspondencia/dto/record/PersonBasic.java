package com.recepcao.correspondencia.dto.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PersonBasic(String cpf) {
}
