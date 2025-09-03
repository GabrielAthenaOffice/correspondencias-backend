package com.recepcao.correspondencia.services.arquivos;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnderecoValidatorService {

    private static final List<String> ENDERECOS_ATHENA = List.of(
            "Travessa Quintino Bocaiúva, 230, São Paulo, SP, 66060-232",
            "Rua Rio Grande do Norte, 1560, Belo Horizonte, MG, 30130-130",
            "AVENIDA SETE DE SETEMBRO, 5388, Curitiba, PR, 80240-000",
            "Rua José Bernardino, 97, Campina Grande, PB, 58408-027",
            "Avenida Desembargador Vitor Lima, 260, Florianópolis, SC, 88040-400",
            "Avenida Dom Luís, 500, Fortaleza, CE, 60160-230",
            "AVENIDA REPÚBLICA DO LÍBANO, 2341, Goiânia, GO, 74125-904",
            "Avenida Governador Argemiro de Figueiredo, 210, João Pessoa, PB, 58037-030",
            "Rua Franco de Sá, 310, Manaus, AM, 69079- 210",
            "Rua DR. Luiz Felipe Camara, 55, Rio Grande do Norte, RN",
            "Rua General Neto, 71, Porto Alegre, RS, 90560-020",
            "Avenida Ayrton Senna, 2500, Rio de Janeiro, RJ, 22775-003",
            "José Aderval Chaves, 78, Recife, PE, 51111-030",
            "Avenida Paulista, 1471, São Paulo, SP, 01311-200",
            "Rua MITRA, 10, São Luís, Maranhão 65075-770",
            "Avenida Tancredo Neves, 1485, Salvador, BA, 41820-020",
            "Avenida Américo Buaiz, 501, Vitória, ES, 29050-423",
            "AVENIDA AYRTON SENNA DA SILVA, 2198, Jaboatão dos Guararapes, PE, 54410-240"

    );

    public boolean enderecoPertenceAthena(String enderecoEncontrado) {
        return ENDERECOS_ATHENA.stream()
                .anyMatch(endereco -> endereco.equalsIgnoreCase(enderecoEncontrado));
    }


}
