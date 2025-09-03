package com.recepcao.correspondencia.repositories;

import com.recepcao.correspondencia.entities.HistoricoInteracao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricoInteracaoRepository extends JpaRepository<HistoricoInteracao, Long> {

    List<HistoricoInteracao> findByEntidade(String entidade);

    List<HistoricoInteracao> findByEntidadeAndAcaoRealizada(String entidade, String acaoRealizada);

    List<HistoricoInteracao> findByEntidadeAndEntidadeId(String entidade, Long entidadeId);

    List<HistoricoInteracao> findByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim);

    Page<HistoricoInteracao> findByDataHoraBetween(LocalDateTime dataInicio, LocalDateTime dataFim, Pageable pageable);

    List<HistoricoInteracao> findByEntidadeOrderByDataHoraDesc(String entidade);

    List<HistoricoInteracao> findByAcaoRealizadaContainingIgnoreCase(String acaoRealizada);

}
