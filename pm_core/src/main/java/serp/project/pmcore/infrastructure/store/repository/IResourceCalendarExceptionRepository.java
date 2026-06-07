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
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarExceptionModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IResourceCalendarExceptionRepository extends JpaRepository<ResourceCalendarExceptionModel, Long> {
    Optional<ResourceCalendarExceptionModel> findByIdAndTenantId(Long id, Long tenantId);

    List<ResourceCalendarExceptionModel> findByTenantIdAndUserIdInAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
            Long tenantId,
            List<Long> userIds,
            LocalDateTime windowEnd,
            LocalDateTime windowStart
    );

    @Modifying
    @Query("""
            UPDATE ResourceCalendarExceptionModel e
            SET e.deletedAt = CURRENT_TIMESTAMP
            WHERE e.id = :id
              AND e.tenantId = :tenantId
              AND e.deletedAt IS NULL
            """)
    void deleteByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);
}
