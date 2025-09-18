package com.futbol.zire_fk.dto;

import com.futbol.zire_fk.entity.Role;
import com.futbol.zire_fk.entity.TrainingDeleteStatus;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class KochDto {

    private Long id;

    @NotBlank(message = "İstifadəçi adı boş ola bilməz")
    private String username;

    @NotBlank(message = "Şifrə boş ola bilməz")
    private String password;

    private boolean active = true;

    @NotBlank(message = "Ad boş ola bilməz")
    private String name;

    @NotBlank(message = "Soyad boş ola bilməz")
    private String surname;

    @NotNull(message = "Doğum tarixi boş ola bilməz")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate born;

    @NotNull(message = "Rol boş ola bilməz")
    private Role role;   // 🔥 String yox, Enum oldu


    // 🔹 Status sahəsi əlavə olunur
    private TrainingDeleteStatus status;

    public TrainingDeleteStatus getStatus() {
        return status;
    }

    public void setStatus(TrainingDeleteStatus status) {
        this.status = status;
    }

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

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
