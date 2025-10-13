package com.recepcao.correspondencia.dto.contracts;

import com.recepcao.correspondencia.entities.Correspondencia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailServiceDTO {
    private String emailDestino;
    private String nomeEmpresaConexa;
}
