/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;
import java.util.List;

public class DynamicSpecification<T> extends BaseSpecification<T> {
    
    public static <T> Specification<T> buildSpecification(List<SearchCriteria> criteriaList) {
        if (criteriaList == null || criteriaList.isEmpty()) {
            return alwaysTrue();
        }
        
        Specification<T> result = alwaysTrue();
        
        for (SearchCriteria criteria : criteriaList) {
            result = result.and(buildSpecification(criteria));
        }
        
        return result;
    }
    
    @SuppressWarnings({"rawtypes"})
    public static <T> Specification<T> buildSpecification(SearchCriteria criteria) {
        return switch (criteria.getOperation()) {
            case EQUAL -> equal(criteria.getField(), criteria.getValue());
            case NOT_EQUAL -> notEqual(criteria.getField(), criteria.getValue());
            case LIKE -> like(criteria.getField(), (String) criteria.getValue());
            case IN -> in(criteria.getField(), criteria.getValues());
            case NOT_IN -> notIn(criteria.getField(), criteria.getValues());
            case GREATER_THAN -> greaterThan(criteria.getField(), (Comparable) criteria.getValue());
            case GREATER_THAN_OR_EQUAL -> greaterThanOrEqual(criteria.getField(), (Comparable) criteria.getValue());
            case LESS_THAN -> lessThan(criteria.getField(), (Comparable) criteria.getValue());
            case LESS_THAN_OR_EQUAL -> lessThanOrEqual(criteria.getField(), (Comparable) criteria.getValue());
            case BETWEEN -> between(criteria.getField(),
                    (Comparable) criteria.getFromValue(),
                    (Comparable) criteria.getToValue());
            case IS_NULL -> isNull(criteria.getField());
            case IS_NOT_NULL -> isNotNull(criteria.getField());
        };
    }
}
