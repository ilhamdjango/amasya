package com.futbol.zire_fk.repository;

import com.futbol.zire_fk.entity.Attendance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Tələbə dərsdə iştirak edib (+1 attendance, +1 lessonHeld)
    @Modifying
    @Transactional
    @Query("UPDATE Attendance a SET a.attendance = a.attendance + 1, a.lessonHeld = a.lessonHeld + 1 WHERE a.student.id = :studentId")
    void incrementAttendanceAndLesson(@Param("studentId") Long studentId);

    // Attendance -1 edilsin
    @Modifying
    @Transactional
    @Query("UPDATE Attendance a SET a.attendance = a.attendance - 1 WHERE a.student.id = :studentId")
    void decrementAttendance(@Param("studentId") Long studentId);

    Attendance findByStudentId(Long studentId);
    List<Attendance> findTop12ByStudentIdOrderByCreateTimeDesc(Long studentId);

    @Query("SELECT COALESCE(SUM(a.attendance), 0) " +
            "FROM Attendance a " +
            "WHERE a.createTime >= :start AND a.createTime < :end")
    BigDecimal sumAttendanceBetween(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);


    @Query("SELECT a.student.id AS studentId, COUNT(a) AS absentCount " +
            "FROM Attendance a " +
            "WHERE a.attendance = 0 " +
            "GROUP BY a.student.id " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> findTop3MostAbsent(Pageable pageable);



}
