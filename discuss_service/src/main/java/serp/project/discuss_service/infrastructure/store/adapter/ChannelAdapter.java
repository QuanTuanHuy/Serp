/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel adapter implementation
 */

package serp.project.discuss_service.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ChannelEntity;
import serp.project.discuss_service.core.domain.enums.ChannelType;
import serp.project.discuss_service.core.port.store.IChannelPort;
import serp.project.discuss_service.infrastructure.store.mapper.ChannelMapper;
import serp.project.discuss_service.infrastructure.store.model.ChannelModel;
import serp.project.discuss_service.infrastructure.store.repository.IChannelRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChannelAdapter implements IChannelPort {

    private final IChannelRepository channelRepository;
    private final ChannelMapper channelMapper;

    @Override
    public ChannelEntity save(ChannelEntity channel) {
        ChannelModel model = channelMapper.toModel(channel);
        ChannelModel saved = channelRepository.save(model);
        return channelMapper.toEntity(saved);
    }

    @Override
    public Optional<ChannelEntity> findById(Long id) {
        return channelRepository.findById(id)
                .map(channelMapper::toEntity);
    }

    @Override
    public List<ChannelEntity> findByIds(List<Long> ids) {
        return channelMapper.toEntityList(channelRepository.findByIdIn(ids));
    }

    @Override
    public List<ChannelEntity> findByTenantId(Long tenantId) {
        return channelMapper.toEntityList(
                channelRepository.findByTenantIdAndIsArchivedFalse(tenantId));
    }

    @Override
    public Pair<Long, List<ChannelEntity>> findByTenantIdPaginated(Long tenantId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastMessageAt"));
        var pageResult = channelRepository.findByTenantIdAndIsArchivedFalse(tenantId, pageable);
        return Pair.of(
                pageResult.getTotalElements(),
                channelMapper.toEntityList(pageResult.getContent())
        );
    }

    @Override
    public List<ChannelEntity> findByTenantIdAndType(Long tenantId, ChannelType type) {
        return channelMapper.toEntityList(
                channelRepository.findByTenantIdAndTypeAndIsArchivedFalse(tenantId, type));
    }

    @Override
    public Optional<ChannelEntity> findDirectChannel(Long tenantId, Long userId1, Long userId2) {
        // Ensure consistent ordering
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        
        return channelRepository.findDirectChannel(tenantId, smallerId, largerId)
                .map(channelMapper::toEntity);
    }

    @Override
    public Optional<ChannelEntity> findByEntity(Long tenantId, String entityType, Long entityId) {
        return channelRepository.findByTenantIdAndEntityTypeAndEntityId(tenantId, entityType, entityId)
                .map(channelMapper::toEntity);
    }

    @Override
    public long countByTenantId(Long tenantId) {
        return channelRepository.countByTenantIdAndIsArchivedFalse(tenantId);
    }

    @Override
    public void deleteById(Long id) {
        channelRepository.deleteById(id);
    }
}
