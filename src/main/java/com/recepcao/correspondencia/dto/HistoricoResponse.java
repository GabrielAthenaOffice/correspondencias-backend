package com.recepcao.correspondencia.dto;

import com.recepcao.correspondencia.entities.HistoricoInteracao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoricoResponse {
    private List<HistoricoInteracao> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;
}
