package com.futbol.zire_fk.service;

import com.futbol.zire_fk.entity.Image;
import com.futbol.zire_fk.entity.Students;
import com.futbol.zire_fk.repository.ImageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    // Şəkil yükləmə
    public Image uploadImage(Image image) {
        return imageRepository.save(image);
    }

    // Bütün şəkilləri gətir
    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    // Tələbəyə aid şəkilləri gətir
    public List<Image> getImagesByStudent(Students student) {
        return imageRepository.findByStudent(student);
    }

    // Şəkili sil
    public void deleteImage(Long id) {
        Optional<Image> image = imageRepository.findById(id);
        image.ifPresent(imageRepository::delete);
    }


    // Yeni şəkil əlavə et
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    public Page<Image> getImagesByStudentPaginated(Students student, Pageable pageable) {
        return imageRepository.findByStudent(student, pageable);
    }

    public Page<Image> getImagesByStudent(Students student, Pageable pageable) {
        return imageRepository.findByStudent(student, pageable);
    }

    // Image-i ID-yə görə gətirən metod
    public Image findById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Image tapılmadı: " + id));
    }





}
