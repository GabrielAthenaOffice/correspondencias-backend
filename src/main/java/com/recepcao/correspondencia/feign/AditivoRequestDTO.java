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
    @NotBlank
    private String empresaId;

    @NotBlank
    private String unidadeNome;

    @NotBlank
    private String unidadeCnpj;

    @NotBlank
    private String unidadeEndereco;

    @NotBlank
    private String pessoaFisicaNome;

    @NotBlank
    private String pessoaFisicaCpf;

    @NotBlank
    private String pessoaFisicaEndereco;

    @NotNull
    private LocalDate dataInicioContrato;

    @NotBlank
    private String pessoaJuridicaNome;

    @NotBlank
    private String pessoaJuridicaCnpj;

    @NotBlank
    private String pessoaJuridicaEndereco;

    @NotBlank
    private String localData;
}
