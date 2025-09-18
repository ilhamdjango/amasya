package com.futbol.zire_fk.service;

import com.futbol.zire_fk.entity.Students;
import com.futbol.zire_fk.repository.StudentsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class StudentDebtService {

    private final StudentsRepository studentsRepository;

    public StudentDebtService(StudentsRepository studentsRepository) {
        this.studentsRepository = studentsRepository;
    }

    // Hər gün gecə 00:00-da yoxlanacaq
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // hər 24 saatdan bir
    public void increaseDebtMonthly() {
           List<Students> students = studentsRepository.findAll();
         //    int todayDay = java.time.LocalDate.now().getDayOfMonth(); //realda bunu islet
        // int todayDay = LocalDate.now().getDayOfMonth();    //bu daha yaxsi
        int todayDay = LocalDate.of(2025, 9, 13).getDayOfMonth();  //mock etmek ucun
        for (Students student : students) {
            if (student.getTraining() != null && student.getCreatedAt() != null) {
                int createdDay = student.getCreatedAt().getDayOfMonth();

                // yalnız qeydiyyat gününə uyğun olanlar
                if (createdDay == todayDay) {
                    BigDecimal monthlyPayment = student.getTraining().getMonthlyPayment();
                    if (monthlyPayment == null) monthlyPayment = BigDecimal.ZERO;
                    BigDecimal currentDebt = student.getInDebt() == null ? BigDecimal.ZERO : student.getInDebt();
                    student.setInDebt(currentDebt.add(monthlyPayment));
                }
            }
        }

        studentsRepository.saveAll(students);
    }
}

