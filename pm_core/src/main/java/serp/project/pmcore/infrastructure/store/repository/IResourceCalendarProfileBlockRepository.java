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
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarProfileBlockModel;

import java.util.List;

@Repository
public interface IResourceCalendarProfileBlockRepository extends JpaRepository<ResourceCalendarProfileBlockModel, Long> {
    List<ResourceCalendarProfileBlockModel> findByProfileIdOrderByDayOfWeekAscStartTimeAsc(Long profileId);

    @Modifying
    @Query("""
            UPDATE ResourceCalendarProfileBlockModel b
            SET b.deletedAt = CURRENT_TIMESTAMP
            WHERE b.profileId = :profileId
              AND b.deletedAt IS NULL
            """)
    void deleteByProfileId(@Param("profileId") Long profileId);
}
