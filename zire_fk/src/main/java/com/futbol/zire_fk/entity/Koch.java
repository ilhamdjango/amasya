package com.futbol.zire_fk.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "koch")
@SQLDelete(sql = "UPDATE koch SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class Koch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private boolean active = true; // default aktiv
    private String name;
    private String surname;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate born;
    private String photo;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Yeni enum sahəsi, default ACTIVE
    @Enumerated(EnumType.STRING)
    private TrainingDeleteStatus adminStatus = TrainingDeleteStatus.ACTIVE;

    // Trainings əlaqəsi
    @OneToMany(mappedBy = "koch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Training> trainings = new ArrayList<>();

    // Constructor
    public Koch() {}

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public LocalDate getBorn() { return born; }
    public void setBorn(LocalDate born) { this.born = born; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public TrainingDeleteStatus getAdminStatus() { return adminStatus; }
    public void setAdminStatus(TrainingDeleteStatus adminStatus) { this.adminStatus = adminStatus; }

    public List<Training> getTrainings() { return trainings; }
    public void setTrainings(List<Training> trainings) { this.trainings = trainings; }
}