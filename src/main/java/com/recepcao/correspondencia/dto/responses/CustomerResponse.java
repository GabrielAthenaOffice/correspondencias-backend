package com.recepcao.correspondencia.dto.responses;

import lombok.Data;

import java.util.List;

@Data
public class CustomerResponse {
    private Long customerId;
    private String name;
    private String firstName;
    private String fieldOfActivity;
    private Boolean isActive;
    private List<String> emailsMessage;
    private List<String> phones;
    private Address address;
    private LegalPerson legalPerson;
    private String statusEmpresa; // Novo campo para status
}
