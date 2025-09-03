package com.recepcao.correspondencia.entities;

import com.recepcao.correspondencia.mapper.enums.StatusCorresp;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "correspondencias")
public class Correspondencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String remetente;
    private String nomeEmpresaConexa;

    @Enumerated(EnumType.STRING)
    private StatusCorresp statusCorresp; // AVISADA, DEVOLVIDA, USO_INDEVIDO, ANALISE

    private LocalDate dataRecebimento;

    private LocalDate dataAvisoConexa;

    @Size(max = 255)
    private String fotoCorrespondencia; // pode ser URL ou path no storage
}
