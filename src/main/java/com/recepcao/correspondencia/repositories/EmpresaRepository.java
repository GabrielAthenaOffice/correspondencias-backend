package com.recepcao.correspondencia.repositories;

import com.recepcao.correspondencia.entities.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByNomeEmpresa(String nomeEmpresa);

    @Query("SELECT e FROM Empresa e WHERE UPPER(e.nomeEmpresa) = UPPER(:nomeEmpresa)")
    Optional<Empresa> findByNomeEmpresaIgnoreCase(@Param("nomeEmpresa") String nomeEmpresa);
}
