package com.hln.challenge.config;
import com.hln.challenge.persistence.models.WoodType;
import com.hln.challenge.persistence.repository.WoodTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WoodTypeDataLoader implements CommandLineRunner {
    private final WoodTypeRepository woodTypeRepository;

    public WoodTypeDataLoader(WoodTypeRepository woodTypeRepository) {
        this.woodTypeRepository = woodTypeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Example wood types
        List<String> types = Arrays.asList("OAK", "PINE", "MAPLE");
        for (String type : types) {
            if (!woodTypeRepository.existsById(type)) {
                woodTypeRepository.save(new WoodType(type));
            }
        }
    }
}

