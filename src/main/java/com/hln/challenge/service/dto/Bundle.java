package com.hln.challenge.service.dto;

import com.hln.challenge.persistence.models.Wood;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bundle {
    private String id;
    private List<Wood> bundle;
    private BigDecimal price;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bundle bundle = (Bundle) o;
        return Objects.equals(id, bundle.id) &&
                Objects.equals(this.bundle, bundle.bundle) &&
                Objects.equals(price, bundle.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bundle, price);
    }
}
