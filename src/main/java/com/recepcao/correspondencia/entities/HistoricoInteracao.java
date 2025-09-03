package com.recepcao.correspondencia.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "historico_interacoes")
public class HistoricoInteracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidade; // Empresa ou Correspondência
    private Long entidadeId; // ID da entidade relacionada
    private String acaoRealizada; // Ex: "Aviso de Aditivo Enviado", "Uso Indevido Detectado"
    private String detalhe; // Detalhes adicionais
    private LocalDateTime dataHora;
}

