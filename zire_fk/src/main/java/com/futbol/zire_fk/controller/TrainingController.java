package com.futbol.zire_fk.controller;

import com.futbol.zire_fk.dto.KochDto;
import com.futbol.zire_fk.dto.TrainingDto;
import com.futbol.zire_fk.entity.*;
import com.futbol.zire_fk.repository.StudentsRepository;
import com.futbol.zire_fk.repository.TrainingRepository;
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

import java.math.BigDecimal;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;
import com.futbol.zire_fk.service.TrainingService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("training/")
public class TrainingController {

    private final KochService kochService;
    private final TrainingService trainingService;
    private final StudentsRepository studentsRepository;
    private final TrainingRepository trainingRepository;


    public TrainingController(KochService kochService, TrainingService trainingService,StudentsRepository studentsRepository,TrainingRepository trainingRepository) {
        this.kochService = kochService;
        this.trainingService = trainingService;
        this.studentsRepository = studentsRepository;
        this.trainingRepository = trainingRepository;
    }

    // ================= Helper metodlar =================

    // Aktiv tələbə sayını hesabla
    private Map<Long, Integer> getActiveStudentCounts(List<Training> trainings) {
        Map<Long, Integer> map = new HashMap<>();
        for (Training t : trainings) {
            int activeCount = (int) t.getStudents().stream()
                    .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                    .count();
            map.put(t.getId(), activeCount);
        }
        return map;
    }

    // Ödənilmiş məbləği hesabla (Student.paid sahəsindən)
    private Map<Long, BigDecimal> getTotalPayments(List<Training> trainings) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Training t : trainings) {
            BigDecimal paidAmount = t.getStudents().stream()
                    .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                    .map(s -> s.getPaid() != null ? s.getPaid() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            map.put(t.getId(), paidAmount);
        }
        return map;
    }

    // Borcun cəmini hesabla
    private Map<Long, BigDecimal> getTotalDebts(List<Training> trainings) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Training t : trainings) {
            BigDecimal debt = t.getStudents().stream()
                    .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                    .map(s -> s.getInDebt() != null ? s.getInDebt() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            map.put(t.getId(), debt);
        }
        return map;
    }


    // Borclu tələbələrin sayını hesabla
    private Map<Long, Integer> getDebtCounts(List<Training> trainings) {
        Map<Long, Integer> map = new HashMap<>();
        for (Training t : trainings) {
            int debtCount = (int) t.getStudents().stream()
                    .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                    .filter(s -> s.getInDebt() != null && s.getInDebt().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            map.put(t.getId(), debtCount);
        }
        return map;
    }





    @GetMapping("/training")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            Model model,
            HttpServletRequest request,
            Principal principal) {

        // Theme cookie oxuma
        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue();
                    break;
                }
            }
        }
        model.addAttribute("theme", theme + "-mode");

        int size = 15; // səhifədə neçə training olacaq

        if (principal != null) {
            String username = principal.getName();
            kochService.findByUsername(username)
                    .ifPresent(koch -> {

                        // 1. Bütün datanı çək (yalnız həmin kocha aid)
                        List<Training> allTrainings = trainingService.getActiveTrainingsByKoch(koch.getId());

                        // 2. Lazımi xəritələri hesabla
                        Map<Long, Integer> activeStudentCounts = getActiveStudentCounts(allTrainings);
                        Map<Long, BigDecimal> totalPaymentsMap = getTotalPayments(allTrainings);
                        Map<Long, BigDecimal> totalDebts = getTotalDebts(allTrainings);
                        Map<Long, Integer> debtCounts = getDebtCounts(allTrainings);


                        // 3. Sort
                        Comparator<Training> comparator;
                        switch (sort) {
                            case "activeStudentCounts":
                                comparator = Comparator.comparing(
                                        (Training t) -> activeStudentCounts.get(t.getId())
                                );
                                break;
                            case "totalDebts":
                                comparator = Comparator.comparing(
                                        (Training t) -> totalDebts.get(t.getId())
                                );
                                break;
                            case "debtCounts":
                                comparator = Comparator.comparing(
                                        (Training t) -> debtCounts.get(t.getId())
                                );
                                break;
                            case "totalPaymentsMap":
                            default:
                                comparator = Comparator.comparing(
                                        (Training t) -> totalPaymentsMap.get(t.getId())
                                );
                                break;
                        }
                        if ("desc".equalsIgnoreCase(order)) {
                            comparator = comparator.reversed();
                        }
                        allTrainings.sort(comparator);

                        // 4. Pagination (slice)
                        int start = page * size;
                        int end = Math.min(start + size, allTrainings.size());
                        List<Training> trainingsPage = allTrainings.subList(start, end);

                        // 5. Footer üçün yekunlar (hamısından)
                        long totalActiveStudents = allTrainings.stream()
                                .flatMap(t -> t.getStudents().stream())
                                .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                                .count();

                        BigDecimal totalDebtSum = allTrainings.stream()
                                .flatMap(t -> t.getStudents().stream())
                                .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                                .map(s -> s.getInDebt() != null ? s.getInDebt() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        long totalInDebtCount = allTrainings.stream()
                                .flatMap(t -> t.getStudents().stream())
                                .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE
                                        && s.getInDebt() != null
                                        && s.getInDebt().compareTo(BigDecimal.ZERO) > 0)
                                .count();

                        BigDecimal totalPaymentsAll = allTrainings.stream()
                                .map(t -> studentsRepository.getTotalPaidByTraining(t.getId(), StudentStatus.ACTIVE))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);


                        // 2️⃣ Map yarat: training.id -> activePercent
                        Map<Long, BigDecimal> activePercentMap = allTrainings.stream()
                                .collect(Collectors.toMap(
                                        Training::getId,
                                        Training::getActivePercent
                                ));

                        // 6. Model
                        model.addAttribute("name", koch.getName());
                        model.addAttribute("trainings", trainingsPage);
                        model.addAttribute("activeStudentCounts", activeStudentCounts);
                        model.addAttribute("totalPaymentsMap", totalPaymentsMap);
                        model.addAttribute("totalDebts", totalDebts);
                        model.addAttribute("debtCounts", debtCounts);
                        model.addAttribute("pageNumber", page);  // mövcud səhifə
                        model.addAttribute("sort", sort);
                        model.addAttribute("order", order);
                        model.addAttribute("activePercent", activePercentMap);

                        model.addAttribute("totalActiveStudents", totalActiveStudents);
                        model.addAttribute("totalDebtSum", totalDebtSum);
                        model.addAttribute("totalInDebtCount", totalInDebtCount);
                        model.addAttribute("totalPaymentsAll", totalPaymentsAll);

                        // Pagination məlumatları
                        int totalPages = (int) Math.ceil((double) allTrainings.size() / size);
                        if(totalPages == 0) totalPages = 1;  // minimum 1 page
                        model.addAttribute("currentPage", page);
                        model.addAttribute("totalPages", totalPages);
                        model.addAttribute("size", size);
                        model.addAttribute("baseUrl", "/training/training");

                    });
        } else {
            model.addAttribute("errorMessage", "İstifadəçi login olmayıb!");
        }

        return "training/training";
    }


    @GetMapping("/trainingAdd")
    public String showAddForm(Model model, HttpServletRequest request, Principal principal) {
        model.addAttribute("trainingDto", new TrainingDto()); // ✅ Əlavə et

        if (principal != null) {
            kochService.findByUsernameAndStatus(principal.getName(), TrainingDeleteStatus.ACTIVE)
                    .ifPresent(user -> model.addAttribute("name", user.getName()));
        }


        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) theme = cookie.getValue();
            }
        }
        model.addAttribute("theme", theme + "-mode");

        return "training/trainingAdd";
    }


    @PostMapping("/trainingAdd")
    public String addTraining(@Valid @ModelAttribute("trainingDto") TrainingDto trainingDto,
                              BindingResult result,
                              Principal principal,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request,
                              Model model) {

        if (result.hasErrors()) {
            model.addAttribute("trainingDto", trainingDto);
            return "training/trainingAdd";
        }

        // Login olmuş koch-u tapırıq
        Koch koch = null;
        if (principal != null) {
            koch = kochService.findByUsernameAndStatus(principal.getName(), TrainingDeleteStatus.ACTIVE)
                    .orElse(null);
        }

        trainingService.saveFromDto(trainingDto, koch);

        // Cookie və success mesaj
        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) theme = cookie.getValue();
            }
        }
        redirectAttributes.addFlashAttribute("theme", theme + "-mode");
        redirectAttributes.addFlashAttribute("successMessage", "Qrup uğurla əlavə edildi!");

        return "redirect:/training/training";
    }




    @PostMapping("/delete/{id}")
    public String deleteTraining(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {
        try {
            trainingService.deleteById(id); // trainingService-də deleteById metodu olmalıdır
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

        return "redirect:/training/training";
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
            kochService.findByUsernameAndStatus(principal.getName(), TrainingDeleteStatus.ACTIVE)
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
            kochService.findByUsernameAndStatus(principal.getName(), TrainingDeleteStatus.ACTIVE)
                    .ifPresent(user -> redirectAttributes.addFlashAttribute("name", user.getName()));
        }

        redirectAttributes.addFlashAttribute("successMessage", "Qrup uğurla dəyişdirildi!");
        return "redirect:/training/training";
    }



}
