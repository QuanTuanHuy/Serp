/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import org.springframework.stereotype.Repository;
import serp.project.account.infrastructure.store.model.UserModuleAccessModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUserModuleAccessRepository extends IBaseRepository<UserModuleAccessModel> {
    Optional<UserModuleAccessModel> findByUserIdAndModuleIdAndOrganizationId(
            Long userId, Long moduleId, Long organizationId);
    
    List<UserModuleAccessModel> findByUserId(Long userId);
    
    List<UserModuleAccessModel> findByUserIdAndOrganizationId(Long userId, Long organizationId);
    
    boolean existsByUserIdAndModuleIdAndOrganizationIdAndIsActive(
            Long userId, Long moduleId, Long organizationId, Boolean isActive);
}
