package com.futbol.zire_fk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.repository.AttendanceRepository;
import com.futbol.zire_fk.repository.CalculatedRepository;
import com.futbol.zire_fk.repository.StudentsRepository;
import com.futbol.zire_fk.service.AttendanceService;
import com.futbol.zire_fk.service.KochService;
import com.futbol.zire_fk.service.StudentsService;
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

import java.math.BigDecimal;
import java.security.Principal;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

import org.springframework.web.bind.annotation.PathVariable;
import com.futbol.zire_fk.entity.Role;
import com.futbol.zire_fk.entity.TrainingDeleteStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;




@Controller
@RequestMapping("koch/")
public class KochController {
    private final KochService kochService;
    private final StudentsService studentsService;
    private final StudentsRepository studentsRepository;
    private  final CalculatedRepository calculatedRepository;
    private final AttendanceService attendanceService;
    public KochController(KochService kochService,StudentsService studentsService, StudentsRepository studentsRepository,CalculatedRepository calculatedRepository,AttendanceService attendanceService) {
        this.kochService = kochService;
        this.studentsService=studentsService;
        this.studentsRepository=studentsRepository;
        this.calculatedRepository=calculatedRepository;
        this.attendanceService=attendanceService;
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
                if ("theme".equals(cookie.getName())) theme = cookie.getValue();
            }
        }
        model.addAttribute("theme", theme + "-mode");

        // Page size
        String userAgent = request.getHeader("User-Agent");
        int size = (userAgent != null && (userAgent.contains("Mobi")
                || userAgent.contains("Android") || userAgent.contains("iPhone"))) ? 5 : 5;

        // Koch list
        Page<Koch> kochPage = kochService.getKochListForUser(principal, PageRequest.of(page, size));
        model.addAttribute("kochs", kochPage.getContent());
        model.addAttribute("page", kochPage);
        model.addAttribute("size", size);

        // 🔹 bunu əlavə et ki, thymeleaf pagination universal işləsin
        model.addAttribute("baseUrl", "/koch/kochList");

        //kochlarin sayi
        long teacherCount = kochService.getTeacherCount();
        model.addAttribute("teacherCount", teacherCount);

        //student sayi
        long studentCount = studentsService.getActiveStudentCount();
        model.addAttribute("studentCount", studentCount);


        Double activePercent = studentsService.getActivePercent();
        model.addAttribute("activePercent", activePercent);

        BigDecimal totalPaid = studentsRepository.getTotalPaid();
        model.addAttribute("totalpaid", totalPaid);


// Son 6 ay diaqram
        LocalDateTime now = LocalDateTime.now();
        List<String> last6MonthNames = new ArrayList<>();
        List<BigDecimal> last6MonthTotals = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusMonths(1);

            BigDecimal total = calculatedRepository.sumTotalCompletedBetween(start, end);

            last6MonthNames.add(start.getMonth().name());
            last6MonthTotals.add(total != null ? total : BigDecimal.ZERO);

        }

// JSON-a çevir və model-ə əlavə et
        ObjectMapper mapper = new ObjectMapper();
        String last6MonthsJson = "[]";
        String attendanceTotalsJson = "[]";

        try {
            last6MonthsJson = mapper.writeValueAsString(last6MonthNames);
            attendanceTotalsJson = mapper.writeValueAsString(last6MonthTotals);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        model.addAttribute("last6MonthsJson", last6MonthsJson);
        model.addAttribute("attendanceTotalsJson", attendanceTotalsJson);


        BigDecimal lastMonthTotal = BigDecimal.ZERO;
        if (!last6MonthTotals.isEmpty()) {
            lastMonthTotal = last6MonthTotals.get(last6MonthTotals.size() - 1);
        }

// Model-ə əlavə et
        model.addAttribute("lastMonthTotal", lastMonthTotal);

        String lastMonthName = "";
        if (!last6MonthNames.isEmpty()) {
            lastMonthName = last6MonthNames.get(last6MonthNames.size() - 1);
        }

// Model-ə əlavə et
        model.addAttribute("lastMonthName", lastMonthName);

        BigDecimal totalDebt = studentsService.getTotalDebt();
        model.addAttribute("totalDebt", totalDebt);

        List<String> topAbsentStudents = attendanceService.getTop3MostAbsentStudentNames();
        model.addAttribute("topAbsentStudents", topAbsentStudents);

        List<String> topDebtors = studentsService.getTop3Debtors();
        model.addAttribute("topDebtors", topDebtors);





        return "koch/kochList";
    }
























    // Formu göstərmək üçün
    @GetMapping("/kochAdd")
    public String showForm(Model model, HttpServletRequest request, Principal principal) {
        model.addAttribute("kochDto", new KochDto());

        // Theme cookie
        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue();
                }
            }
        }
        model.addAttribute("theme", theme + "-mode");

        // Login olmuş istifadəçi və rol
        if (principal != null) {
            kochService.findByUsernameAndStatus(principal.getName(), TrainingDeleteStatus.ACTIVE)
                    .ifPresent(user -> {
                        model.addAttribute("name", user.getName());
                        model.addAttribute("userRole", user.getRole().name()); // <-- rol əlavə etdik
                    });
        }

        return "koch/kochAdd";
    }








    // Form submit (save)
    @PostMapping("/kochAdd")
    public String addKoch(KochDto kochDto) {
        kochService.saveFromDto(kochDto);
        return "redirect:/koch/kochList"; // save sonrası list səhifəsinə yönləndir
    }




    @PostMapping("/kochEdit/{id}")
    public String updateKoch(@PathVariable Long id,
                             @ModelAttribute KochDto kochDto,
                             RedirectAttributes redirectAttributes) {
        Optional<Koch> kochOpt = kochService.findById(id);
        if (kochOpt.isPresent()) {
            Koch koch = kochOpt.get();

            // Əgər son admindirsə və rol dəyişirsə
            if (koch.getRole() == Role.ADMIN &&
                    kochDto.getRole() != Role.ADMIN &&
                    kochService.countByRole(Role.ADMIN) <= 1) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Sistemdə ən azı bir aktiv admin qalmalıdır!");
                return "redirect:/koch/kochList";
            }

            // Sadə mapping
            koch.setName(kochDto.getName());
            koch.setSurname(kochDto.getSurname());
            koch.setUsername(kochDto.getUsername());

            // Şifrəni boş gəlmirsə göndər (amma hələ encode etmə)
            if (kochDto.getPassword() != null && !kochDto.getPassword().isBlank()) {
                koch.setPassword(kochDto.getPassword());
            }

            if (kochDto.getRole() != null) {
                koch.setRole(kochDto.getRole());
            }
            koch.setBorn(kochDto.getBorn());

            // 🔹 Burada RequestParam ilə status yazılır
            if (kochDto.getStatus() != null) {
                koch.setAdminStatus(kochDto.getStatus());
            }

            // Service-də encode olunacaq
            kochService.saveFromDto(kochDto);

            redirectAttributes.addFlashAttribute("successMessage", "İstifadəçi uğurla dəyişdirildi!");
        }
        return "redirect:/koch/kochList";
    }



    @GetMapping("/kochEdit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpServletRequest request,
                           Principal principal) {

        // Koch obyektini tapmaq
        Koch koch = kochService.findById(id)
                .orElseThrow(() -> new RuntimeException("Koch tapılmadı: " + id));

        // Entity → DTO map et
        KochDto dto = new KochDto();
        dto.setId(koch.getId());
        dto.setName(koch.getName());
        dto.setSurname(koch.getSurname());
        dto.setUsername(koch.getUsername());
        dto.setRole(koch.getRole());
        dto.setBorn(koch.getBorn());
        dto.setPassword(koch.getPassword());
        dto.setStatus(koch.getAdminStatus()); 

        model.addAttribute("kochDto", dto);

        // Theme cookie
        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue();
                }
            }
        }
        model.addAttribute("theme", theme + "-mode");

        // Login olmuş istifadəçi
        if (principal != null) {
            kochService.findByUsername(principal.getName())
                    .ifPresent(user -> {
                        model.addAttribute("name", user.getName());
                        model.addAttribute("userRole", user.getRole().name());
                    });
        }

        // Sonuncu ACTIVE SUPERADMIN olub-olmadığını yoxla
        boolean isLastActiveSuperAdmin = false;
        if (koch.getRole() == Role.SUPERADMIN && koch.getAdminStatus() == TrainingDeleteStatus.ACTIVE) {
            long activeSuperAdmins = kochService.countByRole(Role.SUPERADMIN);
            if (activeSuperAdmins <= 1) {
                isLastActiveSuperAdmin = true; // Sonuncu SuperAdmin, dəyişmək və ya silmək olmaz
            }
        }

        model.addAttribute("isLastActiveSuperAdmin", isLastActiveSuperAdmin);

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
