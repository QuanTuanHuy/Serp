/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member adapter implementation
 */

package serp.project.discuss_service.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.ChannelMemberEntity;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.port.store.IChannelMemberPort;
import serp.project.discuss_service.infrastructure.store.mapper.ChannelMemberMapper;
import serp.project.discuss_service.infrastructure.store.model.ChannelMemberModel;
import serp.project.discuss_service.infrastructure.store.repository.IChannelMemberRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChannelMemberAdapter implements IChannelMemberPort {

    private final IChannelMemberRepository channelMemberRepository;
    private final ChannelMemberMapper channelMemberMapper;

    @Override
    public ChannelMemberEntity save(ChannelMemberEntity member) {
        ChannelMemberModel model = channelMemberMapper.toModel(member);
        ChannelMemberModel saved = channelMemberRepository.save(model);
        return channelMemberMapper.toEntity(saved);
    }

    @Override
    public Optional<ChannelMemberEntity> findByChannelIdAndUserId(Long channelId, Long userId) {
        return channelMemberRepository.findByChannelIdAndUserId(channelId, userId)
                .map(channelMemberMapper::toEntity);
    }

    @Override
    public List<ChannelMemberEntity> findByChannelIdAndStatus(Long channelId, MemberStatus status) {
        return channelMemberMapper.toEntityList(
                channelMemberRepository.findByChannelIdAndStatus(channelId, status));
    }

    @Override
    public List<ChannelMemberEntity> findByChannelId(Long channelId) {
        return channelMemberMapper.toEntityList(
                channelMemberRepository.findByChannelId(channelId));
    }

    @Override
    public List<ChannelMemberEntity> findByUserIdAndStatus(Long userId, MemberStatus status) {
        return channelMemberMapper.toEntityList(
                channelMemberRepository.findByUserIdAndStatus(userId, status));
    }

    @Override
    public List<ChannelMemberEntity> findChannelsWithUnread(Long userId) {
        return channelMemberMapper.toEntityList(
                channelMemberRepository.findChannelsWithUnread(userId));
    }

    @Override
    public List<ChannelMemberEntity> findPinnedChannels(Long userId) {
        return channelMemberMapper.toEntityList(
                channelMemberRepository.findByUserIdAndIsPinnedTrueAndStatus(userId, MemberStatus.ACTIVE));
    }

    @Override
    public long countActiveMembers(Long channelId) {
        return channelMemberRepository.countByChannelIdAndStatus(channelId, MemberStatus.ACTIVE);
    }

    @Override
    public boolean isMember(Long channelId, Long userId) {
        return channelMemberRepository.existsByChannelIdAndUserIdAndStatus(channelId, userId, MemberStatus.ACTIVE);
    }

    @Override
    public int incrementUnreadForChannel(Long channelId, Long senderId) {
        return channelMemberRepository.incrementUnreadForChannel(channelId, senderId);
    }

    @Override
    public int markAsRead(Long channelId, Long userId, Long messageId) {
        return channelMemberRepository.markAsRead(channelId, userId, messageId);
    }

    @Override
    public void delete(ChannelMemberEntity member) {
        channelMemberRepository.deleteById(member.getId());
    }

    @Override
    public void deleteByChannelId(Long channelId) {
        channelMemberRepository.deleteByChannelId(channelId);
    }
}
