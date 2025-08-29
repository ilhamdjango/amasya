package com.futbol.zire_fk.controller;

import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.service.KochService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.*;




@Controller
@RequestMapping("training/")
public class TrainingController {
    private final KochService kochService;
    public TrainingController(KochService kochService) {
        this.kochService = kochService;
    }
    @GetMapping("/training")
        public String list(@RequestParam(defaultValue = "0") int page,
        Model model,
        HttpServletRequest request,
        Principal principal) {

            // Theme
            String theme = "dark";
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("theme".equals(cookie.getName())) {
                        theme = cookie.getValue();
                    }
                }
            }
            model.addAttribute("theme", theme + "-mode");

            // Page size cihazdan asılı olaraq təyin olunur
            String userAgent = request.getHeader("User-Agent");
            int size;
            if (userAgent != null && (userAgent.contains("Mobi") || userAgent.contains("Android") || userAgent.contains("iPhone"))) {
                size = 2; // mobil
            } else {
                size = 5; // desktop
            }
            Page<Koch> kochPage = kochService.findAll(PageRequest.of(page, size));
            model.addAttribute("kochs", kochPage.getContent());
            model.addAttribute("page", kochPage);
            model.addAttribute("size", size);

            // İstifadəçi adı əlavə edilir (Optional ilə təhlükəsiz)
            if (principal != null) {
                kochService.findByUsername(principal.getName())
                        .ifPresent(user -> model.addAttribute("name", user.getName())); // name istifadə olunur
            }
        return "training/training"; // Thymeleaf template
    }
}
