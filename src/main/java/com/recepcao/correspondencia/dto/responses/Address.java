package com.recepcao.correspondencia.dto.responses;

import lombok.Data;

@Data
public class Address {
    private String street;
    private String number;
    private String neighborhood;
    private String city;
    private State state;
    private String zipCode;
    private String additionalDetails;
}
