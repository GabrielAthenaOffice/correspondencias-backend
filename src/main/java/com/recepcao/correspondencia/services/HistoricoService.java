package com.recepcao.correspondencia.services;

import com.recepcao.correspondencia.config.APIExceptions;
import com.recepcao.correspondencia.dto.HistoricoResponse;
import com.recepcao.correspondencia.entities.HistoricoInteracao;
import com.recepcao.correspondencia.repositories.HistoricoInteracaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoricoService {

    private final HistoricoInteracaoRepository historicoRepository;

    public void registrar(String entidade, Long entidadeId, String acao, String detalhe) {
        HistoricoInteracao historico = new HistoricoInteracao();
        historico.setEntidade(entidade);
        historico.setEntidadeId(entidadeId);
        historico.setAcaoRealizada(acao);
        historico.setDetalhe(detalhe);
        historico.setDataHora(LocalDateTime.now());

        historicoRepository.save(historico);
    }

    public HistoricoResponse todoOHistorico(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        // Validate sortBy field exists in the entity
        if (!isValidSortField(sortBy)) {
            sortBy = "id"; // Default to id if invalid field
        }

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<HistoricoInteracao> historicoInteracaoPages = historicoRepository.findAll(pageDetails);

        List<HistoricoInteracao> historico = historicoInteracaoPages.getContent();

        // Always return a response, even if empty
        HistoricoResponse historicoResponse = new HistoricoResponse();
        historicoResponse.setContent(historico);
        historicoResponse.setPageNumber(historicoInteracaoPages.getNumber());
        historicoResponse.setPageSize(historicoInteracaoPages.getSize());
        historicoResponse.setTotalElements(historicoInteracaoPages.getTotalElements());
        historicoResponse.setTotalPages(historicoInteracaoPages.getTotalPages());
        historicoResponse.setLastPage(historicoInteracaoPages.isLast());

        return historicoResponse;

    }

    /**
     * Validates if the sort field exists in the HistoricoInteracao entity
     */
    private boolean isValidSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return false;
        }
        
        // Valid fields in HistoricoInteracao entity
        String[] validFields = {"id", "entidade", "entidadeId", "acaoRealizada", "detalhe", "dataHora"};
        for (String field : validFields) {
            if (field.equalsIgnoreCase(sortBy)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Busca histórico por período com paginação
     */
    public HistoricoResponse buscarHistoricoPorPeriodo(LocalDate dataInicio, LocalDate dataFim, 
                                                      Integer pageNumber, Integer pageSize) {
        
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);
        
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, Sort.by("dataHora").descending());
        Page<HistoricoInteracao> historicoPage = historicoRepository.findByDataHoraBetween(inicio, fim, pageDetails);

        HistoricoResponse historicoResponse = new HistoricoResponse();
        historicoResponse.setContent(historicoPage.getContent());
        historicoResponse.setPageNumber(historicoPage.getNumber());
        historicoResponse.setPageSize(historicoPage.getSize());
        historicoResponse.setTotalElements(historicoPage.getTotalElements());
        historicoResponse.setTotalPages(historicoPage.getTotalPages());
        historicoResponse.setLastPage(historicoPage.isLast());

        return historicoResponse;
    }

    /**
     * Busca histórico de uma entidade específica
     */
    public List<HistoricoInteracao> buscarHistoricoPorEntidade(String entidade) {
        return historicoRepository.findByEntidadeOrderByDataHoraDesc(entidade);
    }

    /**
     * Busca histórico de uma entidade específica por ID
     */
    public List<HistoricoInteracao> buscarHistoricoPorEntidadeEId(String entidade, Long entidadeId) {
        return historicoRepository.findByEntidadeAndEntidadeId(entidade, entidadeId);
    }

    /**
     * Busca histórico por ação realizada
     */
    public List<HistoricoInteracao> buscarHistoricoPorAcao(String acaoRealizada) {
        return historicoRepository.findByAcaoRealizadaContainingIgnoreCase(acaoRealizada);
    }

    /**
     * Busca histórico dos últimos N dias
     */
    public List<HistoricoInteracao> buscarHistoricoUltimosDias(int dias) {
        LocalDateTime dataInicio = LocalDateTime.now().minusDays(dias);
        LocalDateTime dataFim = LocalDateTime.now();
        return historicoRepository.findByDataHoraBetween(dataInicio, dataFim);
    }
}
