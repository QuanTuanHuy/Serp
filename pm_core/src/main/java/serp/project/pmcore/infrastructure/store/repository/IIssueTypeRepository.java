/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.IssueTypeModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIssueTypeRepository extends JpaRepository<IssueTypeModel, Long> {

    Optional<IssueTypeModel> findByIdAndTenantId(Long id, Long tenantId);

    List<IssueTypeModel> findAllByTenantIdOrderByHierarchyLevelAsc(Long tenantId);

    boolean existsByTenantIdAndTypeKey(Long tenantId, String typeKey);

    @Modifying
    @Query("UPDATE IssueTypeModel i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.id = :id AND i.tenantId = :tenantId AND i.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
