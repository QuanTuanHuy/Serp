/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel repository
 */

package serp.project.discuss_service.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.infrastructure.store.model.ChannelModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IChannelRepository extends IBaseRepository<ChannelModel> {

    // Find by tenant
    List<ChannelModel> findByTenantIdAndIsArchivedFalse(Long tenantId);

    Page<ChannelModel> findByTenantIdAndIsArchivedFalse(Long tenantId, Pageable pageable);

    // Find by type
    List<ChannelModel> findByTenantIdAndTypeAndIsArchivedFalse(Long tenantId, ChannelType type);

    // Find DIRECT channel between two users
    @Query("SELECT c FROM ChannelModel c WHERE c.tenantId = :tenantId AND c.type = 'DIRECT' " +
           "AND c.createdBy = :userId1 AND c.entityId = :userId2")
    Optional<ChannelModel> findDirectChannel(@Param("tenantId") Long tenantId,
                                             @Param("userId1") Long userId1,
                                             @Param("userId2") Long userId2);

    // Find TOPIC channel by entity
    Optional<ChannelModel> findByTenantIdAndEntityTypeAndEntityId(Long tenantId, String entityType, Long entityId);

    // Find by IDs
    List<ChannelModel> findByIdIn(List<Long> ids);

    // Count channels by tenant
    long countByTenantIdAndIsArchivedFalse(Long tenantId);
}
