package com.recepcao.correspondencia.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressEntity {

    // não excluir. fazer uso para comparação de endereços posteriormente

    private String rua;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;

    public String enderecoFormatado() {
        return String.format("%s, %s, %s, %s, %s", rua, numero, cidade, estado.toUpperCase(), cep);
    }
}

