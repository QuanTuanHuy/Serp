package serp.project.payment_service.repository.specification;

import serp.project.payment_service.dto.transaction.TransactionHistoryFilterRequest;
import serp.project.payment_service.entity.ZaloPayTransaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification builder cho ZaloPayTransaction filtering
 */
public class TransactionSpecification {
    
    /**
     * Tạo Specification từ filter request
     * 
     * @param filter Filter request
     * @return Specification để query
     */
    public static Specification<ZaloPayTransaction> buildSpecification(TransactionHistoryFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter theo app_user
            if (filter.getAppUser() != null && !filter.getAppUser().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("appUser"), filter.getAppUser().trim()));
            }
            
            // Filter theo status
            if (filter.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }
            
            // Filter theo start date
            if (filter.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
            }
            
            // Filter theo end date
            if (filter.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
            }
            
            // Filter theo min amount
            if (filter.getMinAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filter.getMinAmount()));
            }
            
            // Filter theo max amount
            if (filter.getMaxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filter.getMaxAmount()));
            }
            
            // Filter theo bank code
            if (filter.getBankCode() != null && !filter.getBankCode().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("bankCode"), filter.getBankCode().trim()));
            }
            
            // Filter theo search keyword (tìm trong description hoặc title)
            if (filter.getSearchKeyword() != null && !filter.getSearchKeyword().trim().isEmpty()) {
                String keyword = "%" + filter.getSearchKeyword().trim().toLowerCase() + "%";
                Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), keyword);
                Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), keyword);
                predicates.add(criteriaBuilder.or(descriptionPredicate, titlePredicate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
