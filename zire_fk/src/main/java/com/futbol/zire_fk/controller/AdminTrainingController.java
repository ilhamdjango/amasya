package com.futbol.zire_fk.controller;

import com.futbol.zire_fk.dto.KochDto;
import com.futbol.zire_fk.dto.TrainingDto;
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
import com.futbol.zire_fk.entity.Training;
import com.futbol.zire_fk.service.TrainingService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("admintraining/")
public class AdminTrainingController {

    private final KochService kochService;
    private final TrainingService trainingService;
    public AdminTrainingController(KochService kochService, TrainingService trainingService) {
        this.kochService = kochService;
        this.trainingService = trainingService;
    }
    @GetMapping("/admintraining")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpServletRequest request,
            Principal principal) {
        // Theme cookie oxuma
        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) theme = cookie.getValue();
            }
        }
        model.addAttribute("theme", theme + "-mode");
        int size = (request.getHeader("User-Agent") != null &&
                (request.getHeader("User-Agent").contains("Mobi") ||
                        request.getHeader("User-Agent").contains("Android") ||
                        request.getHeader("User-Agent").contains("iPhone"))) ? 1 : 5;
        // 🔹 Burada artıq bütün aktiv traininglər gəlir
        Page<Training> trainingPage = trainingService.getAllTrainings(PageRequest.of(page, size));
        if (principal != null) {
            var userOpt = kochService.findByUsername(principal.getName());
            userOpt.ifPresent(user -> model.addAttribute("name", user.getName()));
        }
        model.addAttribute("trainings", trainingPage.getContent());
        model.addAttribute("page", trainingPage);
        model.addAttribute("size", size);
        List<Koch> kochs = kochService.findAll();
        model.addAttribute("kochs", kochs);
        return "admintraining/admintraining";
    }

    @PostMapping("/delete/{id}")
    public String deleteTraining(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {
        try {
            trainingService.deletetrById(id); // trainingService-də deleteById metodu olmalıdır
            redirectAttributes.addFlashAttribute("successMessage", "Qrup uğurla silindi!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Qrup silinə bilmədi!");
        }

        // Theme cookie oxuma
        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) theme = cookie.getValue();
            }
        }
        redirectAttributes.addFlashAttribute("theme", theme + "-mode");

        return "redirect:/admintraining/admintraining";
    }

    @GetMapping("/trainingEdit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               Principal principal,
                               HttpServletRequest request) {
        Training training = trainingService.findById(id).orElse(null);
        if (training == null) return "redirect:/training/training";

        // DTO yarat
        TrainingDto trainingDto = new TrainingDto(
                training.getId(),
                training.getName(),
                training.getCreatedDate(),
                training.getAgeRange(),
                training.getMonthlyPayment() != null ? training.getMonthlyPayment().doubleValue() : null
        );

        model.addAttribute("trainingDto", trainingDto);

        // Theme cookie oxuma
        String theme = "dark"; // default
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue();
                    break;
                }
            }
        }
        model.addAttribute("theme", theme + "-mode");

        // Logged-in user adı
        if (principal != null) {
            kochService.findByUsername(principal.getName())
                    .ifPresent(user -> model.addAttribute("name", user.getName()));
        }

        return "training/trainingEdit";
    }



    @PostMapping("/trainingEdit/{id}")
    public String updateTraining(@PathVariable Long id,
                                 @ModelAttribute @Valid TrainingDto trainingDto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request,
                                 Principal principal) {

        if (result.hasErrors()) {
            return "trainingEdit"; // səhifəni yenidən göstər
        }

        trainingService.updateFromDto(id, trainingDto);

        // Theme cookie
        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) theme = cookie.getValue();
            }
        }
        redirectAttributes.addFlashAttribute("theme", theme + "-mode");

        // Logged-in user adı
        if (principal != null) {
            var userOpt = kochService.findByUsername(principal.getName());
            if (userOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("name", userOpt.get().getName());
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Qrup uğurla dəyişdirildi!");
        return "redirect:/training/training";
    }



}
