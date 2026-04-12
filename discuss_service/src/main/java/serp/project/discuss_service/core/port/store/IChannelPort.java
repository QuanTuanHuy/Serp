/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel port interface
 */

package serp.project.discuss_service.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;

import java.util.List;
import java.util.Optional;

public interface IChannelPort {

    ChannelEntity save(ChannelEntity channel);

    Optional<ChannelEntity> findById(Long id);

    List<ChannelEntity> findByIds(List<Long> ids);

    List<ChannelEntity> findByTenantId(Long tenantId);

    Pair<Long, List<ChannelEntity>> findByTenantIdPaginated(Long tenantId, int page, int size);

    Pair<Long, List<ChannelEntity>> findUserChannelsPaginated(Long userId, Long tenantId, int page, int size,
            ChannelType type, Boolean isArchived, String entityType, Long entityId, String searchQuery);

    List<ChannelEntity> findByTenantIdAndType(Long tenantId, ChannelType type);

    Optional<ChannelEntity> findDirectChannel(Long tenantId, Long userId1, Long userId2);

    Optional<ChannelEntity> findByEntity(Long tenantId, String entityType, Long entityId);

    long countByTenantId(Long tenantId);

    void deleteById(Long id);
}
