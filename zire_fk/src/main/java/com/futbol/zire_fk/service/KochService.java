package com.futbol.zire_fk.service;

import com.futbol.zire_fk.dto.KochDto;
import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.entity.Role;
import com.futbol.zire_fk.entity.StudentStatus;
import com.futbol.zire_fk.repository.KochRepository;
import com.futbol.zire_fk.entity.TrainingDeleteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KochService {

    private final KochRepository kochRepository;
    private final PasswordEncoder passwordEncoder;

    public KochService(KochRepository kochRepository, PasswordEncoder passwordEncoder) {
        this.kochRepository = kochRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🔹 Login + rol yoxlanışı
    public Koch loginAndGetUser(String username, String password) {
        Optional<Koch> kochOpt = kochRepository.findByUsernameAndAdminStatus(username, TrainingDeleteStatus.ACTIVE);
        if (kochOpt.isPresent()) {
            Koch koch = kochOpt.get();
            if (koch.isActive() && passwordEncoder.matches(password, koch.getPassword())) {
                return koch; // login uğurlu, Koch obyektini qaytarırıq
            }
        }
        return null; // login uğursuz
    }

    // 🔹 Cari istifadəçinin görə biləcəyi Koch listi
    public Page<Koch> getKochListForUser(Koch currentUser, Pageable pageable) {
        if (currentUser == null) return Page.empty();

        if (currentUser.getRole() == Role.SUPERADMIN) {
            // SUPERADMIN: ACTIVE + DELETED bütün istifadəçilər
            List<Koch> list = kochRepository.findByAdminStatusIn(
                    List.of(TrainingDeleteStatus.ACTIVE, TrainingDeleteStatus.DELETED)
            );
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), list.size());
            return new PageImpl<>(list.subList(start, end), pageable, list.size());
        } else {
            // ADMIN və digərləri: yalnız ACTIVE istifadəçilər
            return kochRepository.findByAdminStatus(TrainingDeleteStatus.ACTIVE, pageable);
        }
    }



    // 🔹 Sadəcə ACTIVE istifadəçilər (Page ilə)
    public Page<Koch> findAll(Pageable pageable) {
        return kochRepository.findByAdminStatus(TrainingDeleteStatus.ACTIVE, pageable);
    }

    // 🔹 Super admin üçün ACTIVE + DELETED (Page ilə)
    public Page<Koch> findAllForSuperAdmin(Pageable pageable) {
        List<Koch> list = kochRepository.findByAdminStatusIn(
                List.of(TrainingDeleteStatus.ACTIVE, TrainingDeleteStatus.DELETED)
        );
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<Koch> sublist = list.subList(start, end);
        return new PageImpl<>(sublist, pageable, list.size());
    }

    // 🔹 Cari istifadəçinin görə biləcəyi Koch listi
    public Page<Koch> getKochListForUser(java.security.Principal principal, Pageable pageable) {
        if (principal == null) return Page.empty();

        Optional<Koch> userOpt = findByUsername(principal.getName());
        if (userOpt.isEmpty()) return Page.empty();

        Koch user = userOpt.get();

        // Rol yoxlaması
        if (user.getRole() == Role.SUPERADMIN) {
            return findAllForSuperAdmin(pageable); // ACTIVE + DELETED
        } else {
            return findAll(pageable); // Yalnız ACTIVE
        }
    }



    // 🔹 ID ilə tapmaq
    public Optional<Koch> findById(Long id) {
        return kochRepository.findById(id);
    }

    // 🔹 İstifadəçi adı ilə tapmaq
    public Optional<Koch> findByUsername(String username) {
        return kochRepository.findByUsername(username);
    }




    public List<Koch> findAllActive() {
        return kochRepository.findByAdminStatus(TrainingDeleteStatus.ACTIVE);
    }

    // 🔹 İstifadəçi adı + status ilə tapmaq
    public Optional<Koch> findByUsernameAndStatus(String username, TrainingDeleteStatus status) {
        return kochRepository.findByUsernameAndAdminStatus(username, status);
    }

    // 🔹 Yeni istifadəçi əlavə etmək
    public Koch save(Koch koch) {
        return kochRepository.save(koch);
    }

    // 🔹 DTO ilə əlavə etmək / redaktə etmək
    public Koch saveFromDto(KochDto kochDto) {
        Koch koch;
        if (kochDto.getId() != null) {
            koch = kochRepository.findById(kochDto.getId())
                    .orElseThrow(() -> new RuntimeException("Koch tapılmadı"));
        } else {
            koch = new Koch();
            koch.setActive(true);
            koch.setAdminStatus(TrainingDeleteStatus.ACTIVE);
        }

        koch.setName(kochDto.getName());
        koch.setSurname(kochDto.getSurname());
        koch.setUsername(kochDto.getUsername());

        if (kochDto.getPassword() != null && !kochDto.getPassword().isBlank()) {
            koch.setPassword(passwordEncoder.encode(kochDto.getPassword()));
        }

        if (kochDto.getRole() != null) {
            koch.setRole(kochDto.getRole());
        }

        koch.setBorn(kochDto.getBorn());
        koch.setActive(kochDto.isActive());

        return kochRepository.save(koch);
    }

    // 🔹 Aktiv istifadəçilərin sayını rola görə
    public long countByRole(Role role) {
        return kochRepository.countByRoleAndAdminStatus(role, TrainingDeleteStatus.ACTIVE);
    }

    // 🔹 Soft delete
    public void deleteById(Long id) {
        Koch koch = kochRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Koch tapılmadı"));

        // Sonuncu SuperAdmin yoxlaması
        if (koch.getRole() == Role.SUPERADMIN) {
            long activeSuperAdmins = kochRepository.countByRoleAndAdminStatus(Role.SUPERADMIN, TrainingDeleteStatus.ACTIVE);
            if (activeSuperAdmins <= 1) {
                throw new IllegalArgumentException("Sonuncu SuperAdmin-i silmək olmaz!");
            }
        }

        // Əgər ADMIN roludursa, onu da ayrıca yoxlaya bilərsən
        if (koch.getRole() == Role.ADMIN) {
            long activeAdmins = kochRepository.countByRoleAndAdminStatus(Role.ADMIN, TrainingDeleteStatus.ACTIVE);
            if (activeAdmins <= 1) {
                throw new IllegalArgumentException("Sistemdə ən azı bir aktiv admin qalmalıdır!");
            }
        }

        koch.setAdminStatus(TrainingDeleteStatus.DELETED);
        kochRepository.save(koch);
    }


    // 🔹 Login yoxlaması
    public boolean login(String username, String password) {
        Optional<Koch> kochOpt = kochRepository.findByUsernameAndAdminStatus(username, TrainingDeleteStatus.ACTIVE);
        if (kochOpt.isPresent()) {
            Koch koch = kochOpt.get();
            return koch.isActive()
                    && koch.getAdminStatus() == TrainingDeleteStatus.ACTIVE
                    && passwordEncoder.matches(password, koch.getPassword());
        }
        return false;
    }

    public long getTeacherCount() {
        // role = Role.KOCH, adminStatus = TrainingDeleteStatus.ACTIVE
        return kochRepository.countByRoleAndAdminStatus(Role.KOCH, TrainingDeleteStatus.ACTIVE);
    }



}
