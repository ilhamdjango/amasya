package com.futbol.zire_fk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class TrainingDto {

    private Long id;

    @NotBlank(message = "Qrupun adı boş ola bilməz")
    private String name;

    @NotNull(message = "Yaranma tarixi boş ola bilməz")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate born;

    @NotBlank(message = "Yaş aralığı boş ola bilməz")
    private String ageRange;

    @NotNull(message = "Ödəniş məbləği boş ola bilməz")
    @Positive(message = "Ödəniş müsbət olmalıdır")
    private Double monthlyPayment;

    // Constructor
    public TrainingDto() {}

    public TrainingDto(Long id, String name, LocalDate born, String ageRange, Double monthlyPayment) {
        this.id = id;
        this.name = name;
        this.born = born;
        this.ageRange = ageRange;
        this.monthlyPayment = monthlyPayment;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBorn() {
        return born;
    }

    public void setBorn(LocalDate born) {
        this.born = born;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public Double getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(Double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }
}
