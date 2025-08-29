package com.futbol.zire_fk.service;

import com.futbol.zire_fk.dto.KochDto;
import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.entity.Role;
import com.futbol.zire_fk.repository.KochRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.EmptyResultDataAccessException;
import java.util.Optional;
import java.util.List;



@Service
public class KochService {

    private final KochRepository kochRepository;
    private final PasswordEncoder passwordEncoder;

    public KochService(KochRepository kochRepository, PasswordEncoder passwordEncoder) {
        this.kochRepository = kochRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Bütün aktiv istifadəçiləri gətirir
    public List<Koch> findAll() {
        return kochRepository.findAll(); // @Where sayəsində yalnız active=true gəlir
    }

    // Pagination ilə
    public Page<Koch> findAll(Pageable pageable) {
        return kochRepository.findAll(pageable);
    }

    // İstifadəçi sayını qaytarır (aktiv olanlar)
    public long count() {
        return kochRepository.count();
    }

    // ID ilə tapmaq
    public Optional<Koch> findById(Long id) {
        return kochRepository.findById(id);
    }

    // Username ilə tapmaq
    public Optional<Koch> findByUsername(String username) {
        return kochRepository.findByUsername(username);
    }

    // Yeni istifadəçi əlavə etmək
    public Koch save(Koch koch) {
        return kochRepository.save(koch);
    }

    // DTO ilə əlavə etmək
    public void saveFromDto(KochDto kochDto) {
        Koch koch = new Koch();
        koch.setName(kochDto.getName());
        koch.setSurname(kochDto.getSurname());
        koch.setUsername(kochDto.getUsername());
        koch.setPassword(passwordEncoder.encode(kochDto.getPassword()));
        if (kochDto.getRole() != null) {
            koch.setRole(Role.valueOf(kochDto.getRole().toUpperCase()));
        }
        koch.setBorn(kochDto.getBorn());
        koch.setActive(kochDto.isActive());

        kochRepository.save(koch);
    }

    // Aktiv istifadəçilərin sayını rola görə qaytarır
    public long countByRole(Role role) {
        return kochRepository.countByRoleAndActiveTrue(role);
    }

    // Soft delete (fiziki silmir, sadəcə active=false)
    public void deleteById(Long id) {
        Optional<Koch> kochOpt = kochRepository.findById(id);
        if (kochOpt.isPresent()) {
            Koch koch = kochOpt.get();

            if (koch.getRole() == Role.ADMIN) {
                long activeAdmins = kochRepository.countByRoleAndActiveTrue(Role.ADMIN);
                if (activeAdmins <= 1) {
                    throw new IllegalArgumentException("Sistemdə ən azı bir aktiv admin qalmalıdır!");
                }
            }

            kochRepository.deactivateById(id); // Normal deactivate
        } else {
            throw new EmptyResultDataAccessException(1);
        }
    }

    // Login yoxlaması
    public boolean login(String username, String password) {
        Optional<Koch> kochOpt = kochRepository.findByUsername(username);
        if (kochOpt.isPresent()) {
            Koch koch = kochOpt.get();
            // aktivdir və password uyğun gəlirsə
            if (koch.isActive() && passwordEncoder.matches(password, koch.getPassword())) {
                return true;
            }
        }
        return false;
    }
}
