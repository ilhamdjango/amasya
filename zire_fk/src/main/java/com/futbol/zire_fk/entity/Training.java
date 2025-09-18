package com.futbol.zire_fk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import com.futbol.zire_fk.entity.StudentStatus; // sizin enum yeri
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "training")
public class Training {





    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;            // Qrupun adı
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate createdDate;  // Yaranma tarixi
    private String ageRange;        // Yaş aralığı
    private Integer childCount = 0;     // Uşaq sayı (default 0)
    private Integer breakCount = 0;     // Fasilə edənlər (default 0)
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPayment = BigDecimal.ZERO; // Tam ödəniş məbləği (default 0.00)
    @Column(precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;   // Ödənilmiş məbləğ (default 0.00)
    @Column(precision = 10, scale = 2)
    private BigDecimal unpaidAmount = BigDecimal.ZERO; // Ödənilməmiş məbləğ (default 0.00)
    private Integer debtCount = 0;      // Borc nəfərlə (default 0)
    @Min(0)
    @Max(100)
    @Column(precision = 5, scale = 2)
    private BigDecimal activePercent = BigDecimal.ZERO;




    @Enumerated(EnumType.STRING) // və ya ORDINAL
    private TrainingDeleteStatus trainingDelete = TrainingDeleteStatus.ACTIVE;

    public TrainingDeleteStatus getTrainingDelete() {
        return trainingDelete;
    }

    public void setTrainingDelete(TrainingDeleteStatus trainingDelete) {
        this.trainingDelete = trainingDelete;
    }

    @Column(precision = 10, scale = 2)
    private BigDecimal monthlyPayment = BigDecimal.ZERO; // Aylıq ödəniş

    @ManyToOne
    @JoinColumn(name = "koch_id")
    private Koch koch;

    public Training() {}

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDate.now();
    }

    @OneToMany(mappedBy = "training", fetch = FetchType.LAZY)
    private List<Students> students;

    // Getter
    public List<Students> getStudents() {
        return students;
    }

    // Setter
    public void setStudents(List<Students> students) {
        this.students = students;
    }



    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getCreatedDate() { return createdDate; }
    // Setter optional, çünki updatable=false olsa da, lazım olsa əlavə edə bilərsiniz
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public String getAgeRange() { return ageRange; }
    public void setAgeRange(String ageRange) { this.ageRange = ageRange; }

    public Integer getChildCount() { return childCount; }
    public void setChildCount(Integer childCount) { this.childCount = childCount; }

    public Integer getBreakCount() { return breakCount; }
    public void setBreakCount(Integer breakCount) { this.breakCount = breakCount; }

    public BigDecimal getTotalPayment() { return totalPayment; }
    public void setTotalPayment(BigDecimal totalPayment) { this.totalPayment = totalPayment; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public BigDecimal getUnpaidAmount() { return unpaidAmount; }
    public void setUnpaidAmount(BigDecimal unpaidAmount) { this.unpaidAmount = unpaidAmount; }

    public Integer getDebtCount() { return debtCount; }
    public void setDebtCount(Integer debtCount) { this.debtCount = debtCount; }

    public BigDecimal getActivePercent() {
        return activePercent;
    }

    public void setActivePercent(BigDecimal activePercent) {
        this.activePercent = activePercent;
    }

    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }

    public Koch getKoch() { return koch; }
    public void setKoch(Koch koch) { this.koch = koch; }
}
