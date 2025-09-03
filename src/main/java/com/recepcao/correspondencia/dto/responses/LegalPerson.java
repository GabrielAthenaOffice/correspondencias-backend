package com.recepcao.correspondencia.dto.responses;

import lombok.Data;

@Data
public class LegalPerson {
    private String cnpj;
    private String foundationDate;
    private String municipalInscription;
    private String stateInscription;
}
