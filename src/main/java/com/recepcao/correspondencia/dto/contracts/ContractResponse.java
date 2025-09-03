package com.recepcao.correspondencia.dto.contracts;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ContractResponse {
    private Long contractId;
    private Object productQuotas; // Pode ser específico se souber a estrutura
    private String createdAt;
    private String updatedAt;
    private Long customerId;
    private String paymentFrequency;
    private Long sellerId;
    private String endDate;
    private Long endReasonId;
    private Long planId;
    private Boolean hadProrata;
    private Object lastContractualReadjustment;
    private Integer salesQuantity;
    private BigDecimal refundAmount;
    private Long costCenterId;
    private String dateSalesGeneration;
    private String startDate;
    private Integer dueDay;
    private String contractSummary;
    private String notes;
    private BigDecimal amount;
    private String fidelityDate;
    private List<ComplementaryService> complementaryServices;
}
