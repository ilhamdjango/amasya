package com.futbol.zire_fk.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tələbə ilə əlaqə (bir tələbənin çoxlu attendance qeydi ola bilər)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Students student;

    // Keçirilmiş dərs sayı
    @Column(name = "lesson_held", precision = 10, scale = 2)
    private BigDecimal lessonHeld = BigDecimal.ZERO;


    // Tələbənin iştirak etdiyi dərs sayı
    @Column(name = "attendance", precision = 10, scale = 2)
    private BigDecimal attendance= BigDecimal.ZERO;

    // Yaradılma vaxtı
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    // --- Getter & Setter ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Students getStudent() {
        return student;
    }

    public void setStudent(Students student) {
        this.student = student;
    }

    public BigDecimal getLessonHeld() {
        return lessonHeld;
    }

    public void setLessonHeld(BigDecimal lessonHeld) {
        this.lessonHeld = lessonHeld;
    }

    public BigDecimal getAttendance() {
        return attendance;
    }

    public void setAttendance(BigDecimal attendance) {
        this.attendance = attendance;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }
}
