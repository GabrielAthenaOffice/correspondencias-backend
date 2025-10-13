package com.recepcao.correspondencia.dto.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConexaContractResponse(Long contractId, Long customerId, String startDate) {
}
