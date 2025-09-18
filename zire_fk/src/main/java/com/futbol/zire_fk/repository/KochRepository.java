package com.futbol.zire_fk.repository;

import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.entity.Role;
import com.futbol.zire_fk.entity.StudentStatus;
import com.futbol.zire_fk.entity.TrainingDeleteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KochRepository extends JpaRepository<Koch, Long> {

    // ACTIVE status ilə username tapmaq
    Optional<Koch> findByUsernameAndAdminStatus(String username, TrainingDeleteStatus adminStatus);

    // Statusdan asılı olmayaraq username ilə tapmaq
    Optional<Koch> findByUsername(String username);

    // Rol + adminStatus = ACTIVE olanların sayı
    long countByRoleAndAdminStatus(Role role, TrainingDeleteStatus adminStatus);



    // Yalnız ACTIVE adminStatus-lu istifadəçiləri gətirmək
    List<Koch> findByAdminStatus(TrainingDeleteStatus adminStatus);

    // ACTIVE və ya DELETED adminStatus-lu istifadəçiləri gətirmək
    List<Koch> findByAdminStatusIn(List<TrainingDeleteStatus> statuses);

    // ACTIVE istifadəçiləri Page ilə
    Page<Koch> findByAdminStatus(TrainingDeleteStatus adminStatus, Pageable pageable);

    // İstifadəçi adı + role ilə tapmaq (super admin və ya digər rollar üçün)
    Optional<Koch> findByUsernameAndRole(String username, Role role);

    // ACTIVE və role = ADMIN olanlar
    @Query("SELECT k FROM Koch k WHERE k.role = :role AND k.adminStatus = :status")
    Page<Koch> findByRoleAndAdminStatus(@Param("role") Role role,
                                        @Param("status") TrainingDeleteStatus status,
                                        Pageable pageable);
}
