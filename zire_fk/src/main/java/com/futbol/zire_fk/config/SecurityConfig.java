package com.futbol.zire_fk.config;

import com.futbol.zire_fk.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final MyUserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomSuccessHandler customSuccessHandler;

    @Autowired
    public SecurityConfig(MyUserDetailsService userDetailsService,
                          BCryptPasswordEncoder passwordEncoder,
                          CustomSuccessHandler customSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.customSuccessHandler = customSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // açıq səhifələr
                        .requestMatchers("/login", "/css/**", "/js/**", "/set-theme").permitAll()

                        // rola görə icazələr
                        .requestMatchers("/koch/**").hasRole("ADMIN")   // ADMIN üçün
                        .requestMatchers("/training/**").hasRole("KOCH") // KOCH üçün

                        // qalan bütün requestlər login tələb edir
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler) // ✅ rola görə yönləndirmə
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                )
                .logout(logout -> logout.permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/set-theme"));

        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }
}
