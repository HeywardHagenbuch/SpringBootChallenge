package com.hln.challenge.persistence.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "wood")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Wood {
    @EmbeddedId
    private WoodId woodId;

    @MapsId("type") // Maps 'type' attribute of WoodId to 'type' in WoodType
    @ManyToOne
    @JoinColumn(name = "type", referencedColumnName = "type", insertable = false, updatable = false)
    private WoodType woodType;

    private BigDecimal price;

    // Override equals and hashCode to handle composite key
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wood wood)) return false;
        return Objects.equals(woodId, wood.woodId);
    }

    @Override
    public int hashCode() {
        return woodId != null ? woodId.hashCode() : 0;
    }
}