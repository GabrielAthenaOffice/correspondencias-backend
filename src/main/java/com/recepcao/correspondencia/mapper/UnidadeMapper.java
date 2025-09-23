package com.recepcao.correspondencia.mapper;

import com.recepcao.correspondencia.feign.AditivoContratual;
import com.recepcao.correspondencia.feign.AditivoRequestDTO;

import java.time.LocalDateTime;

public class UnidadeMapper {
    public static AditivoContratual toEntity(AditivoRequestDTO dto) {
        AditivoContratual aditivo = new AditivoContratual();
        aditivo.setEmpresaId(Long.valueOf(dto.getEmpresaId()));

        aditivo.setUnidadeNome(dto.getUnidadeNome());
        aditivo.setUnidadeCnpj(dto.getUnidadeCnpj());
        aditivo.setUnidadeEndereco(dto.getUnidadeEndereco());

        aditivo.setPessoaFisicaNome(dto.getPessoaFisicaNome());
        aditivo.setPessoaFisicaCpf(dto.getPessoaFisicaCpf());
        aditivo.setPessoaFisicaEndereco(dto.getPessoaFisicaEndereco());

        aditivo.setDataInicioContrato(dto.getDataInicioContrato());

        aditivo.setPessoaJuridicaNome(dto.getPessoaJuridicaNome());
        aditivo.setPessoaJuridicaCnpj(dto.getPessoaJuridicaCnpj());
        aditivo.setPessoaJuridicaEndereco(dto.getPessoaJuridicaEndereco());

        aditivo.setDataCriacao(LocalDateTime.parse(dto.getLocalData()));

        return aditivo;
    }
}
