package com.futbol.zire_fk.service;

import com.futbol.zire_fk.dto.TrainingDto;
import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.entity.Training;
import com.futbol.zire_fk.entity.TrainingDeleteStatus;
import com.futbol.zire_fk.repository.TrainingRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;

    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    // 🔹 Koch-a görə ACTIVE trainings (pagination ilə)
    public Page<Training> getTrainingsByKoch(Long kochId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return trainingRepository.findByKochIdActive(kochId, TrainingDeleteStatus.ACTIVE, pageable);
    }

    // 🔹 Bütün aktiv trainings (pagination ilə)
    public Page<Training> getAllActiveTrainings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        return trainingRepository.findAllActive(TrainingDeleteStatus.ACTIVE, pageable);
    }

    // 🔹 Həm ACTIVE, həm DELETED trainings
    public Page<Training> getAllTrainings(Pageable pageable) {
        return trainingRepository.findAllByStatuses(
                List.of(TrainingDeleteStatus.ACTIVE, TrainingDeleteStatus.DELETED),
                pageable
        );

    }
    // 🔹 Həm ACTIVE
    public Page<Training> getAllTrainingsa(Pageable pageable) {
        return trainingRepository.findAllByStatuses(
                List.of(TrainingDeleteStatus.ACTIVE),
                pageable
        );

    }

    // 🔹 Yeni training əlavə etmək (DTO + Koch)
    public Training saveFromDto(TrainingDto dto, Koch koch) {
        Training training = new Training();
        training.setName(dto.getName());
        training.setAgeRange(dto.getAgeRange());
        training.setMonthlyPayment(BigDecimal.valueOf(dto.getMonthlyPayment()));
        training.setCreatedDate(dto.getBorn());
        training.setKoch(koch);
        training.setTrainingDelete(TrainingDeleteStatus.ACTIVE);
        return trainingRepository.save(training);
    }

    // 🔹 ID ilə training tapmaq
    public Optional<Training> findById(Long id) {
        return trainingRepository.findById(id);
    }

    // 🔹 Mövcud training-i update etmək
    public Training updateFromDto(Long id, TrainingDto dto) {
        return trainingRepository.findById(id)
                .map(training -> {
                    training.setName(dto.getName());
                    training.setAgeRange(dto.getAgeRange());
                    training.setMonthlyPayment(BigDecimal.valueOf(dto.getMonthlyPayment()));
                    training.setCreatedDate(dto.getBorn());
                    training.setTrainingDelete(dto.getStatus());
                    return trainingRepository.save(training);
                })
                .orElseThrow(() -> new RuntimeException("Training tapılmadı"));
    }

    // 🔹 Soft delete (status = DELETED)
    public void deleteById(Long id) {
        Optional<Training> trainingOpt = trainingRepository.findById(id);
        if (trainingOpt.isPresent()) {
            trainingRepository.updateTrainingDelete(id, TrainingDeleteStatus.DELETED);
        } else {
            throw new EmptyResultDataAccessException(1);
        }
    }

    // 🔹 Soft delete (status = DELETED)
    public void deleteByIdforAdmin(Long id) {
        Optional<Training> trainingOpt = trainingRepository.findById(id);
        if (trainingOpt.isPresent()) {
            trainingRepository.updateTrainingDelete(id, TrainingDeleteStatus.ARCHIVED);
        } else {
            throw new EmptyResultDataAccessException(1);
        }
    }



    // 🔹 Arxivləşdirmək (status = ARCHIVED)
    public void archiveById(Long id) {
        Optional<Training> trainingOpt = trainingRepository.findById(id);
        if (trainingOpt.isPresent()) {
            trainingRepository.updateTrainingDelete(id, TrainingDeleteStatus.ARCHIVED);
        } else {
            throw new EmptyResultDataAccessException(1);
        }
    }
    public Page<Training> getAllTrainingsByKochStatus(TrainingDeleteStatus status, Pageable pageable) {
        return trainingRepository.findAllByKoch_AdminStatus(status, pageable);
    }

    // TrainingService
    public void updatePaidAmount(Training training, BigDecimal totalPayment, BigDecimal totalDebt) {
        training.setPaidAmount(totalPayment.subtract(totalDebt));
        trainingRepository.save(training); // DB-yə yazılır
    }

    public List<Training> getActiveTrainingsByKoch(Long kochId) {
        return trainingRepository.findByKochIdAndTrainingDelete(kochId, TrainingDeleteStatus.ACTIVE);
    }

    public List<Training> getAllActive() {
        List<TrainingDeleteStatus> statuses = List.of(TrainingDeleteStatus.ACTIVE, TrainingDeleteStatus.DELETED);
        return trainingRepository.findAllByTrainingDeleteIn(statuses);
    }

    public List<Training> getAllActivess() {
        List<TrainingDeleteStatus> statuses = List.of(TrainingDeleteStatus.ACTIVE);
        return trainingRepository.findAllByTrainingDeleteIn(statuses);
    }




}
