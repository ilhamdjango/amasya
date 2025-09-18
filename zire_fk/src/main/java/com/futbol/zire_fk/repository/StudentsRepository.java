package com.futbol.zire_fk.repository;

import com.futbol.zire_fk.entity.Attendance;
import com.futbol.zire_fk.entity.Students;
import com.futbol.zire_fk.entity.StudentStatus; // ✅ Düzgün enum
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentsRepository extends JpaRepository<Students, Long> {

    // müəyyən bir training-ə bağlı bütün ACTIVE tələbələri gətir
    List<Students> findByTrainingIdAndStudentStatus(Long trainingId, StudentStatus studentStatus);

    // adı ilə axtarış (optional)
    List<Students> findByNameContainingIgnoreCase(String name);

    // Borcu minimum dəyərdən böyük olan tələbələri tapmaq üçün
    List<Students> findByInDebtGreaterThan(BigDecimal minDebt);


    List<Students> findByActivityRateGreaterThan(BigDecimal activityRate);

    Page<Students> findByTrainingIdAndStudentStatus(Long trainingId, StudentStatus status, Pageable pageable);

    Page<Students> findByTrainingIdAndStudentStatusIn(Long trainingId, List<StudentStatus> statuses, Pageable pageable);

    Page<Students> findByTrainingId(Long trainingId, Pageable pageable);

    // StudentStatus = ACTIVE olanların sayını qaytarır
    long countByStudentStatus(StudentStatus studentStatus);

    @Query("SELECT AVG(s.activityRate) FROM Students s WHERE s.studentStatus = com.futbol.zire_fk.entity.StudentStatus.ACTIVE")
    Double findAverageActivityRate();

    @Query("SELECT SUM(s.paid) FROM Students s")
    BigDecimal getTotalPaid();


    @Query("SELECT COALESCE(SUM(s.paid), 0) " +
            "FROM Students s " +
            "WHERE s.training.id = :trainingId " +
            "AND s.studentStatus = :status")
    BigDecimal getTotalPaidByTraining(@Param("trainingId") Long trainingId,
                                      @Param("status") StudentStatus status);



    @Query("SELECT s FROM Students s LEFT JOIN FETCH s.training WHERE s.id = :id")
    Optional<Students> findByIdWithTraining(@Param("id") Long id);

    // Müəllim adı üçün custom query
    @Query("SELECT t.koch.name FROM Training t WHERE t.id = :trainingId")
    String findTeacherNameByTrainingId(@Param("trainingId") Long trainingId);

    // Service dəyişmədən ACTIVE tələbələri default gətirmək üçün helper metod
    default List<Students> findActiveByTrainingId(Long trainingId) {
        return findByTrainingIdAndStudentStatus(trainingId, StudentStatus.ACTIVE);
    }

    default Page<Students> findActiveByTrainingId(Long trainingId, Pageable pageable) {
        return findByTrainingIdAndStudentStatus(trainingId, StudentStatus.ACTIVE, pageable);
    }


    List<Students> findAllByTrainingIdAndStudentStatus(Long trainingId, StudentStatus studentStatus);

    @Query("SELECT SUM(s.completed) FROM Students s")
    BigDecimal sumCompleted();

    List<Students> findAllByStudentStatus(StudentStatus status);

    List<Students> findAllByTrainingId(Long trainingId);

    @Query("SELECT SUM(s.completed) FROM Students s WHERE s.createdAt BETWEEN :start AND :end")
    BigDecimal sumCompletedByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);



    @Query("SELECT SUM(s.inDebt) FROM Students s")
    BigDecimal sumInDebt();

    @Query("SELECT s.name FROM Students s ORDER BY s.inDebt DESC")
    List<String> findTopStudentsByDebt(Pageable pageable);


}
