package com.recepcao.correspondencia.entities;

import com.recepcao.correspondencia.mapper.enums.Situacao;
import com.recepcao.correspondencia.mapper.enums.StatusEmpresa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "empresas")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeEmpresa;

    private String cnpj;

    private String unidade;

    private List<String> email;

    private List<String> telefone;

    @Embedded
    private AddressEntity endereco;

    @Enumerated(EnumType.STRING)
    private StatusEmpresa statusEmpresa;

    @Enumerated(EnumType.STRING)
    private Situacao situacao;

    private String mensagem;
}