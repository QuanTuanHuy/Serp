/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository.specification;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.dto.request.HubFilterRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class HubSpecification {

    private HubSpecification() {
    }

    public static Specification<Hub> byFilter(Long tenantId, HubFilterRequest filterRequest) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("tenantId"), tenantId));

            if (hasText(filterRequest.getKeyword())) {
                String keywordPattern = toLikePattern(filterRequest.getKeyword());
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("code")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keywordPattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("addressDetail")), keywordPattern)
                ));
            }

            if (hasText(filterRequest.getCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        toLikePattern(filterRequest.getCode())
                ));
            }

            if (hasText(filterRequest.getName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        toLikePattern(filterRequest.getName())
                ));
            }

            if (hasText(filterRequest.getProvinceCode())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("provinceCode")),
                        filterRequest.getProvinceCode().trim().toLowerCase(Locale.ROOT)
                ));
            }

            if (hasText(filterRequest.getWardCode())) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("wardCode")),
                        filterRequest.getWardCode().trim().toLowerCase(Locale.ROOT)
                ));
            }

            if (filterRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filterRequest.getStatus()));
            }

            if (filterRequest.getHasLocation() != null) {
                predicates.add(Boolean.TRUE.equals(filterRequest.getHasLocation())
                        ? criteriaBuilder.isNotNull(root.get("location"))
                        : criteriaBuilder.isNull(root.get("location")));
            }

            if (filterRequest.getMinLatitude() != null) {
                predicates.add(criteriaBuilder.ge(root.get("locationLatitude"), filterRequest.getMinLatitude()));
            }
            if (filterRequest.getMaxLatitude() != null) {
                predicates.add(criteriaBuilder.le(root.get("locationLatitude"), filterRequest.getMaxLatitude()));
            }
            if (filterRequest.getMinLongitude() != null) {
                predicates.add(criteriaBuilder.ge(root.get("locationLongitude"), filterRequest.getMinLongitude()));
            }
            if (filterRequest.getMaxLongitude() != null) {
                predicates.add(criteriaBuilder.le(root.get("locationLongitude"), filterRequest.getMaxLongitude()));
            }

            if (filterRequest.getMinDailyCapacity() != null) {
                predicates.add(criteriaBuilder.ge(root.get("dailyCapacity"), filterRequest.getMinDailyCapacity()));
            }
            if (filterRequest.getMaxDailyCapacity() != null) {
                predicates.add(criteriaBuilder.le(root.get("dailyCapacity"), filterRequest.getMaxDailyCapacity()));
            }

            if (filterRequest.getMinCurrentLoad() != null) {
                predicates.add(criteriaBuilder.ge(root.get("currentLoad"), filterRequest.getMinCurrentLoad()));
            }
            if (filterRequest.getMaxCurrentLoad() != null) {
                predicates.add(criteriaBuilder.le(root.get("currentLoad"), filterRequest.getMaxCurrentLoad()));
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
