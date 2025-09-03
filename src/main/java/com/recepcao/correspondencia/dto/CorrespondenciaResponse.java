package com.recepcao.correspondencia.dto;

import com.recepcao.correspondencia.entities.Correspondencia;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CorrespondenciaResponse {
    private List<Correspondencia> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private boolean lastPage;

    public CorrespondenciaResponse() {

    }
}
