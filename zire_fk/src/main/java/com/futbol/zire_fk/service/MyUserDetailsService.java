package com.futbol.zire_fk.service;

import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.entity.Role;
import com.futbol.zire_fk.entity.TrainingDeleteStatus;
import com.futbol.zire_fk.repository.KochRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final KochRepository kochRepository;

    public MyUserDetailsService(KochRepository kochRepository) {
        this.kochRepository = kochRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1️⃣ ACTIVE statuslu istifadəçi axtarılır
        Optional<Koch> activeUser = kochRepository.findByUsernameAndAdminStatus(username, TrainingDeleteStatus.ACTIVE);

        Koch koch;

        if (activeUser.isPresent()) {
            koch = activeUser.get();
        } else {
            // 2️⃣ ACTIVE tapılmadı, statusdan asılı olmayaraq SUPER_ADMIN yoxlanılır
            koch = kochRepository.findByUsernameAndRole(username, Role.SUPERADMIN)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        // 3️⃣ Rol adı (Spring Security üçün String olmalıdır)
        String role = koch.getRole() != null ? koch.getRole().name() : "USER";

        // 4️⃣ UserDetails qaytarılır
        return User.builder()
                .username(koch.getUsername())
                .password(koch.getPassword()) // BCrypt hash
                .roles(role)
                .build();
    }
}
