package com.recepcao.correspondencia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CorrespondenciaComEmpresaDTO {
    private Long id;
    private String remetente;
    private String nomeEmpresaConexa;
    private String statusCorresp;
    private LocalDate dataRecebimento;
    private LocalDate dataAvisoConexa;
    private String fotoCorrespondencia;
    // Dados da empresa diretamente
    private String nomeEmpresa;
    private String cnpj;
    private List<String> email;
    private List<String> telefone;
    private String statusEmpresa;
    private String situacao;
    private String mensagem;
} 