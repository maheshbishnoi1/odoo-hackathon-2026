package com.odoo.backend.specification;

import com.odoo.backend.entity.Expense;
import com.odoo.backend.enums.ExpenseType;
import com.odoo.backend.enums.PaymentMode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamic, composable filtering of {@link Expense} queries.
 */
public class ExpenseSpecification {

    private ExpenseSpecification() {
        // Utility class — not instantiable
    }

    /**
     * Builds a combined specification from optional filter parameters.
     * Any {@code null} parameter is silently ignored.
     *
     * @param vehicleId   optional vehicle ID filter
     * @param tripId      optional trip ID filter
     * @param expenseType optional expense type filter
     * @param paymentMode optional payment mode filter
     * @param fromDate    optional expense date lower bound (inclusive)
     * @param toDate      optional expense date upper bound (inclusive)
     * @return composable {@link Specification}
     */
    public static Specification<Expense> withFilters(
            Long vehicleId,
            Long tripId,
            ExpenseType expenseType,
            PaymentMode paymentMode,
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

            if (expenseType != null) {
                predicates.add(criteriaBuilder.equal(root.get("expenseType"), expenseType));
            }

            if (paymentMode != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMode"), paymentMode));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("expenseDate"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("expenseDate"), toDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
