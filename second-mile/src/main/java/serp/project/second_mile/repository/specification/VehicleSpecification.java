/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository.specification;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubStaff;
import serp.project.second_mile.domain.Vehicle;
import serp.project.second_mile.dto.request.VehicleFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class VehicleSpecification {
    private VehicleSpecification() {
    }

    public static Specification<Vehicle> byFilter(Long tenantId, VehicleFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (hasText(filterRequest.getKeyword())) {
                String keywordPattern = toLikePattern(filterRequest.getKeyword());
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("licensePlate")),
                        keywordPattern
                ));
            }

            if (hasText(filterRequest.getLicensePlate())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("licensePlate")),
                        toLikePattern(filterRequest.getLicensePlate())
                ));
            }

            if (filterRequest.getVehicleType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleType"), filterRequest.getVehicleType()));
            }

            if (filterRequest.getHubId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("hubId"), filterRequest.getHubId()));
            }

            if (hasText(filterRequest.getHubKeyword())) {
                String hubKeywordPattern = toLikePattern(filterRequest.getHubKeyword());
                Subquery<Long> hubSubquery = query.subquery(Long.class);
                Root<Hub> hubRoot = hubSubquery.from(Hub.class);
                hubSubquery.select(hubRoot.get("id"))
                        .where(
                                criteriaBuilder.equal(hubRoot.get("tenantId"), tenantId),
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(hubRoot.get("code")),
                                                hubKeywordPattern
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(hubRoot.get("name")),
                                                hubKeywordPattern
                                        )
                                )
                        );
                predicates.add(root.get("hubId").in(hubSubquery));
            }

            if (filterRequest.getAssignedStaffId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedStaffId"), filterRequest.getAssignedStaffId()));
            }

            if (hasText(filterRequest.getDriverKeyword())) {
                String driverKeywordPattern = toLikePattern(filterRequest.getDriverKeyword());
                Subquery<Long> driverSubquery = query.subquery(Long.class);
                Root<HubStaff> driverRoot = driverSubquery.from(HubStaff.class);
                driverSubquery.select(driverRoot.get("id"))
                        .where(
                                criteriaBuilder.equal(driverRoot.get("tenantId"), tenantId),
                                criteriaBuilder.or(
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(driverRoot.get("code")),
                                                driverKeywordPattern
                                        ),
                                        criteriaBuilder.like(
                                                criteriaBuilder.lower(driverRoot.get("fullName")),
                                                driverKeywordPattern
                                        )
                                )
                        );
                predicates.add(root.get("assignedStaffId").in(driverSubquery));
            }

            if (filterRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String toLikePattern(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }
}

