package com.futbol.zire_fk.entity;
import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "koch")
public class Koch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password; // yeni əlavə
    private boolean active; // yeni əlavə, true/false
    private String name;
    private String surname;
    private LocalDate born; // Tarixi LocalDate ilə saxlayır
    private String photo;


    @Enumerated(EnumType.STRING)
    private Role role;

    public Koch(Long id, String username, String password, boolean active, String name, String surname, LocalDate born, String photo, String degree, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.active = active;
        this.name = name;
        this.surname = surname;
        this.born = born;
        this.photo = photo;
        this.role = role;
    }

    // Constructors
    public Koch() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBorn() {
        return born;
    }

    public void setBorn(LocalDate born) {
        this.born = born;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }



    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // Getters & Setters

}
