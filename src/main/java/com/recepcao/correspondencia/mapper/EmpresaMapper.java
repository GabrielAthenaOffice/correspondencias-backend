package com.recepcao.correspondencia.mapper;

import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import com.recepcao.correspondencia.entities.AddressEntity;
import com.recepcao.correspondencia.entities.Empresa;
import com.recepcao.correspondencia.mapper.enums.Situacao;
import com.recepcao.correspondencia.mapper.enums.StatusEmpresa;

import java.util.Collections;


public class EmpresaMapper {

    public static Empresa fromCustomerResponse(CustomerResponse customer) {
        if (customer == null) return null;

        Empresa empresa = new Empresa();
        empresa.setNomeEmpresa(customer.getName());
        empresa.setCnpj(customer.getLegalPerson() != null ? customer.getLegalPerson().getCnpj() : null);
        empresa.setUnidade(customer.getAddress() != null ? customer.getAddress().getState().getName() : null);
        empresa.setEmail(customer.getEmailsMessage() != null && !customer.getEmailsMessage().isEmpty() ? Collections.singletonList(customer.getEmailsMessage().get(0)) : null);
        empresa.setTelefone(customer.getPhones() != null && !customer.getPhones().isEmpty() ? Collections.singletonList(customer.getPhones().get(0)) : null);

        if (customer.getAddress() != null) {
            empresa.setEndereco(new AddressEntity(
                    customer.getAddress().getStreet(),
                    customer.getAddress().getNumber(),
                    customer.getAddress().getNeighborhood(),
                    customer.getAddress().getCity(),
                    customer.getAddress().getState() != null ? customer.getAddress().getState().getAbbreviation() : null,
                    customer.getAddress().getZipCode()
            ));
        }

        empresa.setStatusEmpresa(StatusEmpresa.AGUARDANDO);
        empresa.setSituacao(customer.getLegalPerson() != null && customer.getLegalPerson().getCnpj() != null
                ? Situacao.CNPJ
                : Situacao.CPF);
        empresa.setMensagem(null); // Preencher posteriormente se necessário

        return empresa;
    }

    public static String sanitizeCompanyName(String companyName) {
        if (companyName == null || companyName.isEmpty()) {
            return null;
        }
        // Remove espaços extras e caracteres especiais, se necessário
        return companyName.trim().replaceAll("[^a-zA-Z0-9 ]", "");
    }

}
