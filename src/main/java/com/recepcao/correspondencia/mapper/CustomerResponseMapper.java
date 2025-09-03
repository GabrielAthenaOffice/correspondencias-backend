package com.recepcao.correspondencia.mapper;

import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import com.recepcao.correspondencia.entities.AddressEntity;
import com.recepcao.correspondencia.entities.Customer;
import com.recepcao.correspondencia.entities.LegalPersonEntity;

public class CustomerResponseMapper {

    public static Customer fromCustomerResponse(CustomerResponse response) {
        Customer customer = new Customer();
        customer.setCustomerId(response.getCustomerId());
        customer.setName(response.getName());
        customer.setFirstName(response.getFirstName());
        customer.setFieldOfActivity(response.getFieldOfActivity());
        customer.setIsActive(response.getIsActive());
        customer.setEmailsMessage(response.getEmailsMessage());
        customer.setPhones(response.getPhones());

        // Endereço
        if (response.getAddress() != null) {
            AddressEntity endereco = new AddressEntity();
            endereco.setRua(response.getAddress().getStreet());
            endereco.setNumero(response.getAddress().getNumber());
            endereco.setBairro(response.getAddress().getNeighborhood());
            endereco.setCidade(response.getAddress().getCity());
            endereco.setEstado(response.getAddress().getState() != null ? response.getAddress().getState().getName() : null);
            endereco.setCep(response.getAddress().getZipCode());
            customer.setEndereco(endereco);
        }

        // Representante Legal
        if (response.getLegalPerson() != null) {
            LegalPersonEntity legalPerson = new LegalPersonEntity();
            legalPerson.setCnpj(response.getLegalPerson().getCnpj());
            legalPerson.setFoundationDate(response.getLegalPerson().getFoundationDate());
            legalPerson.setMunicipalInscription(response.getLegalPerson().getMunicipalInscription());
            legalPerson.setStateInscription(response.getLegalPerson().getStateInscription());
            customer.setLegalPerson(legalPerson);
        }

        return customer;
    }
}

