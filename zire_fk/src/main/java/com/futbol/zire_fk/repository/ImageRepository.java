package com.futbol.zire_fk.repository;

import com.futbol.zire_fk.entity.Image;
import com.futbol.zire_fk.entity.Students;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    // Verilən tələbəyə aid bütün şəkilləri gətir
    List<Image> findByStudent(Students student);

    // Səhifələmə ilə tələbəyə aid şəkilləri gətir
    Page<Image> findByStudent(Students student, Pageable pageable);

    // Fayl adını tapmaq üçün opsional metod
    List<Image> findByFileNameContainingIgnoreCase(String fileName);

}
