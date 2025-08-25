package com.futbol.zire_fk.repository;

import com.futbol.zire_fk.entity.Koch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface KochRepository extends JpaRepository<Koch, Long> {
    Optional<Koch> findByUsername(String username);
}




