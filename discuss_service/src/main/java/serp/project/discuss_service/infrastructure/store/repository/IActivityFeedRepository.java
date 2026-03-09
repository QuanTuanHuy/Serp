/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity feed repository
 */

package serp.project.discuss_service.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.discuss_service.core.domain.enums.ActionType;
import serp.project.discuss_service.infrastructure.store.model.ActivityFeedModel;

import java.util.List;

@Repository
public interface IActivityFeedRepository extends IBaseRepository<ActivityFeedModel> {

    Page<ActivityFeedModel> findByUserIdOrderByOccurredAtDesc(Long userId, Pageable pageable);

    List<ActivityFeedModel> findByUserIdAndIsReadFalseOrderByOccurredAtDesc(Long userId);

    List<ActivityFeedModel> findByUserIdAndActionType(Long userId, ActionType actionType, Pageable pageable);

    List<ActivityFeedModel> findByEntityTypeAndEntityId(String entityType, Long entityId);

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE ActivityFeedModel a SET a.isRead = true, a.readAt = :readAt WHERE a.userId = :userId AND a.isRead = false")
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") Long readAt);

    @Modifying
    @Query("UPDATE ActivityFeedModel a SET a.isRead = true, a.readAt = :readAt WHERE a.id IN :ids")
    int markAsRead(@Param("ids") List<Long> ids, @Param("readAt") Long readAt);

    @Modifying
    @Query("DELETE FROM ActivityFeedModel a WHERE a.occurredAt < :before")
    int deleteOldActivities(@Param("before") Long before);
}
