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
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarAssignmentModel;

import java.util.List;

@Repository
public interface IResourceCalendarAssignmentRepository extends JpaRepository<ResourceCalendarAssignmentModel, Long> {
    List<ResourceCalendarAssignmentModel> findByTenantIdAndProfileIdOrderByUserIdAsc(Long tenantId, Long profileId);

    List<ResourceCalendarAssignmentModel> findByTenantIdOrderByUserIdAscEffectiveFromAsc(Long tenantId);

    @Modifying
    @Query("""
            UPDATE ResourceCalendarAssignmentModel a
            SET a.deletedAt = CURRENT_TIMESTAMP
            WHERE a.tenantId = :tenantId
              AND a.profileId = :profileId
              AND a.deletedAt IS NULL
            """)
    void deleteByTenantIdAndProfileId(@Param("tenantId") Long tenantId, @Param("profileId") Long profileId);
}
