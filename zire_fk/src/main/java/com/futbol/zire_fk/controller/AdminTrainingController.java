package com.futbol.zire_fk.controller;


import com.futbol.zire_fk.dto.TrainingDto;
import com.futbol.zire_fk.entity.*;
import com.futbol.zire_fk.repository.CalculatedRepository;
import com.futbol.zire_fk.repository.StudentsRepository;
import com.futbol.zire_fk.service.KochService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final StudentsRepository studentsRepository;
    private final CalculatedRepository calculatedRepository;
    public AdminTrainingController(KochService kochService, TrainingService trainingService,StudentsRepository studentsRepository,CalculatedRepository calculatedRepository) {
        this.kochService = kochService;
        this.trainingService = trainingService;
        this.studentsRepository=studentsRepository;
        this.calculatedRepository = calculatedRepository;

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

    // Ödənilmiş məbləği hesabla (Student.paid və superadmin üçün VERIFIED ilə birlikdə)
    private Map<Long, BigDecimal>getTotalPayments(List<Training> trainings, boolean isSuperAdmin) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Training t : trainings) {
            BigDecimal paidAmount = t.getStudents().stream()
                    .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                    .map(s -> {
                        if (isSuperAdmin) {
                            // Superadmin → paid + VERIFIED
                            return (s.getPaid() != null ? s.getPaid() : BigDecimal.ZERO)
                                    .add(s.getVERIFIED() != null ? s.getVERIFIED() : BigDecimal.ZERO);
                        } else {
                            // Admin → yalnız paid
                            return s.getPaid() != null ? s.getPaid() : BigDecimal.ZERO;
                        }
                    })
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

    @GetMapping("/admintraining")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpServletRequest request) {

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

        // 1. Bütün training-ləri çək (bütün koch-lar)
        List<Training> allTrainings = trainingService.getAllActive();
        List<Training> allTrainingss = trainingService.getAllActivess();


        // 2. Lazımi xəritələri hesabla
        Map<Long, Integer> activeStudentCounts = getActiveStudentCounts(allTrainings);
        boolean isSuperAdmin = request.isUserInRole("SUPERADMIN");
        Map<Long, BigDecimal> totalPaymentsMap = getTotalPayments(allTrainings, isSuperAdmin);
        Map<Long, BigDecimal> totalDebts = getTotalDebts(allTrainings);
        Map<Long, Integer> debtCounts = getDebtCounts(allTrainings);

        // 3. Sort
        Comparator<Training> comparator;
        switch (sort) {
            case "activeStudentCounts":
                comparator = Comparator.comparing(t -> activeStudentCounts.get(t.getId()));
                break;
            case "totalDebts":
                comparator = Comparator.comparing(t -> totalDebts.get(t.getId()));
                break;
            case "debtCounts":
                comparator = Comparator.comparing(t -> debtCounts.get(t.getId()));
                break;
            case "id":
                comparator = Comparator.comparing(Training::getId);
                break;
            case "totalPaymentsMap":
            default:
                comparator = Comparator.comparing(t -> totalPaymentsMap.get(t.getId()));
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
        long totalActiveStudents = allTrainingss.stream()
                .flatMap(t -> t.getStudents().stream())
                .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                .count();

        BigDecimal totalDebtSum = allTrainingss.stream()
                .flatMap(t -> t.getStudents().stream())
                .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                .map(s -> s.getInDebt() != null ? s.getInDebt() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalInDebtCount = allTrainingss.stream()
                .flatMap(t -> t.getStudents().stream())
                .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE
                        && s.getInDebt() != null
                        && s.getInDebt().compareTo(BigDecimal.ZERO) > 0)
                .count();





        BigDecimal totalPaymentsAll = totalPaymentsMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalPaymentsAll", totalPaymentsAll);

        //  Completed cəmi
        BigDecimal totalCompleted = studentsRepository.sumCompleted();
        if (totalCompleted == null) totalCompleted = BigDecimal.ZERO;



        // 6. Model
        model.addAttribute("trainings", trainingsPage);
        model.addAttribute("activeStudentCounts", activeStudentCounts);
        model.addAttribute("totalPaymentsMap", totalPaymentsMap);
        model.addAttribute("totalDebts", totalDebts);
        model.addAttribute("debtCounts", debtCounts);
        model.addAttribute("pageNumber", page);  // mövcud səhifə
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("size", size);

        model.addAttribute("totalActiveStudents", totalActiveStudents);
        model.addAttribute("totalDebtSum", totalDebtSum);
        model.addAttribute("totalInDebtCount", totalInDebtCount);
        model.addAttribute("totalPaymentsAll", totalPaymentsAll);

        // Pagination məlumatları
        int totalPages = (int) Math.ceil((double) allTrainings.size() / size);
        if(totalPages == 0) totalPages = 1;  // boş olsa da 1 səhifə göstər
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("baseUrl", "/admintraining/admintraining");
        model.addAttribute("totalCompleted", totalCompleted); // ✅ burda

        return "admintraining/admintraining";
    }





    @PostMapping("/delete/{id}")
    public String deleteTraining(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {
        try {
            trainingService.deleteByIdforAdmin(id);//trainingService-də deleteById metodu olmalıdır
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




    @GetMapping("/admintrainingEdit/{id}")
    public String showEditForm(@PathVariable Long id,
                               Model model,
                               Principal principal,
                               HttpServletRequest request) {
        Training training = trainingService.findById(id).orElse(null);
        if (training == null) return "redirect:/admintraining/admintraining";

        // DTO yarat
        TrainingDto trainingDto = new TrainingDto(
                training.getId(),
                training.getName(),
                training.getCreatedDate(),
                training.getAgeRange(),
                training.getMonthlyPayment() != null ? training.getMonthlyPayment().doubleValue() : null
        );

// ✅ Statusu set et
        trainingDto.setStatus(training.getTrainingDelete());

        model.addAttribute("trainingDto", trainingDto);
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


        if (principal != null) {
            kochService.findByUsername(principal.getName())
                    .ifPresent(user -> {
                        model.addAttribute("name", user.getName());
                        model.addAttribute("userRole", user.getRole().name()); // rol əlavə et

                    });

        }





        return "admintraining/admintrainingEdit";
    }


    @PostMapping("/admintrainingEdit/{id}")
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

        // Logged-in user adı (yalnız ACTIVE statuslu)
        if (principal != null) {
            kochService.findByUsernameAndStatus(principal.getName(), TrainingDeleteStatus.ACTIVE)
                    .ifPresent(user -> redirectAttributes.addFlashAttribute("name", user.getName()));
        }

        redirectAttributes.addFlashAttribute("successMessage", "Qrup uğurla dəyişdirildi!");
        return "redirect:/admintraining/admintraining";
    }

    @PostMapping("/admintrainingVerified/{trainingId}")
    @Transactional
    public String makePayment(@PathVariable Long trainingId, RedirectAttributes redirectAttributes) {

        // ACTIVE statuslu tələbələri götür
        List<Students> activeStudents = studentsRepository.findAllByTrainingIdAndStudentStatus(
                trainingId, StudentStatus.ACTIVE
        );

        // Hər bir studentin paid dəyərini VERIFIED-ə kopyala və paid-i sıfırla
        activeStudents.forEach(student -> {
            student.setVERIFIED(student.getPaid());
            student.setPaid(BigDecimal.ZERO);  // paid sıfırlanır
            studentsRepository.save(student);  // DB-də update
        });

        return "redirect:/admintraining/admintraining";
    }

    @PostMapping("/admintrainingCompleted/{trainingId}")
    @Transactional
    public String completePayments(@PathVariable Long trainingId) {

        // ACTIVE statuslu tələbələri götür
        List<Students> activeStudents = studentsRepository.findAllByTrainingIdAndStudentStatus(
                trainingId, StudentStatus.ACTIVE
        );

        activeStudents.forEach(student -> {
            // paid və VERIFIED-i completed-ə əlavə et
            BigDecimal currentCompleted = student.getCompleted() != null ? student.getCompleted() : BigDecimal.ZERO;
            BigDecimal newCompleted = currentCompleted
                    .add(student.getPaid() != null ? student.getPaid() : BigDecimal.ZERO)
                    .add(student.getVERIFIED() != null ? student.getVERIFIED() : BigDecimal.ZERO);

            student.setCompleted(newCompleted);

            // paid və VERIFIED sıfırlanır
            student.setPaid(BigDecimal.ZERO);
            student.setVERIFIED(BigDecimal.ZERO);

            // DB update
            studentsRepository.save(student);


        });

        return "redirect:/admintraining/admintraining";
    }


    @PostMapping("/calculated")
    public String calculated() {
        // 1. Cəmi hesabla
        BigDecimal totalCompleted = studentsRepository.sumCompleted();
        if (totalCompleted == null) totalCompleted = BigDecimal.ZERO;

        System.out.println("Completed cəmi: " + totalCompleted);

        // 2. Calculated entity yaradıb DB-yə yaz
        Calculated calc = new Calculated(totalCompleted);
        calculatedRepository.save(calc);

        // 3. Bütün studentlərin completed sahəsini sıfırla
        List<Students> students = studentsRepository.findAll();
        for (Students student : students) {
            student.setCompleted(BigDecimal.ZERO); // və ya null
        }
        studentsRepository.saveAll(students);

        return "redirect:/admintraining/admintraining";
    }





}
