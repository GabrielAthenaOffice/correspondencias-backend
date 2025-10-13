package com.recepcao.correspondencia.repositories;

import com.recepcao.correspondencia.entities.Correspondencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorrespondenciaRepository extends JpaRepository<Correspondencia, Long> {
    List<Correspondencia> findByNomeEmpresaConexaIgnoreCase(String nomeEmpresaConexa);
    List<Correspondencia> findByNomeEmpresaConexaContainingIgnoreCase(String trecho);
    List<Correspondencia> findByNomeEmpresaConexaOrderByDataRecebimentoDesc(String nomeEmpresaConexa);
}
