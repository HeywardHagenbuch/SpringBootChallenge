package com.hln.challenge.persistence.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class WoodId implements Serializable {
    private String type; // Part of the composite key
    private Long id;     // Part of the composite key
}