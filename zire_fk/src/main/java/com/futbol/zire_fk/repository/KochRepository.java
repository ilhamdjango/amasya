package com.futbol.zire_fk.repository;

import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface KochRepository extends JpaRepository<Koch, Long> {

    Optional<Koch> findByUsername(String username);

    long countByRoleAndActiveTrue(Role role);

    // Səhifələmə üçün
    Page<Koch> findByActiveTrue(Pageable pageable);

    // Soft delete üçün update query
    @Modifying
    @Transactional
    @Query("UPDATE Koch k SET k.active = false WHERE k.id = :id")
    void deactivateById(@Param("id") Long id);

    // Aktiv olanları çəkmək üçün
    List<Koch> findByActiveTrue();
}
