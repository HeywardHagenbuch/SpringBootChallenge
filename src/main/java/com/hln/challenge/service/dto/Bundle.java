package com.hln.challenge.service.dto;

import com.hln.challenge.persistence.models.Wood;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bundle {
    private String id;
    private List<Wood> bundle;
    private BigDecimal price;
}
