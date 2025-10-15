package com.recepcao.correspondencia.entities;

import com.recepcao.correspondencia.mapper.enums.StatusCorresp;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private LocalDateTime dataRecebimento;

    private LocalDate dataAvisoConexa;

    @ElementCollection
    @CollectionTable(name = "correspondencia_anexos",
            joinColumns = @JoinColumn(name = "correspondencia_id"))
    @Column(name = "arquivo_url", length = 255)
    private List<String> anexos = new ArrayList<>();

    // (opcional) mantenha temporariamente p/ compatibilidade e remova depois
    // @Deprecated
    // private String fotoCorrespondencia;
}
