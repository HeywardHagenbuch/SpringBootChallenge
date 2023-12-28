package com.hln.challenge.service;

import com.hln.challenge.persistence.models.Wood;
import com.hln.challenge.persistence.models.WoodId;
import com.hln.challenge.persistence.repository.WoodRepository;
import com.hln.challenge.persistence.repository.WoodTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class WoodTransactionalService {

    private final WoodRepository woodRepository;
    private final WoodTypeRepository woodTypeRepository;

    public WoodTransactionalService(WoodRepository woodRepository, WoodTypeRepository woodTypeRepository) {
        this.woodRepository = woodRepository;
        this.woodTypeRepository = woodTypeRepository;
    }

    @Transactional
    public List<Wood> validateAndSave(List<Wood> woods) {
        Set<Wood> validWoods = new HashSet<>();
        for (Wood wood : woods) {
            if (isValidWood(wood)) {
                validWoods.add(wood);
                woodRepository.save(wood);
            }
        }
        return new ArrayList<>(validWoods);
    }

    boolean isValidWood(Wood wood) {
        WoodId woodId = wood.getWoodId();
        boolean isIdValid = woodId != null && woodId.getId() > 0;
        boolean isPriceValid = wood.getPrice() != null && wood.getPrice().compareTo(BigDecimal.ZERO) >= 0;
        boolean isTypeValid = wood.getWoodType() != null &&
                woodTypeRepository.existsById(wood.getWoodType().getType());

        return isIdValid && isPriceValid && isTypeValid;
    }
}
