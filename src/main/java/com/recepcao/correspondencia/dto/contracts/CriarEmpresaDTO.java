package com.recepcao.correspondencia.dto.contracts;


import jakarta.validation.constraints.NotBlank;

public record CriarEmpresaDTO(@NotBlank String nomeEmpresa) {}

