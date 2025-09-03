package com.recepcao.correspondencia.dto.contracts;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ComplementaryService {
    private Long productOrServiceId;
    private String startDate;
    private String endDate;
    private Boolean isActive;
    private Integer quantity;
    private BigDecimal amount;
    private String notes;
}
