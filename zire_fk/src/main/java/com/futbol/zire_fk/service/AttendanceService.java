package com.futbol.zire_fk.service;

import com.futbol.zire_fk.entity.Attendance;
import com.futbol.zire_fk.entity.Students;
import com.futbol.zire_fk.entity.Training;
import com.futbol.zire_fk.repository.AttendanceRepository;
import com.futbol.zire_fk.repository.StudentsRepository;
import com.futbol.zire_fk.repository.TrainingRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentsRepository studentsRepository;
    private final TrainingRepository trainingRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentsRepository studentsRepository,TrainingRepository trainingRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentsRepository = studentsRepository;
        this.trainingRepository=trainingRepository;
    }


    @Transactional
    public void markPresentForTraining(Long trainingId) {
        List<Students> students = studentsRepository.findAllByTrainingId(trainingId);
        if (students.isEmpty()) return;

        for (Students student : students) {
            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setAttendance(BigDecimal.ONE);
            attendance.setLessonHeld(BigDecimal.ONE);
            attendanceRepository.save(attendance);

            List<Attendance> last8 = attendanceRepository
                    .findTop12ByStudentIdOrderByCreateTimeDesc(student.getId());

            BigDecimal totalAttendance = BigDecimal.ZERO;
            BigDecimal totalLessonHeld = BigDecimal.ZERO;
            for (Attendance a : last8) {
                totalAttendance = totalAttendance.add(a.getAttendance());
                totalLessonHeld = totalLessonHeld.add(a.getLessonHeld());
            }

            BigDecimal activityRate = BigDecimal.ZERO;
            if (totalLessonHeld.compareTo(BigDecimal.ZERO) > 0) {
                activityRate = totalAttendance.divide(totalLessonHeld, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            student.setActivityRate(activityRate);
            studentsRepository.save(student);
        }

        // Training-in activityRate ortalamasını hesabla
        BigDecimal totalActivity = students.stream()
                .map(Students::getActivityRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal averageActivity = totalActivity.divide(
                BigDecimal.valueOf(students.size()), 2, RoundingMode.HALF_UP
        );

        Training training = students.get(0).getTraining();
        training.setActivePercent(averageActivity);
        trainingRepository.save(training);
    }


    @Transactional
    public void markAbsentForStudent(Long studentId) {
        Optional<Students> studentOpt = studentsRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            return;
        }

        Students student = studentOpt.get();

        // Yeni Attendance əlavə et
        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setAttendance(BigDecimal.ZERO); // gəlməyib
        attendance.setLessonHeld(BigDecimal.ONE);  // dərs keçirilib
        attendanceRepository.save(attendance);

        // Son 8 Attendance qeydlərini götür
        List<Attendance> last8 = attendanceRepository
                .findTop12ByStudentIdOrderByCreateTimeDesc(studentId);

        BigDecimal activityRate = BigDecimal.ZERO;
        if (!last8.isEmpty()) {
            BigDecimal totalAttendance = BigDecimal.ZERO;
            BigDecimal totalLessonHeld = BigDecimal.ZERO;

            for (Attendance a : last8) {
                totalAttendance = totalAttendance.add(a.getAttendance());
                totalLessonHeld = totalLessonHeld.add(a.getLessonHeld());
            }

            if (totalLessonHeld.compareTo(BigDecimal.ZERO) > 0) {
                activityRate = totalAttendance
                        .divide(totalLessonHeld, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
        }

        student.setActivityRate(activityRate);
        studentsRepository.save(student);

        // 🔹 Training üçün ortalama aktivliyi güncəllə
        Training training = student.getTraining();
        if (training != null) {
            List<Students> studentsInTraining = studentsRepository.findAllByTrainingId(training.getId());
            BigDecimal sum = BigDecimal.ZERO;
            int count = studentsInTraining.size();
            for (Students s : studentsInTraining) {
                sum = sum.add(s.getActivityRate() != null ? s.getActivityRate() : BigDecimal.ZERO);
            }
            BigDecimal average = count > 0 ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            training.setActivePercent(average);
            trainingRepository.save(training);
        }
    }

    public List<String> getTop3MostAbsentStudentNames() {
        List<Object[]> top3 = attendanceRepository.findTop3MostAbsent(PageRequest.of(0, 3));

        List<String> names = new ArrayList<>();
        for (Object[] row : top3) {
            Long studentId = (Long) row[0];
            Students student = studentsRepository.findById(studentId)
                    .orElse(null);
            if (student != null) {
                names.add(student.getName()); // Student entity-də ad sütunu
            }
        }
        return names;
    }


}


