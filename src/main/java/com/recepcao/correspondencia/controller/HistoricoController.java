package com.recepcao.correspondencia.controller;


import com.recepcao.correspondencia.clients.AppConstants;
import com.recepcao.correspondencia.dto.HistoricoResponse;
import com.recepcao.correspondencia.entities.HistoricoInteracao;
import com.recepcao.correspondencia.repositories.HistoricoInteracaoRepository;
import com.recepcao.correspondencia.services.HistoricoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/historicos")
@RequiredArgsConstructor
public class HistoricoController {

    private final HistoricoInteracaoRepository historicoRepository;
    private final HistoricoService historicoService;

    /**
     * Lista todo o histórico com paginação
     */
    @GetMapping("/todos-processos")
    public ResponseEntity<HistoricoResponse> buscarTodoOHistorico(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                                                                  @RequestParam(name = "pageSize", defaultValue = "50", required = false) Integer pageSize,
                                                                  @RequestParam(name = "sortBy", defaultValue = "id", required = false) String sortBy,
                                                                  @RequestParam(name = "sortOrder", defaultValue = "desc", required = false) String sortOrder) {

        HistoricoResponse historicoResponse = historicoService.todoOHistorico(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(historicoResponse, HttpStatus.OK);
    }

    /**
     * Filtra histórico por entidade e/ou ação realizada
     */
    @GetMapping("/filtrar-por-entidade")
    public ResponseEntity<List<HistoricoInteracao>> filtrarHistorico(
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) String acaoRealizada
    ) {
        List<HistoricoInteracao> historicos;

        if (entidade != null && acaoRealizada != null) {
            historicos = historicoRepository.findByEntidadeAndAcaoRealizada(entidade, acaoRealizada);
        } else if (entidade != null) {
            historicos = historicoRepository.findByEntidade(entidade);
        } else {
            historicos = historicoRepository.findAll();
        }

        return ResponseEntity.ok(historicos);
    }

    /**
     * Busca histórico por data específica
     */
    @GetMapping("/por-data")
    public ResponseEntity<List<HistoricoInteracao>> buscarHistoricoPorData(
            @RequestParam LocalDate data
    ) {
        List<HistoricoInteracao> historicos = historicoRepository.findByDataHoraBetween(
                data.atStartOfDay(),
                data.atTime(23, 59, 59)
        );
        return ResponseEntity.ok(historicos);
    }

    /**
     * Busca histórico de uma correspondência específica
     */
    @GetMapping("/correspondencia/{correspondenciaId}")
    public ResponseEntity<List<HistoricoInteracao>> buscarHistoricoCorrespondencia(
            @PathVariable Long correspondenciaId
    ) {
        List<HistoricoInteracao> historicos = historicoRepository.findByEntidadeAndEntidadeId("Correspondencia", correspondenciaId);
        return ResponseEntity.ok(historicos);
    }

    /**
     * Busca histórico de uma empresa específica
     */
    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<HistoricoInteracao>> buscarHistoricoEmpresa(
            @PathVariable Long empresaId
    ) {
        List<HistoricoInteracao> historicos = historicoRepository.findByEntidadeAndEntidadeId("Empresa", empresaId);
        return ResponseEntity.ok(historicos);
    }

    /**
     * Busca histórico por período
     */
    @GetMapping("/por-periodo")
    public ResponseEntity<List<HistoricoInteracao>> buscarHistoricoPorPeriodo(
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim
    ) {
        List<HistoricoInteracao> historicos = historicoRepository.findByDataHoraBetween(
                dataInicio.atStartOfDay(),
                dataFim.atTime(23, 59, 59)
        );
        return ResponseEntity.ok(historicos);
    }

    /**
     * Busca histórico por período com paginação
     */
    @GetMapping("/por-periodo-paginado")
    public ResponseEntity<HistoricoResponse> buscarHistoricoPorPeriodoPaginado(
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim,
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "50", required = false) Integer pageSize
    ) {
        HistoricoResponse historicoResponse = historicoService.buscarHistoricoPorPeriodo(dataInicio, dataFim, pageNumber, pageSize);
        return ResponseEntity.ok(historicoResponse);
    }

    /**
     * Busca histórico dos últimos N dias
     */
    @GetMapping("/ultimos-dias")
    public ResponseEntity<List<HistoricoInteracao>> buscarHistoricoUltimosDias(
            @RequestParam(defaultValue = "7") int dias
    ) {
        List<HistoricoInteracao> historicos = historicoService.buscarHistoricoUltimosDias(dias);
        return ResponseEntity.ok(historicos);
    }

    /**
     * Busca histórico por ação realizada
     */
    @GetMapping("/por-acao")
    public ResponseEntity<List<HistoricoInteracao>> buscarHistoricoPorAcao(
            @RequestParam String acaoRealizada
    ) {
        List<HistoricoInteracao> historicos = historicoService.buscarHistoricoPorAcao(acaoRealizada);
        return ResponseEntity.ok(historicos);
    }
}

