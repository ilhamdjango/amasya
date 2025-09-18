package com.futbol.zire_fk.dto;

import com.futbol.zire_fk.entity.StudentStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentsDto {
    private String name;
    private String surname;
    private String phone;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdAt;

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateBorn;

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private StudentStatus studentStatus;

    public StudentStatus getStudentStatus() {
        return studentStatus;
    }

    public void setStudentStatus(StudentStatus studentStatus) {
        this.studentStatus = studentStatus;
    }

    public Boolean getDocuments() {
        return documents;
    }

    public void setDocuments(Boolean documents) {
        this.documents = documents;
    }

    private Boolean documents; // 🔹 ə
    private Long trainingId;

    // Getter və Setter-lər
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDateBorn() { return dateBorn; }
    public void setDateBorn(LocalDate dateBorn) { this.dateBorn = dateBorn; }

    public Long getTrainingId() { return trainingId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }
}
