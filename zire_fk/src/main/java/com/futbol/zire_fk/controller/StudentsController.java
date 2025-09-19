package com.futbol.zire_fk.controller;

import com.futbol.zire_fk.entity.*;
import com.futbol.zire_fk.repository.StudentsRepository;
import com.futbol.zire_fk.service.AttendanceService;
import com.futbol.zire_fk.service.ImageService;
import com.futbol.zire_fk.service.StudentDebtService;
import com.futbol.zire_fk.service.StudentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.futbol.zire_fk.repository.TrainingRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import com.futbol.zire_fk.dto.StudentsDto;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;



import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentsController {


    private final StudentsService studentsService;
    private final TrainingRepository trainingRepository;
    private final StudentsRepository studentsRepository;
    private final StudentDebtService studentDebtService;
    private final ImageService imageService; //
    private final AttendanceService attendanceService;

    @Autowired
    public StudentsController(StudentsService studentsService,
                              TrainingRepository trainingRepository,
                              StudentsRepository studentsRepository,
                              AttendanceService attendanceService,
                              StudentDebtService studentDebtService,
                              ImageService imageService) {
        this.studentsService = studentsService;
        this.trainingRepository = trainingRepository;
        this.studentsRepository = studentsRepository;
        this.studentDebtService = studentDebtService;
        this.imageService=imageService;
        this.attendanceService=attendanceService;
    }

//Zamani test ucun saxlayanda
//    @GetMapping("/test-debt")
//    public String testDebtIncrease() {
//        studentDebtService.increaseDebtMonthly();
//        return "students/test-debt"; // HTML faylının adı (test-debt.html)
//    }


    @GetMapping("/{trainingId}")
    public String getStudentsByTraining(@PathVariable Long trainingId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "5") int size,
                                        @RequestParam(defaultValue = "id") String sort,
                                        @RequestParam(defaultValue = "asc") String order,
                                        Model model,
                                        HttpServletRequest request) {


        // 🔹 Rol əldə etmək
        boolean isAdmin = request.isUserInRole("ADMIN");
        boolean isSuperAdmin = request.isUserInRole("SUPERADMIN");


        Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "inDebt")); // inDebt sahəsinə görə sort


        Page<Students> studentsPage;

        if (isSuperAdmin) {
            // 🔹 SUPERADMIN → ACTIVE + DELETED + ARCHIVED
            studentsPage = studentsRepository.findByTrainingIdAndStudentStatusIn(
                    trainingId,
                    List.of(StudentStatus.ACTIVE, StudentStatus.DELETED, StudentStatus.ARCHIVED),
                    pageable
            );
        } else if (isAdmin) {
            // 🔹 ADMIN → ACTIVE + DELETED
            studentsPage = studentsRepository.findByTrainingIdAndStudentStatusIn(
                    trainingId,
                    List.of(StudentStatus.ACTIVE, StudentStatus.DELETED),
                    pageable
            );
        } else {
            // 🔹 Digər istifadəçilər (KOCH) → yalnız ACTIVE
            studentsPage = studentsRepository.findByTrainingIdAndStudentStatus(
                    trainingId,
                    StudentStatus.ACTIVE,
                    pageable
            );
        }


        model.addAttribute("trainingId", trainingId);
        model.addAttribute("studentsPage", studentsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("order", order);


        // Theme oxuma
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

        // ACTIVE tələbələrin ümumi borcu
        BigDecimal totalDebt = studentsService.findStudentsByTrainingId(trainingId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent().stream()
                .filter(s -> s.getStudentStatus() == StudentStatus.ACTIVE)
                .map(s -> s.getInDebt() != null ? s.getInDebt() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalDebt", totalDebt);

        // Training və müəllim adları
        model.addAttribute("trainingName", studentsService.getTrainingNameById(trainingId));
        model.addAttribute("teacherName", studentsService.getTeacherNameByTrainingId(trainingId));

        int totalPages = studentsPage.getTotalPages();
        if (totalPages == 0) totalPages = 1;  // minimum 1 page


        // Pagination üçün məlumatlar
        model.addAttribute("pageNumber", studentsPage.getNumber());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("order", order);
        model.addAttribute("baseUrl", "/students/" + trainingId);

        return "students/students";
    }


    @GetMapping("/studentadd")
    public String showAddStudentForm(@RequestParam(required = false) Long trainingId,
                                     Model model,
                                     HttpServletRequest request) {

        // Theme oxuma
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

        // Teacher və Training adlarını əlavə et
        String trainingName = null;
        String teacherName = null;
        if (trainingId != null) {
            trainingName = studentsService.getTrainingNameById(trainingId);
            teacherName = studentsService.getTeacherNameByTrainingId(trainingId);
        }
        model.addAttribute("trainingName", trainingName);
        model.addAttribute("teacherName", teacherName);

        // Yeni StudentsDto yaradılır
        StudentsDto studentDto = new StudentsDto();
        if (trainingId != null) {
            studentDto.setTrainingId(trainingId);
        }
        model.addAttribute("studentDto", studentDto); // DTO form üçün
        model.addAttribute("trainingId", trainingId); // Bu sətir çox vacibdir

        // Dropdown üçün bütün trainings
        List<Training> trainings = trainingRepository.findAll();
        model.addAttribute("trainings", trainings);

        return "students/studentadd";
    }

    @PostMapping("/studentadd")
    public String addStudent(@ModelAttribute("studentDto") StudentsDto studentDto) {

        // DTO -> Entity çevrilməsi
        Students student = new Students();
        student.setName(studentDto.getName());
        student.setSurname(studentDto.getSurname());
        student.setPhone(studentDto.getPhone());
        student.setDateBorn(studentDto.getDateBorn());
        student.setCreatedAt(studentDto.getCreatedAt());
        student.setDocuments(false);


        // Training set etmək
        if (studentDto.getTrainingId() != null) {
            Training training = trainingRepository.findById(studentDto.getTrainingId()).orElse(null);
            student.setTraining(training);
            if (training != null) {
                // 🔹 Borcu training-in monthlyPayment-i ilə set etmək
                student.setInDebt(training.getMonthlyPayment());
            }
        }

        // DB-ə yazmaq
        studentsService.saveStudent(student);


        // Redirect
        Long trainingId = studentDto.getTrainingId();
        if (trainingId != null) {
            return "redirect:/students/" + trainingId;
        }
        return "redirect:/students";
    }

    @PostMapping("/payment/{id}")
    public String makePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Students student = studentsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tələbə tapılmadı: " + id));

        BigDecimal paymentAmount = student.getTraining().getMonthlyPayment(); // aylıq ödəniş

        // Əgər tələbənin borcu yoxdursa, ödəniş etməsin
        if (student.getInDebt() == null || student.getInDebt().compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tələbənin borcu yoxdur!");
            return "redirect:/students/" + student.getTraining().getId();
        }

        // Borcu azalt
        BigDecimal newInDebt = student.getInDebt().subtract(paymentAmount);
        if (newInDebt.compareTo(BigDecimal.ZERO) < 0) {
            newInDebt = BigDecimal.ZERO;
        }
        student.setInDebt(newInDebt);

        // Paid artır (yalnız borcu varsa)
        BigDecimal currentPaid = student.getPaid() != null ? student.getPaid() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(paymentAmount);
        student.setPaid(newPaid);

        // Statusu yenilə
        student.setProgressStatus(StudentProgressStatus.PAID);

        studentsRepository.save(student);

        redirectAttributes.addFlashAttribute("message", "Ödəniş uğurla həyata keçirildi!");
        return "redirect:/students/" + student.getTraining().getId();
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, Authentication authentication) {
        // Tələbəni alırıq ki, training id-ni götürək
        Students student = studentsService.findById(id);
        Long trainingId = student.getTraining().getId();

        // Yumşaq silmə (rola görə)
        studentsService.softDelete(id, authentication);

        // Training id-ə yönləndir
        return "redirect:/students/" + trainingId;
    }

    @GetMapping("/edit/{id}")
    public String editStudent(@PathVariable Long id, Model model, HttpServletRequest request) {

        // Tələbəni DB-dən al
        Students student = studentsService.findById(id);

        if (student == null) {
            return "redirect:/students?error=notfound";
        }

        // Entity-dən DTO-yə köçür
        StudentsDto studentDto = new StudentsDto();
        studentDto.setId(student.getId());
        studentDto.setName(student.getName());
        studentDto.setSurname(student.getSurname());
        studentDto.setPhone(student.getPhone());
        studentDto.setDateBorn(student.getDateBorn());
        studentDto.setCreatedAt(student.getCreatedAt());
        studentDto.setStudentStatus(student.getStudentStatus());
        studentDto.setDocuments(student.getDocuments());
        studentDto.setTrainingId(student.getTraining() != null ? student.getTraining().getId() : null);

        // Theme oxuma
        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue();
                    break;
                }
            }
        }

        model.addAttribute("studentDto", studentDto);  // DTO əlavə edilir
        model.addAttribute("theme", theme + "-mode");

        return "students/studentEdit";  // studentEdit.html faylı
    }


    @PostMapping("/edit")
    public String updateStudent(@ModelAttribute("student") StudentsDto studentsDto, Principal principal) {

        // DB-dən tələbəni al
        Students student = studentsService.findById(studentsDto.getId()); // student-ə görə məlumat yenilənir

        if (student == null) {
            return "redirect:/students?error=notfound";
        }

        // Məlumatları entity-ə köçür
        student.setName(studentsDto.getName());
        student.setSurname(studentsDto.getSurname());
        student.setPhone(studentsDto.getPhone());
        student.setDateBorn(studentsDto.getDateBorn());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"));

        if (isSuperAdmin) {
            student.setStudentStatus(studentsDto.getStudentStatus());
            // Əgər SUPERADMIN yaradılma tarixini dəyişə bilərsə
            student.setCreatedAt(studentsDto.getCreatedAt());
        } else if (isAdmin) {
            if (studentsDto.getStudentStatus() == StudentStatus.ACTIVE
                    || studentsDto.getStudentStatus() == StudentStatus.DELETED) {
                student.setStudentStatus(studentsDto.getStudentStatus());
            }
            // ADMIN adətən yaradılma tarixini dəyişə bilməz
        }

        // Yenilənmiş məlumatları yadda saxla
        studentsService.saveStudent(student);

        // Redirection zamanı yalnız trainingId lazım
        return "redirect:/students/"+studentsDto.getTrainingId();
    }












    @GetMapping("/documents/{studentId}/{trainingId}")
    public String studentDocuments(
            @PathVariable Long studentId,
            @PathVariable Long trainingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model,
            HttpServletRequest request) {

        Students student = studentsService.findById(studentId);
        if (student == null) {
            return "redirect:/students?error=notfound";
        }

        String theme = "dark";
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("theme".equals(cookie.getName())) {
                    theme = cookie.getValue();
                    break;
                }
            }
        }

        Page<Image> imagesPage = imageService.getImagesByStudentPaginated(student, PageRequest.of(page, size));


        model.addAttribute("student", student);
        model.addAttribute("trainingId", trainingId);
        model.addAttribute("imagesPage", imagesPage);
        model.addAttribute("images", imagesPage.getContent());
        model.addAttribute("theme", theme);

// ✅ Pagination üçün uyğun atributlar
        model.addAttribute("pageNumber", imagesPage.getNumber());
        model.addAttribute("totalPages", imagesPage.getTotalPages());
        model.addAttribute("size", imagesPage.getSize());

        return "students/documents";
    }











    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/documents/{studentId}/{trainingId}")
    public String uploadImage(@PathVariable Long studentId,
                              @PathVariable Long trainingId,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam("documentName") String documentName) throws IOException {

        Students student = studentsService.findById(studentId);
        if (student == null) {
            return "redirect:/students?error=notfound";
        }

        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs(); // Qovluğu yarad
            System.out.println("Qovluq yaradıldı: " + uploadPath.getAbsolutePath());
        }

        // Faylın adı
        String fileName = file.getOriginalFilename();
        File dest = new File(uploadPath, fileName);

        // Faylı diskə yaz
        file.transferTo(dest);

        // DB-yə əlavə et
        Image img = new Image();
        img.setFileName(fileName);
        img.setDocumentName(documentName);
        img.setUploadDate(LocalDateTime.now());
        img.setUploadedBy("SYSTEM");
        img.setStudent(student);
        imageService.saveImage(img);

        return "redirect:/students/documents/" + studentId + "/" + trainingId;
    }










    // 🗑️ Şəkil silmə
    @GetMapping("/documents/{studentId}/delete/{imageId}")
    public String deleteImage(@PathVariable Long studentId,
                              @PathVariable Long imageId) {
        // Image obyekti götürülür
        Image image = imageService.findById(imageId);

        // Student-in training-id tapılır
        Long trainingId = image.getStudent().getTraining().getId();

        // Image silinir
        imageService.deleteImage(imageId);

        // Redirect edilir
        return "redirect:/students/documents/" + studentId + "/" + trainingId;
    }


    // Tamamlandı (true)
    @GetMapping("/documents/completed/{studentId}/{trainingId}")
    public String markCompleted(@PathVariable Long studentId,
                                @PathVariable Long trainingId) {
        Students student = studentsService.findById(studentId);
        if(student != null) {
            student.setDocuments(true);
            studentsService.save(student); // DB-yə yaz
        }
        return "redirect:/students/documents/" + studentId + "/" + trainingId;
    }

    // Tamamlanmadı (false)
    @GetMapping("/documents/notcompleted/{studentId}/{trainingId}")
    public String markNotCompleted(@PathVariable Long studentId,
                                   @PathVariable Long trainingId) {
        Students student = studentsService.findById(studentId);
        if(student != null) {
            student.setDocuments(false);
            studentsService.save(student); // DB-yə yaz
        }
        return "redirect:/students/documents/" + studentId + "/" + trainingId;
    }


    @GetMapping("/studentHours/{trainingId}")
    public String studentHours(@PathVariable("trainingId") Long trainingId, Model model) {
        attendanceService.markPresentForTraining(trainingId);
        model.addAttribute("trainingId", trainingId);
        return "redirect:/students/"+trainingId;
    }

    @GetMapping("/attendance")
    public String studentAttendance(@RequestParam("studentId") Long studentId) {
        attendanceService.markAbsentForStudent(studentId);

        Long trainingId = studentsRepository.findById(studentId)
                .map(s -> s.getTraining().getId())
                .orElseThrow(() -> new RuntimeException("Student tapılmadı: " + studentId));

        return "redirect:/students/" + trainingId;
    }






}