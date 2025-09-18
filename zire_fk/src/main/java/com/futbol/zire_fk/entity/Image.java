package com.futbol.zire_fk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String documentName; // ✅ Yeni sahə

    private LocalDateTime uploadDate;

    private String uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Students student;

    // Constructors
    public Image() {}

    public Image(String fileName, String documentName, LocalDateTime uploadDate, String uploadedBy, Students student) {
        this.fileName = fileName;
        this.documentName = documentName;
        this.uploadDate = uploadDate;
        this.uploadedBy = uploadedBy;
        this.student = student;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getDocumentName() { return documentName; } // ✅ getter
    public void setDocumentName(String documentName) { this.documentName = documentName; } // ✅ setter

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public Students getStudent() { return student; }
    public void setStudent(Students student) { this.student = student; }

    // Thymeleaf üçün fayl URL
    @Transient
    public String getFileUrl() {
        return "/uploads/" + this.fileName;
    }
}
