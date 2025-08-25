package com.futbol.zire_fk.service;
import com.futbol.zire_fk.entity.Koch;
import com.futbol.zire_fk.repository.KochRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.futbol.zire_fk.dto.KochDto;
import com.futbol.zire_fk.entity.Role;
@Service
public class KochService {



    @Autowired
    private final KochRepository kochRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    public KochService(KochRepository kochRepository) {
        this.kochRepository = kochRepository;
    }

    public List<Koch> findAll() {
        return kochRepository.findAll();
    }

    public long count() {
        return kochRepository.count();
    }

    public Optional<Koch> findById(Long id) {
        return kochRepository.findById(id);
    }

    // İstifadəçi tapmaq (controller üçün)
    public Optional<Koch> findByUsername(String username) {
        return kochRepository.findByUsername(username);
    }

    public Koch save(Koch koch) {
        return kochRepository.save(koch);
    }


    public void saveFromDto(KochDto kochDto) {
        Koch koch = new Koch();
        koch.setName(kochDto.getName());
        koch.setSurname(kochDto.getSurname());
        koch.setUsername(kochDto.getUsername());
        koch.setPassword(passwordEncoder.encode(kochDto.getPassword())); // password encode
        if (kochDto.getRole() != null) {
            koch.setRole(Role.valueOf(kochDto.getRole().toUpperCase()));
        }
        koch.setBorn(kochDto.getBorn());
        koch.setActive(kochDto.isActive());

        // Əlavə default dəyərlər təyin etmək istəsəniz burada edə bilərsiniz

        kochRepository.save(koch);
    }

    public void deleteById(Long id) {
        kochRepository.deleteById(id);
    }

    public Page<Koch> findAll(Pageable pageable) {
        return kochRepository.findAll(pageable);
    }
        // Login yoxlaması
    public boolean login(String username, String password) {
            Optional<Koch> kochOpt = kochRepository.findByUsername(username);
            if (kochOpt.isPresent()) {
                Koch koch = kochOpt.get();
                // aktivdir və password uyğun gəlirsə
                if (koch.isActive() && passwordEncoder.matches(password, koch.getPassword())) {
                    return true;
                }
            }
            return false;
        }







}
