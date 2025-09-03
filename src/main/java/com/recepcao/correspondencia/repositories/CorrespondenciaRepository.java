package com.recepcao.correspondencia.repositories;

import com.recepcao.correspondencia.entities.Correspondencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorrespondenciaRepository extends JpaRepository<Correspondencia, Long> {
}
