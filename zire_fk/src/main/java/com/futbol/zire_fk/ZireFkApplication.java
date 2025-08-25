package com.futbol.zire_fk;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ZireFkApplication {



        public static void main(String[] args) {
            SpringApplication.run(ZireFkApplication.class, args);

            // PasswordEncoder yaradılır
            PasswordEncoder encoder = new BCryptPasswordEncoder();

            // Kodlanacaq şifrə
            String rawPassword = "Canavar91";

            // Şifrəni kodla
            String encodedPassword = encoder.encode(rawPassword);

            // Konsola çıxar
            System.out.println("Encoded password: " + encodedPassword);
        }
    }



