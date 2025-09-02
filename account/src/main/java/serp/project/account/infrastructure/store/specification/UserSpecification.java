/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;
import serp.project.account.infrastructure.store.model.UserModel;
import java.time.LocalDateTime;
import java.util.List;

public class UserSpecification extends BaseSpecification<UserModel> {
    
    public static Specification<UserModel> containsEmail(String email) {
        return like("email", email);
    }

    public static Specification<UserModel> containsName(String name) {
        return like("fullName", name);
    }
    
    public static Specification<UserModel> hasExactEmail(String email) {
        return equal("email", email);
    }
    
    public static Specification<UserModel> hasExactFullName(String fullName) {
        return equal("fullName", fullName);
    }
    
    public static Specification<UserModel> hasId(Long id) {
        return equal("id", id);
    }
    
    public static Specification<UserModel> hasIds(List<Long> ids) {
        return in("id", ids);
    }
    
    public static Specification<UserModel> createdBetween(LocalDateTime from, LocalDateTime to) {
        return dateTimeBetween("createdAt", from, to);
    }
    
    public static Specification<UserModel> createdAfter(LocalDateTime date) {
        return greaterThanOrEqual("createdAt", date);
    }
    
    public static Specification<UserModel> createdBefore(LocalDateTime date) {
        return lessThanOrEqual("createdAt", date);
    }
    
    public static Specification<UserModel> updatedAfter(LocalDateTime date) {
        return greaterThanOrEqual("updatedAt", date);
    }
    
    public static Specification<UserModel> updatedBefore(LocalDateTime date) {
        return lessThanOrEqual("updatedAt", date);
    }
    
    public static Specification<UserModel> searchUsersWithEmailOrName(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return alwaysTrue();
        }
        return or(
            containsEmail(searchTerm),
            containsName(searchTerm)
        );
    }
    
    public static Specification<UserModel> searchUsers(String email, String name, 
                                                      LocalDateTime createdFrom,
                                                      LocalDateTime createdTo,
                                                      LocalDateTime updatedFrom,
                                                      LocalDateTime updatedTo) {
        Specification<UserModel> spec = alwaysTrue();
        
        if (email != null && !email.trim().isEmpty()) {
            spec = spec.and(containsEmail(email));
        }
        
        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and(containsName(name));
        }
        
        if (createdFrom != null || createdTo != null) {
            spec = spec.and(createdBetween(createdFrom, createdTo));
        }
        
        if (updatedFrom != null || updatedTo != null) {
            spec = spec.and(dateTimeBetween("updatedAt", updatedFrom, updatedTo));
        }
        
        return spec;
    }
    
    public static Specification<UserModel> recentUsers(int days) {
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(days);
        return greaterThanOrEqual("createdAt", daysAgo);
    }
    
    public static Specification<UserModel> advancedSearch(String searchTerm, 
                                                         LocalDateTime fromDate,
                                                         LocalDateTime toDate,
                                                         List<Long> excludeIds) {
        Specification<UserModel> spec = alwaysTrue();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            spec = spec.and(searchUsersWithEmailOrName(searchTerm));
        }
        
        if (fromDate != null || toDate != null) {
            spec = spec.and(createdBetween(fromDate, toDate));
        }
        
        if (excludeIds != null && !excludeIds.isEmpty()) {
            spec = spec.and(notIn("id", excludeIds));
        }
        
        return spec;
    }
}
