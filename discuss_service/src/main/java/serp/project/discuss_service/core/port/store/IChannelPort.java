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

    /**
     * Save a channel (create or update)
     */
    ChannelEntity save(ChannelEntity channel);

    /**
     * Find channel by ID
     */
    Optional<ChannelEntity> findById(Long id);

    /**
     * Find channels by IDs
     */
    List<ChannelEntity> findByIds(List<Long> ids);

    /**
     * Find all active channels for a tenant
     */
    List<ChannelEntity> findByTenantId(Long tenantId);

    /**
     * Find channels by tenant with pagination
     */
    Pair<Long, List<ChannelEntity>> findByTenantIdPaginated(Long tenantId, int page, int size);

    /**
     * Find channels by type
     */
    List<ChannelEntity> findByTenantIdAndType(Long tenantId, ChannelType type);

    /**
     * Find DIRECT channel between two users
     */
    Optional<ChannelEntity> findDirectChannel(Long tenantId, Long userId1, Long userId2);

    /**
     * Find TOPIC channel by entity
     */
    Optional<ChannelEntity> findByEntity(Long tenantId, String entityType, Long entityId);

    /**
     * Count active channels for tenant
     */
    long countByTenantId(Long tenantId);

    /**
     * Delete channel by ID
     */
    void deleteById(Long id);
}
