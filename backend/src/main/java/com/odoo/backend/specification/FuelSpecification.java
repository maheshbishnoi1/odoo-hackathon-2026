package com.odoo.backend.specification;

import com.odoo.backend.entity.FuelLog;
import com.odoo.backend.enums.FuelType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic, composable filtering of {@link FuelLog} queries.
 */
public class FuelSpecification {

    private FuelSpecification() {
        // Utility class — not instantiable
    }

    /**
     * Builds a combined specification from optional filter parameters.
     * Any {@code null} parameter is ignored.
     *
     * @param vehicleId optional vehicle ID filter
     * @param tripId    optional trip ID filter
     * @param fuelType  optional fuel type filter
     * @param fromDate  optional fuel date lower bound (inclusive)
     * @param toDate    optional fuel date upper bound (inclusive)
     * @return composable {@link Specification}
     */
    public static Specification<FuelLog> withFilters(
            Long vehicleId,
            Long tripId,
            FuelType fuelType,
            LocalDate fromDate,
            LocalDate toDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (vehicleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicle").get("id"), vehicleId));
            }

            if (tripId != null) {
                predicates.add(criteriaBuilder.equal(root.get("trip").get("id"), tripId));
            }

            if (fuelType != null) {
                predicates.add(criteriaBuilder.equal(root.get("fuelType"), fuelType));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("fuelDate"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("fuelDate"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
