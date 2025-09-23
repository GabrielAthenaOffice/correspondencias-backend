package com.recepcao.correspondencia.feign;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AditivoRequestDTO {

    private String empresaId;

    private String unidadeNome;
    private String unidadeCnpj;
    private String unidadeEndereco;

    private String pessoaFisicaNome;
    private String pessoaFisicaCpf;
    private String pessoaFisicaEndereco;

    private LocalDate dataInicioContrato;
    private String pessoaJuridicaNome;
    private String pessoaJuridicaCnpj;

    private String pessoaJuridicaEndereco;

    private String localData;
}
