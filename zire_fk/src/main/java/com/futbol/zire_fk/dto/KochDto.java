package com.futbol.zire_fk.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class KochDto {

    private Long id; // Entity-də var, DTO-da optional

    @NotBlank(message = "İstifadəçi adı boş ola bilməz")
    private String username;

    @NotBlank(message = "Şifrə boş ola bilməz")
    private String password;

    private boolean active = true; // default true

    @NotBlank(message = "Ad boş ola bilməz")
    private String name;

    @NotBlank(message = "Soyad boş ola bilməz")
    private String surname;

    @NotNull(message = "Doğum tarixi boş ola bilməz")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate born;



    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
// Getter və Setter-lər

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


}
