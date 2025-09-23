package com.recepcao.correspondencia.feign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AditivoRepository extends JpaRepository<AditivoContratual, Long> {
}
