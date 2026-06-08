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
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarSlotModel;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IResourceCalendarSlotRepository extends JpaRepository<ResourceCalendarSlotModel, Long> {
    @Query("""
            SELECT s FROM ResourceCalendarSlotModel s
            WHERE s.tenantId = :tenantId
              AND s.userId IN :userIds
              AND s.slotStart < :planningEnd
              AND s.slotEnd > :planningStart
            ORDER BY s.userId ASC, s.slotStart ASC, s.id ASC
            """)
    List<ResourceCalendarSlotModel> findOverlappingSlots(@Param("tenantId") Long tenantId,
                                                         @Param("userIds") List<Long> userIds,
                                                         @Param("planningStart") LocalDateTime planningStart,
                                                         @Param("planningEnd") LocalDateTime planningEnd);

    @Modifying
    @Query("""
            DELETE FROM ResourceCalendarSlotModel s
            WHERE s.tenantId = :tenantId
              AND s.userId IN :userIds
              AND s.slotStart < :windowEnd
              AND s.slotEnd > :windowStart
              AND s.source IN :sources
            """)
    void hardDeleteGeneratedSlots(@Param("tenantId") Long tenantId,
                                  @Param("userIds") List<Long> userIds,
                                  @Param("windowStart") LocalDateTime windowStart,
                                  @Param("windowEnd") LocalDateTime windowEnd,
                                  @Param("sources") List<String> sources);
}
