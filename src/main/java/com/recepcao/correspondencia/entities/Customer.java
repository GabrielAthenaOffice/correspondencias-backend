package com.recepcao.correspondencia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customers")
public class Customer {

    @Id
    private Long customerId;  // mesmo ID do Conexa

    private String name;
    private String firstName;
    private String fieldOfActivity;
    private Boolean isActive;

    @ElementCollection
    private List<String> emailsMessage;

    @ElementCollection
    private List<String> phones;

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AddressEntity endereco;

    @Embedded
    private LegalPersonEntity legalPerson;

    private String statusEmpresa; // Novo campo para status

    public Long getId() {
        return this.customerId;
    }

}

