package com.odoo.backend.specification;

import com.odoo.backend.entity.MaintenanceRecord;
import com.odoo.backend.enums.MaintenanceStatus;
import com.odoo.backend.enums.MaintenanceType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic, composable filtering of {@link MaintenanceRecord} queries.
 *
 * <p>Usage example (in service layer):</p>
 * <pre>{@code
 *   Specification<MaintenanceRecord> spec =
 *       MaintenanceSpecification.withFilters(vehicleId, status, type, fromDate, toDate);
 *   Page<MaintenanceRecord> page = repository.findAll(spec, pageable);
 * }</pre>
 */
public class MaintenanceSpecification {

    private MaintenanceSpecification() {
        // Utility class — not instantiable
    }

    /**
     * Builds a combined specification from the provided filter parameters.
     * Any {@code null} parameter is silently ignored (i.e., no filter applied for that field).
     *
     * @param vehicleId       optional vehicle ID filter
     * @param status          optional maintenance status filter
     * @param maintenanceType optional maintenance type filter
     * @param fromDate        optional start-date lower bound (inclusive)
     * @param toDate          optional start-date upper bound (inclusive)
     * @return a {@link Specification} that applies all non-null filters via AND
     */
    public static Specification<MaintenanceRecord> withFilters(
            Long vehicleId,
            MaintenanceStatus status,
            MaintenanceType maintenanceType,
            LocalDate fromDate,
            LocalDate toDate) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (vehicleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicle").get("id"), vehicleId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (maintenanceType != null) {
                predicates.add(criteriaBuilder.equal(root.get("maintenanceType"), maintenanceType));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Filters by vehicle ID only.
     *
     * @param vehicleId the vehicle primary key
     * @return specification filtering by vehicle
     */
    public static Specification<MaintenanceRecord> byVehicleId(Long vehicleId) {
        return (root, query, cb) -> cb.equal(root.get("vehicle").get("id"), vehicleId);
    }

    /**
     * Filters by maintenance status.
     *
     * @param status the status to filter by
     * @return specification filtering by status
     */
    public static Specification<MaintenanceRecord> byStatus(MaintenanceStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
