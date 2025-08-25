package com.futbol.zire_fk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;

@Controller
public class LoginController {

    // Login səhifəsini göstərir və cookie-dən theme oxuyur
    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        String theme = "dark"; // default

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue(); // "dark" / "light"
                }
            }
        }
        model.addAttribute("theme", theme + "-mode"); // body üçün
        return "login";
    }



    // Root URL → login-ə yönləndirir
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    // Toggle üçün cookie yazan endpoint

    @PostMapping("/set-theme")
    @ResponseBody
    public void setTheme(@RequestParam String theme, HttpServletResponse response) {
        Cookie cookie = new Cookie("theme", theme);
        cookie.setMaxAge(60 * 60 * 24 * 365); // 1 il
        cookie.setPath("/"); // bütün sayt üçün
        cookie.setHttpOnly(false); // JS görə bilsin
        response.addCookie(cookie);
    }


}
