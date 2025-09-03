package com.recepcao.correspondencia.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LegalPersonEntity {
    private String cnpj;
    private String foundationDate;
    private String municipalInscription;
    private String stateInscription;
}
