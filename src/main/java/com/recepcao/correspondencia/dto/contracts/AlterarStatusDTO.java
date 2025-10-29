package com.recepcao.correspondencia.dto.contracts;

import com.recepcao.correspondencia.mapper.enums.StatusCorresp;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusDTO(
        @NotNull StatusCorresp status,
        String motivo,       // opcional – para histórico
        String alteradoPor   // opcional – quem mudou
) {}
