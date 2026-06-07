/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.RepTimeBlockModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepTimeBlockRepository extends JpaRepository<RepTimeBlockModel, Long> {

    Optional<RepTimeBlockModel> findByActivityIdAndTenantId(Long activityId, Long tenantId);

    void deleteByActivityIdAndTenantId(Long activityId, Long tenantId);

    @Query("""
            SELECT COUNT(b) FROM RepTimeBlockModel b
            WHERE b.tenantId = :tenantId
              AND b.teamMemberId = :teamMemberId
              AND b.startTime < :endTime
              AND b.endTime > :startTime
            """)
    long countConflicts(@Param("teamMemberId") Long teamMemberId,
                        @Param("tenantId") Long tenantId,
                        @Param("startTime") Long startTime,
                        @Param("endTime") Long endTime);

    List<RepTimeBlockModel> findByTeamMemberIdAndTenantIdAndEndTimeGreaterThan(Long teamMemberId, Long tenantId, Long fromTime);
}
