/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Activity feed adapter implementation
 */

package serp.project.discuss_service.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ActivityFeedEntity;
import serp.project.discuss_service.core.domain.enums.ActionType;
import serp.project.discuss_service.core.port.store.IActivityFeedPort;
import serp.project.discuss_service.infrastructure.store.mapper.ActivityFeedMapper;
import serp.project.discuss_service.infrastructure.store.model.ActivityFeedModel;
import serp.project.discuss_service.infrastructure.store.repository.IActivityFeedRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ActivityFeedAdapter implements IActivityFeedPort {

    private final IActivityFeedRepository activityFeedRepository;
    private final ActivityFeedMapper activityFeedMapper;

    @Override
    public ActivityFeedEntity save(ActivityFeedEntity activity) {
        ActivityFeedModel model = activityFeedMapper.toModel(activity);
        ActivityFeedModel saved = activityFeedRepository.save(model);
        return activityFeedMapper.toEntity(saved);
    }

    @Override
    public List<ActivityFeedEntity> saveAll(List<ActivityFeedEntity> activities) {
        List<ActivityFeedModel> models = activityFeedMapper.toModelList(activities);
        List<ActivityFeedModel> saved = activityFeedRepository.saveAll(models);
        return activityFeedMapper.toEntityList(saved);
    }

    @Override
    public Optional<ActivityFeedEntity> findById(Long id) {
        return activityFeedRepository.findById(id)
                .map(activityFeedMapper::toEntity);
    }

    @Override
    public Pair<Long, List<ActivityFeedEntity>> findByUserId(Long userId, int page, int size) {
        var pageable = PageRequest.of(page, size);
        var pageResult = activityFeedRepository.findByUserIdOrderByOccurredAtDesc(userId, pageable);
        return Pair.of(
                pageResult.getTotalElements(),
                activityFeedMapper.toEntityList(pageResult.getContent())
        );
    }

    @Override
    public List<ActivityFeedEntity> findUnreadByUserId(Long userId) {
        return activityFeedMapper.toEntityList(
                activityFeedRepository.findByUserIdAndIsReadFalseOrderByOccurredAtDesc(userId));
    }

    @Override
    public List<ActivityFeedEntity> findByUserIdAndActionType(Long userId, ActionType actionType, int page, int size) {
        var pageable = PageRequest.of(page, size);
        return activityFeedMapper.toEntityList(
                activityFeedRepository.findByUserIdAndActionType(userId, actionType, pageable));
    }

    @Override
    public List<ActivityFeedEntity> findByEntity(String entityType, Long entityId) {
        return activityFeedMapper.toEntityList(
                activityFeedRepository.findByEntityTypeAndEntityId(entityType, entityId));
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return activityFeedRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public int markAllAsRead(Long userId) {
        return activityFeedRepository.markAllAsRead(userId, Instant.now().toEpochMilli());
    }

    @Override
    public int markAsRead(List<Long> activityIds) {
        return activityFeedRepository.markAsRead(activityIds, Instant.now().toEpochMilli());
    }

    @Override
    public int deleteOldActivities(Long before) {
        return activityFeedRepository.deleteOldActivities(before);
    }
}
