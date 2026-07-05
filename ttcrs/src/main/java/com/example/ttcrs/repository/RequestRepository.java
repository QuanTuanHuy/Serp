package com.example.ttcrs.repository;

import com.example.ttcrs.constant.RequestStatus;
import com.example.ttcrs.constant.RequestType;
import com.example.ttcrs.entity.RequestEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository cho {@link RequestEntity}.
 *
 * Kế thừa {@link JpaSpecificationExecutor} để hỗ trợ dynamic filtering
 * thông qua Criteria API (Specification pattern).
 */
@Repository
public interface RequestRepository
        extends JpaRepository<RequestEntity, Long>,
                JpaSpecificationExecutor<RequestEntity> {
    // Tự parse method name → Spring Data JPA sẽ tự generate query                   
    List<RequestEntity> findAllByTransportPlanId(Long transportPlanId);

    // =========================================================================
    // Inner class chứa các Specification tĩnh — tái sử dụng linh hoạt
    // =========================================================================

    /**
     * Tập hợp các {@link Specification} dùng để build dynamic query
     * cho bảng {@code requests}.
     *
     * <p>Cách dùng:
     * <pre>
     *   Specification&lt;RequestEntity&gt; spec =
     *       RequestSpecs.withTenantId(tenantId)
     *           .and(RequestSpecs.withStatuses(statuses))
     *           .and(RequestSpecs.withType(type));
     * </pre>
     */
    final class RequestSpecs {

        private RequestSpecs() { /* utility class */ }

        /**
         * [BẮT BUỘC] Lọc theo tenant_id — luôn phải có trong mọi query.
         */
        public static Specification<RequestEntity> withTenantId(Long tenantId) {
            return (root, query, cb) ->
                    cb.equal(root.get("tenantId"), tenantId);
        }

        /**
         * Lọc theo danh sách status (IN clause).
         * Nếu list null hoặc rỗng → không áp dụng điều kiện.
         */
        public static Specification<RequestEntity> withStatuses(List<RequestStatus> statuses) {
            return (root, query, cb) -> {
                if (statuses == null || statuses.isEmpty()) {
                    return cb.conjunction(); // TRUE — không filter
                }
                return root.get("status").in(statuses);
            };
        }

        /**
         * Lọc theo request type (OF, IF, OE, IE).
         * Nếu null → không áp dụng.
         */
        public static Specification<RequestEntity> withType(RequestType type) {
            return (root, query, cb) ->
                    type == null
                            ? cb.conjunction()
                            : cb.equal(root.get("type"), type);
        }

        /**
         * Lọc theo src_location_code (exact match, case-insensitive).
         * Nếu null hoặc blank → không áp dụng.
         */
        public static Specification<RequestEntity> withSrcLocationCode(String srcLocationCode) {
            return (root, query, cb) ->
                    (srcLocationCode == null || srcLocationCode.isBlank())
                            ? cb.conjunction()
                            : cb.equal(
                                    cb.lower(root.get("srcLocationCode")),
                                    srcLocationCode.toLowerCase()
                              );
        }

        /**
         * Lọc theo dest_location_code (exact match, case-insensitive).
         * Nếu null hoặc blank → không áp dụng.
         */
        public static Specification<RequestEntity> withDestLocationCode(String destLocationCode) {
            return (root, query, cb) ->
                    (destLocationCode == null || destLocationCode.isBlank())
                            ? cb.conjunction()
                            : cb.equal(
                                    cb.lower(root.get("destLocationCode")),
                                    destLocationCode.toLowerCase()
                              );
        }

        /**
         * Lọc theo customer_id.
         * Nếu null → không áp dụng.
         */
        public static Specification<RequestEntity> withCustomerId(Long customerId) {
            return (root, query, cb) ->
                    customerId == null
                            ? cb.conjunction()
                            : cb.equal(root.get("customerId"), customerId);
        }

        /**
         * Lọc theo created_by (userId của người tạo).
         * Nếu null → không áp dụng.
         */
        public static Specification<RequestEntity> withCreatedBy(Long createdBy) {
            return (root, query, cb) ->
                    createdBy == null
                            ? cb.conjunction()
                            : cb.equal(root.get("createdBy"), createdBy);
        }

        /**
         * Lọc theo khoảng thời gian tạo (created_at BETWEEN from AND to).
         * Từng đầu có thể null (open-ended range).
         */
        public static Specification<RequestEntity> withCreatedBetween(
                LocalDateTime from,
                LocalDateTime to
        ) {
            return (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (from != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
                }
                if (to != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
                }

                // Nếu cả hai đều null → conjunction (không filter)
                return predicates.isEmpty()
                        ? cb.conjunction()
                        : cb.and(predicates.toArray(new Predicate[0]));
            };
        }
    }
}
