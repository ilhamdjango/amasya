package com.futbol.zire_fk.service;


import com.futbol.zire_fk.entity.StudentStatus;
import com.futbol.zire_fk.entity.Students;
import com.futbol.zire_fk.repository.StudentsRepository;
import com.futbol.zire_fk.entity.Training;
import com.futbol.zire_fk.repository.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.math.BigDecimal;
import java.util.List;

@Service
public class StudentsService {

    private final StudentsRepository studentsRepository;
    private final TrainingRepository trainingRepository;

    @Autowired
    public StudentsService(StudentsRepository studentsRepository,
                           TrainingRepository trainingRepository) {
        this.studentsRepository = studentsRepository;
        this.trainingRepository = trainingRepository;
    }

    public Students saveStudent(Students student) {
        return studentsRepository.save(student);
    }


    // Pageable üçün
    public Page<Students> findStudentsByTrainingId(Long trainingId, Pageable pageable) {
        return studentsRepository.findByTrainingIdAndStudentStatus(trainingId, StudentStatus.ACTIVE, pageable);
    }

    // List üçün
    public List<Students> findStudentsListByTrainingId(Long trainingId) {
        return studentsRepository.findByTrainingIdAndStudentStatus(trainingId, StudentStatus.ACTIVE);
    }




    public String getTrainingNameById(Long trainingId) {
        return trainingRepository.findById(trainingId)
                .map(Training::getName)
                .orElse("Naməlum Qrup");
    }



    public String getTeacherNameByTrainingId(Long trainingId) {
        return studentsRepository.findTeacherNameByTrainingId(trainingId);
    }

    // ✅ Yeni tələbə əlavə edərkən borcu monthlyPayment ilə doldur
    public Students addStudent(Students student) {
        Training training = student.getTraining();
        if (training != null) {
            student.setInDebt(training.getMonthlyPayment());
            BigDecimal debt = student.getInDebt();
        }
        return studentsRepository.save(student);
    }

    // 🔹 Yumşaq silmə (rola görə fərqli status)
    public void softDelete(Long id, Authentication authentication) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tələbə tapılmadı"));

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
            student.setStudentStatus(StudentStatus.CLEANED);
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            student.setStudentStatus(StudentStatus.ARCHIVED);
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_KOCH"))) {
            student.setStudentStatus(StudentStatus.DELETED);
        } else {
            throw new RuntimeException("Silmək üçün icazə yoxdur!");
        }

        studentsRepository.save(student);
    }



    public Students findById(Long id) {
        return studentsRepository.findByIdWithTraining(id)
                .orElse(null);
    }

    public long getActiveStudentCount() {
        return studentsRepository.countByStudentStatus(StudentStatus.ACTIVE);
    }

    public Double getActivePercent() {
        Double avg = studentsRepository.findAverageActivityRate();
        return avg != null ? avg : 0.0;
    }


    public Students save(Students student) {
        return studentsRepository.save(student);
    }

    public Map<String, BigDecimal> getLast3MonthsCompleted() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();

        LocalDate now = LocalDate.now();

        for (int i = 2; i >= 0; i--) { // son 3 ay
            YearMonth ym = YearMonth.from(now).minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            BigDecimal total = studentsRepository.sumCompletedByDateRange(start, end);
            result.put(ym.getMonth().name(), total != null ? total : BigDecimal.ZERO);
        }

        return result;
    }
    public BigDecimal getTotalDebt() {
        // Repository metodunu çağırırıq
        BigDecimal totalDebt = studentsRepository.sumInDebt();
        return totalDebt != null ? totalDebt : BigDecimal.ZERO;
    }
    public List<String> getTop3Debtors() {
        return studentsRepository.findTopStudentsByDebt(PageRequest.of(0, 3));
    }

}
