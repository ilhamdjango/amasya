package com.futbol.zire_fk.controller;

import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.service.KochService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.futbol.zire_fk.dto.KochDto;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;
import org.springframework.web.bind.annotation.PathVariable;
import com.futbol.zire_fk.entity.Role;




@Controller
@RequestMapping("koch/")
public class KochController {
    private final KochService kochService;
    public KochController(KochService kochService) {
        this.kochService = kochService;
    }
    @GetMapping("/kochList")
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

        return "koch/kochList";
    }

    @PostMapping("/kochEdit/{id}")
    public String updateKoch(@PathVariable Long id, @ModelAttribute KochDto kochDto, RedirectAttributes redirectAttributes) {
        Optional<Koch> kochOpt = kochService.findById(id);
        if (kochOpt.isPresent()) {
            Koch koch = kochOpt.get();

            // Əgər son admindirsə və rol dəyişirsə
            if (koch.getRole() == Role.ADMIN &&
                    !Role.valueOf(kochDto.getRole().toUpperCase()).equals(Role.ADMIN) &&
                    kochService.countByRole(Role.ADMIN) <= 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "Sistemdə ən azı bir aktiv admin qalmalıdır!");
                return "redirect:/koch/kochList";
            }

            // Digər sahələri güncəllə
            koch.setName(kochDto.getName());
            koch.setSurname(kochDto.getSurname());
            koch.setUsername(kochDto.getUsername());
            if (kochDto.getRole() != null) {
                koch.setRole(Role.valueOf(kochDto.getRole().toUpperCase()));
            }
            koch.setBorn(kochDto.getBorn());
            kochService.save(koch);

            redirectAttributes.addFlashAttribute("successMessage", "İstifadəçi uğurla dəyişdirildi!");
        }
        return "redirect:/koch/kochList";
    }



    // Formu göstərmək üçün
    @GetMapping("/kochAdd")
    public String showForm(Model model, HttpServletRequest request, Principal principal) {
        model.addAttribute("kochDto", new KochDto());

        // Theme cookie-dən oxumaq
        String theme = "dark"; // default
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue(); // dark / light
                }
            }
        }
        model.addAttribute("theme", theme + "-mode"); // body üçün
        // İstifadəçi adı əlavə et (login olmuşdursa)
        if (principal != null) {
            kochService.findByUsername(principal.getName())
                    .ifPresent(user -> model.addAttribute("name", user.getName()));
        }

        return "koch/kochAdd"; // Thymeleaf form səhifəsi
    }

    // Form submit (save)
    @PostMapping("/kochAdd")
    public String addKoch(KochDto kochDto) {
        kochService.saveFromDto(kochDto);
        return "redirect:/koch/kochList"; // save sonrası list səhifəsinə yönləndir
    }



    @GetMapping("/kochEdit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpServletRequest request,
                           Principal principal) {
        // Koch obyektini tapmaq
        Koch koch = kochService.findById(id)
                .orElseThrow(() -> new RuntimeException("Koch tapılmadı: " + id));
        model.addAttribute("koch", koch);
        // Theme cookie-dən oxumaq
        String theme = "dark"; // default
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue(); // dark / light
                }
            }
        }
        model.addAttribute("theme", theme + "-mode"); // body üçün
        // İstifadəçi adı əlavə et (login olmuşdursa)
        if (principal != null) {
            kochService.findByUsername(principal.getName())
                    .ifPresent(user -> model.addAttribute("name", user.getName()));
        }
        return "koch/kochEdit";
    }




    @PostMapping("/delete/{id}")
    public String deleteKoch(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            kochService.deleteById(id); // Service-də soft delete
            redirectAttributes.addFlashAttribute("successMessage", "İstifadəçi uğurla silindi!");
        } catch (EmptyResultDataAccessException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "İstifadəçi tapılmadı!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage()); // Servisdən gələn konkret mesaj
        }
        return "redirect:/koch/kochList";
    }



}
