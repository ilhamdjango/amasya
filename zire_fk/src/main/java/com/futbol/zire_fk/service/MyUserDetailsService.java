package com.futbol.zire_fk.service;

import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.repository.KochRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final KochRepository kochRepository;

    public MyUserDetailsService(KochRepository kochRepository) {
        this.kochRepository = kochRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Koch koch = kochRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String role = koch.getRole() != null ? koch.getRole().name() : "USER"; // Enum üçün .name()
        // əgər Role class-dırsa, .getName() və ya toString() istifadə edə bilərsiniz

        return User.builder()
                .username(koch.getUsername())
                .password(koch.getPassword()) // BCrypt hash
                .roles(role)                   // String olmalıdır
                .build();
    }

}
