package com.recepcao.correspondencia.dto.contracts;

import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import lombok.Data;

@Data
public class CustomerWithContractResponse {
    private CustomerResponse customer;
    private ContractResponse contract;

    public CustomerWithContractResponse(CustomerResponse customer, ContractResponse contract) {
        this.customer = customer;
        this.contract = contract;
    }
}
