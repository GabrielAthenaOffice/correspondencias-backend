package com.recepcao.correspondencia.dto.responses;

import lombok.Data;

import java.util.List;

@Data
public class ConexaCustomerListResponse {
    private List<CustomerResponse> data;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}
