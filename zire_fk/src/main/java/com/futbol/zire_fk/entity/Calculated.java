package com.futbol.zire_fk.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculated")
public class Calculated {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BigDecimal sahə
    @Column(nullable = false)
    private BigDecimal totalCompleted;

    // Tarix və zaman
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Constructor, Getter, Setter
    public Calculated() {
    }

    public Calculated(BigDecimal totalCompleted) {
        this.totalCompleted = totalCompleted;
        this.createdAt = LocalDateTime.now(); // yaradıldığı anın vaxtı
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getTotalCompleted() {
        return totalCompleted;
    }

    public void setTotalCompleted(BigDecimal totalCompleted) {
        this.totalCompleted = totalCompleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
