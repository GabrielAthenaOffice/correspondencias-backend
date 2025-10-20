package com.recepcao.correspondencia.repositories;

import com.recepcao.correspondencia.entities.UserAthena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAthena, Long> {
    Optional<UserAthena> findByEmail(String email);
    Optional<UserAthena> findByNome(String nome);
}
