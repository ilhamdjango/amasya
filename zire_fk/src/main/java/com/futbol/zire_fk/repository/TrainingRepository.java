package com.futbol.zire_fk.repository;

import com.futbol.zire_fk.entity.Training;
import com.futbol.zire_fk.entity.TrainingDeleteStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {

    // Mövcud: Koch-a görə bütün trainings
    List<Training> findByKochId(Long kochId);
    List<Training> findByKochIdAndTrainingDelete(Long kochId, TrainingDeleteStatus trainingDeleteStatus);

    // Pagination üçün Koch-a görə, status parametrli
    @Query("SELECT t FROM Training t WHERE t.koch.id = :kochId AND t.trainingDelete = :status")
    Page<Training> findByKochIdActive(@Param("kochId") Long kochId,
                                      @Param("status") TrainingDeleteStatus status,
                                      Pageable pageable);

    // Pagination üçün bütün aktiv trainings
    @Query("SELECT t FROM Training t WHERE t.trainingDelete = :status")
    Page<Training> findAllActive(@Param("status") TrainingDeleteStatus status,
                                 Pageable pageable);

    // Pagination üçün bütün trainings, müxtəlif statuslar
    @Query("SELECT t FROM Training t WHERE t.trainingDelete IN :statuses")
    Page<Training> findAllByStatuses(@Param("statuses") List<TrainingDeleteStatus> statuses,
                                     Pageable pageable);

    // Soft delete / update status
    @Modifying
    @Transactional
    @Query("UPDATE Training t SET t.trainingDelete = :status WHERE t.id = :id")
    void updateTrainingDelete(@Param("id") Long id, @Param("status") TrainingDeleteStatus status);

    Page<Training> findAllByKoch_AdminStatus(TrainingDeleteStatus status, Pageable pageable);

    List<Training> findAllByTrainingDeleteIn(List<TrainingDeleteStatus> statuses);







}


