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
import serp.project.pmcore.infrastructure.store.model.IssueTypeSchemeModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIssueTypeSchemeRepository extends JpaRepository<IssueTypeSchemeModel, Long> {

    Optional<IssueTypeSchemeModel> findByIdAndTenantId(Long id, Long tenantId);

    List<IssueTypeSchemeModel> findAllByTenantIdOrderByCreatedAtDesc(Long tenantId);

    boolean existsByTenantIdAndName(Long tenantId, String name);

    @Modifying
    @Query("UPDATE IssueTypeSchemeModel i SET i.deletedAt = CURRENT_TIMESTAMP WHERE i.id = :id AND i.tenantId = :tenantId AND i.deletedAt IS NULL")
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
