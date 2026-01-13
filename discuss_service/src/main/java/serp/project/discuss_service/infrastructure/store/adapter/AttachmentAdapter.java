/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment adapter implementation
 */

package serp.project.discuss_service.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.AttachmentEntity;
import serp.project.discuss_service.core.port.store.IAttachmentPort;
import serp.project.discuss_service.infrastructure.store.mapper.AttachmentMapper;
import serp.project.discuss_service.infrastructure.store.model.AttachmentModel;
import serp.project.discuss_service.infrastructure.store.repository.IAttachmentRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttachmentAdapter implements IAttachmentPort {

    private final IAttachmentRepository attachmentRepository;
    private final AttachmentMapper attachmentMapper;

    @Override
    public AttachmentEntity save(AttachmentEntity attachment) {
        AttachmentModel model = attachmentMapper.toModel(attachment);
        AttachmentModel saved = attachmentRepository.save(model);
        return attachmentMapper.toEntity(saved);
    }

    @Override
    public List<AttachmentEntity> saveAll(List<AttachmentEntity> attachments) {
        List<AttachmentModel> models = attachmentMapper.toModelList(attachments);
        List<AttachmentModel> saved = attachmentRepository.saveAll(models);
        return attachmentMapper.toEntityList(saved);
    }

    @Override
    public Optional<AttachmentEntity> findById(Long id) {
        return attachmentRepository.findById(id)
                .map(attachmentMapper::toEntity);
    }

    @Override
    public List<AttachmentEntity> findByMessageId(Long messageId) {
        return attachmentMapper.toEntityList(
                attachmentRepository.findByMessageId(messageId));
    }

    @Override
    public List<AttachmentEntity> findByMessageIds(List<Long> messageIds) {
        return attachmentMapper.toEntityList(
                attachmentRepository.findByMessageIdIn(messageIds));
    }

    @Override
    public List<AttachmentEntity> findByChannelId(Long channelId) {
        return attachmentMapper.toEntityList(
                attachmentRepository.findByChannelId(channelId));
    }

    @Override
    public long countByChannelId(Long channelId) {
        return attachmentRepository.countByChannelId(channelId);
    }

    @Override
    public void deleteById(Long id) {
        attachmentRepository.deleteById(id);
    }

    @Override
    public void deleteByMessageId(Long messageId) {
        attachmentRepository.deleteByMessageId(messageId);
    }
}
