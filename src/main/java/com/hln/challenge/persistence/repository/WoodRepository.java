package com.hln.challenge.persistence.repository;

import com.hln.challenge.persistence.models.Wood;
import com.hln.challenge.persistence.models.WoodId;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WoodRepository extends JpaRepository<Wood, WoodId> {
    // This method is used to find a single wood entry by its composite primary key
    @Override
    @NonNull
    Optional<Wood> findById(@NonNull WoodId woodId);

    // This method is used to find all wood entries with a specific type
    List<Wood> findByWoodType_Type(String type);

    // This method finds all wood types stored in repository
    @Query("SELECT w.type FROM WoodType w")
    List<String> findAllWoodTypes();

    List<Wood> findByWoodType_TypeAndPriceBetween(String type, BigDecimal minPrice, BigDecimal maxPrice);
}