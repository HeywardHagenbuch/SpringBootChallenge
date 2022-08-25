package com.hln.challenge.persistence.repository;

import com.hln.challenge.persistence.models.WoodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WoodTypeRepository extends JpaRepository<WoodType, String> {
    boolean existsByType(String type);

}
