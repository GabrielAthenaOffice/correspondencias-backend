package com.recepcao.correspondencia.feign;

import com.recepcao.correspondencia.entities.Empresa;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class AditivoContratual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento com Empresa
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    private String unidadeNome;      // ATHENA OFFICE LTDA
    private String unidadeCnpj;
    private String unidadeEndereco;

    private String pessoaFisicaNome; // puxado do cadastro
    private String pessoaFisicaCpf;  // preenchido manualmente
    private String pessoaFisicaEndereco; // puxado do cadastro

    private LocalDate dataInicioContrato;

    private String pessoaJuridicaNome; // preenchido manualmente
    private String pessoaJuridicaCnpj; // preenchido manualmente
    private String pessoaJuridicaEndereco; // puxado do cadastro (se houver)

    private LocalDateTime dataCriacao = LocalDateTime.now();
}

