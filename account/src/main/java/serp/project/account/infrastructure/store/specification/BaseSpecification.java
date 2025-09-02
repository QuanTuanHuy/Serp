/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public class BaseSpecification<T> {

    public static <T> Specification<T> alwaysTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    public static <T> Specification<T> alwaysFalse() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
    }

    public static <T> Specification<T> equal(String field, Object value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.equal(root.get(field), value);
        };
    }

    public static <T> Specification<T> notEqual(String field, Object value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.notEqual(root.get(field), value);
        };
    }

    public static <T> Specification<T> like(String field, String value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null || value.trim().isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(field)),
                    "%" + value.toLowerCase() + "%"
            );
        };
    }

    public static <T> Specification<T> in(String field, List<?> values) {
        return (root, query, criteriaBuilder) -> {
            if (values == null || values.isEmpty()) return criteriaBuilder.conjunction();
            return root.get(field).in(values);
        };
    }

    public static <T> Specification<T> notIn(String field, List<?> values) {
        return (root, query, criteriaBuilder) -> {
            if (values == null || values.isEmpty()) return criteriaBuilder.conjunction();
            return criteriaBuilder.not(root.get(field).in(values));
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Specification<T> greaterThan(String field, Comparable value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.greaterThan(root.get(field), value);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Specification<T> greaterThanOrEqual(String field, Comparable value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.greaterThanOrEqualTo(root.get(field), value);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Specification<T> lessThan(String field, Comparable value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.lessThan(root.get(field), value);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Specification<T> lessThanOrEqual(String field, Comparable value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) return criteriaBuilder.conjunction();
            return criteriaBuilder.lessThanOrEqualTo(root.get(field), value);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Specification<T> between(String field, Comparable from, Comparable to) {
        return (root, query, criteriaBuilder) -> {
            if (from == null && to == null) return criteriaBuilder.conjunction();
            if (from == null) return criteriaBuilder.lessThanOrEqualTo(root.get(field), to);
            if (to == null) return criteriaBuilder.greaterThanOrEqualTo(root.get(field), from);
            return criteriaBuilder.between(root.get(field), from, to);
        };
    }

    public static <T> Specification<T> dateTimeBetween(String field, LocalDateTime from, LocalDateTime to) {
        return between(field, from, to);
    }

    public static <T> Specification<T> isNull(String field) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get(field));
    }

    public static <T> Specification<T> isNotNull(String field) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get(field));
    }

    public static <T> Specification<T> orderBy(String field, boolean ascending) {
        return (root, query, criteriaBuilder) -> {
            if (ascending) {
                query.orderBy(criteriaBuilder.asc(root.get(field)));
            } else {
                query.orderBy(criteriaBuilder.desc(root.get(field)));
            }
            return criteriaBuilder.conjunction();
        };
    }

    // Helper method to combine specifications with AND
    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specifications) {
        if (specifications == null || specifications.length == 0) {
            return alwaysTrue();
        }
        
        Specification<T> result = specifications[0];
        for (int i = 1; i < specifications.length; i++) {
            if (specifications[i] != null) {
                result = result.and(specifications[i]);
            }
        }
        return result != null ? result : alwaysTrue();
    }

    // Helper method to combine specifications with OR
    @SafeVarargs
    public static <T> Specification<T> or(Specification<T>... specifications) {
        if (specifications == null || specifications.length == 0) {
            return alwaysFalse();
        }
        
        Specification<T> result = specifications[0];
        for (int i = 1; i < specifications.length; i++) {
            if (specifications[i] != null) {
                result = result.or(specifications[i]);
            }
        }
        return result != null ? result : alwaysFalse();
    }
}