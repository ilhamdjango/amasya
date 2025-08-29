package com.futbol.zire_fk.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String redirectUrl = "/"; // default

        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String role = auth.getAuthority();
            System.out.println("User role: " + role); // ✅ Burada görəcəksən
            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/koch/kochList";  // ADMIN səhifəsi
                break;
            } else if (role.equals("ROLE_KOCH")) {
                redirectUrl = "/training/training";  // KOCH səhifəsi
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}

