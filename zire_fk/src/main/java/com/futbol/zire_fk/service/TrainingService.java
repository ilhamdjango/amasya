package com.futbol.zire_fk.service;

import com.futbol.zire_fk.dto.TrainingDto;
import java.math.BigDecimal;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.futbol.zire_fk.entity.Training;
import com.futbol.zire_fk.repository.TrainingRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.time.format.DateTimeFormatter;
import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.entity.TrainingDeleteStatus;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TrainingService {
    private final TrainingRepository trainingRepository;
    public TrainingService(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }
    // Soft delete (fiziki silmir, sadəcə active=false)
    public void deleteById(Long id) {
        Optional<Training> trainingOpt = trainingRepository.findById(id);
        if (trainingOpt.isPresent()) {
            trainingRepository.updateTrainingDelete(id, TrainingDeleteStatus.DELETED); // delete edəndə 2 olsun
        } else {
            throw new EmptyResultDataAccessException(1);
        }
    }

    public void deletetrById(Long id) {
        Optional<Training> trainingOpt = trainingRepository.findById(id);
        if (trainingOpt.isPresent()) {
            trainingRepository.updateTrainingDelete(id, TrainingDeleteStatus.ARCHIVED); // delete edəndə 2 olsun
        } else {
            throw new EmptyResultDataAccessException(1);
        }
    }

    // Bütün aktiv trainings



    // Koch-a görə aktiv trainings
    public Page<Training> findByKochId(Long kochId, Pageable pageable) {
        return trainingRepository.findByKochIdActive(kochId, TrainingDeleteStatus.ACTIVE, pageable);
    }

    // həm ACTIVE, həm DELETED
    public Page<Training> getAllTrainings(Pageable pageable) {
        return trainingRepository.findAllByStatuses(
                List.of(TrainingDeleteStatus.ACTIVE, TrainingDeleteStatus.DELETED),
                pageable
        );
    }



    public Training saveFromDto(TrainingDto dto, Koch koch) {
        Training training = new Training();
        training.setName(dto.getName());
        training.setAgeRange(dto.getAgeRange());
        training.setMonthlyPayment(BigDecimal.valueOf(dto.getMonthlyPayment()));
        training.setCreatedDate(dto.getBorn());
        training.setActivePercent((double) ThreadLocalRandom.current().nextInt(20, 101));
        // Koch-u entity-yə birbaşa set edirik
        training.setKoch(koch);

        return trainingRepository.save(training);
    }


    public Optional<Training> findById(Long id) {
        return trainingRepository.findById(id);
    }

    public Training updateFromDto(Long id, TrainingDto dto) {
        return trainingRepository.findById(id)
                .map(training -> {
                    training.setName(dto.getName());
                    training.setAgeRange(dto.getAgeRange());
                    training.setMonthlyPayment(BigDecimal.valueOf(dto.getMonthlyPayment()));

                    // dd-MM-yyyy formatında ekrana yazmaq
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    System.out.println(dto.getBorn().format(formatter) + "--------dd----");

                    // LocalDate obyektini birbaşa set et
                    training.setCreatedDate(dto.getBorn());

                    return trainingRepository.save(training);
                })
                .orElseThrow(() -> new RuntimeException("Training tapılmadı"));
    }

}

