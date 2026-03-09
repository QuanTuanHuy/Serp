/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity feed port interface
 */

package serp.project.discuss_service.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.discuss_service.core.domain.entity.ActivityFeedEntity;
import serp.project.discuss_service.core.domain.enums.ActionType;

import java.util.List;
import java.util.Optional;

public interface IActivityFeedPort {

    /**
     * Save an activity (create or update)
     */
    ActivityFeedEntity save(ActivityFeedEntity activity);

    /**
     * Save multiple activities
     */
    List<ActivityFeedEntity> saveAll(List<ActivityFeedEntity> activities);

    /**
     * Find activity by ID
     */
    Optional<ActivityFeedEntity> findById(Long id);

    /**
     * Find activities for user with pagination
     */
    Pair<Long, List<ActivityFeedEntity>> findByUserId(Long userId, int page, int size);

    /**
     * Find unread activities for user
     */
    List<ActivityFeedEntity> findUnreadByUserId(Long userId);

    /**
     * Find activities by type for user
     */
    List<ActivityFeedEntity> findByUserIdAndActionType(Long userId, ActionType actionType, int page, int size);

    /**
     * Find activities for an entity
     */
    List<ActivityFeedEntity> findByEntity(String entityType, Long entityId);

    /**
     * Count unread activities for user
     */
    long countUnreadByUserId(Long userId);

    /**
     * Mark all activities as read for user
     */
    int markAllAsRead(Long userId);

    /**
     * Mark specific activities as read
     */
    int markAsRead(List<Long> activityIds);

    /**
     * Delete old activities (for cleanup)
     */
    int deleteOldActivities(Long before);
}
