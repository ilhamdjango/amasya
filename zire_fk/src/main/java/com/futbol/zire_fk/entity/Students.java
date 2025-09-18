package com.futbol.zire_fk.entity;


import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Students {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Ad
    private String surname;     // Soyad

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateBorn; // Doğum tarixi

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdAt;

    private String phone;       // Telefon

    @Column(nullable = false)
    private BigDecimal inDebt;

    public BigDecimal getInDebt() { return inDebt; }
    public void setInDebt(BigDecimal inDebt) { this.inDebt = inDebt; }

    @Column(nullable = false)
    private BigDecimal paid = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal  VERIFIED = BigDecimal.ZERO;

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }







    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public BigDecimal getCompleted() {
        return completed;
    }

    public void setCompleted(BigDecimal completed) {
        this.completed = completed;
    }


    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getVERIFIED() {
        return VERIFIED;
    }

    public void setVERIFIED(BigDecimal VERIFIED) {
        this.VERIFIED = VERIFIED;
    }

    public StudentProgressStatus getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(StudentProgressStatus progressStatus) {
        this.progressStatus = progressStatus;
    }

    @Column(nullable = false)
    private BigDecimal completed = BigDecimal.ZERO;





    private Boolean documents;  // Şəxsi sənədlər (true varsa təqdim edilib)


    @Column(name = "activity_rate")
    private BigDecimal activityRate = BigDecimal.ZERO;

    public BigDecimal getActivityRate() {
        return activityRate;
    }

    public void setActivityRate(BigDecimal activityRate) {
        this.activityRate = activityRate;
    }

    // Bir tələbə yalnız bir məşq qrupuna bağlıdır
    @ManyToOne
    @JoinColumn(name = "training_id")
    private Training training;

    public Students() {}



    public StudentStatus getStudentStatus() {
        return studentStatus;
    }

    public void setStudentStatus(StudentStatus studentStatus) {
        this.studentStatus = studentStatus;
    }



    // 🔹 Enum String kimi DB-də saxlanacaq
    @Enumerated(EnumType.STRING)
    @Column(name = "student_status", length = 20, nullable = false)
    private StudentStatus studentStatus = StudentStatus.ACTIVE;


    @Enumerated(EnumType.STRING)
    private StudentProgressStatus progressStatus = StudentProgressStatus.IN_DEBT;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public LocalDate getDateBorn() { return dateBorn; }
    public void setDateBorn(LocalDate dateBorn) { this.dateBorn = dateBorn; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }



    public Boolean getDocuments() { return documents; }
    public void setDocuments(Boolean documents) { this.documents = documents; }



    public Training getTraining() { return training; }
    public void setTraining(Training training) { this.training = training; }
}
