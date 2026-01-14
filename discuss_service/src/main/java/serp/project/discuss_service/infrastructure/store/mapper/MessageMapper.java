/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message entity/model mapper
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.entity.MessageEntity;
import serp.project.discuss_service.infrastructure.store.model.MessageModel;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MessageMapper extends BaseMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageEntity toEntity(MessageModel model) {
        if (model == null) {
            return null;
        }

        return MessageEntity.builder()
                .id(model.getId())
                .channelId(model.getChannelId())
                .senderId(model.getSenderId())
                .tenantId(model.getTenantId())
                .content(model.getContent())
                .messageType(model.getMessageType())
                .mentions(arrayToList(model.getMentions()))
                .parentId(model.getParentId())
                .threadCount(model.getThreadCount())
                .isEdited(model.getIsEdited())
                .editedAt(localDateTimeToLong(model.getEditedAt()))
                .isDeleted(model.getIsDeleted())
                .deletedAt(localDateTimeToLong(model.getDeletedAt()))
                .deletedBy(model.getDeletedBy())
                .reactions(parseReactions(model.getReactions()))
                .metadata(model.getMetadata())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public MessageModel toModel(MessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return MessageModel.builder()
                .id(entity.getId())
                .channelId(entity.getChannelId())
                .senderId(entity.getSenderId())
                .tenantId(entity.getTenantId())
                .content(entity.getContent())
                .messageType(entity.getMessageType())
                .mentions(listToArray(entity.getMentions()))
                .parentId(entity.getParentId())
                .threadCount(entity.getThreadCount())
                .isEdited(entity.getIsEdited())
                .editedAt(longToLocalDateTime(entity.getEditedAt()))
                .isDeleted(entity.getIsDeleted())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt()))
                .deletedBy(entity.getDeletedBy())
                .reactions(serializeReactions(entity.getReactions()))
                .metadata(entity.getMetadata())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<MessageEntity> toEntityList(List<MessageModel> models) {
        if (models == null) {
            return null;
        }
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<MessageModel> toModelList(List<MessageEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    // Array <-> List conversion
    private List<Long> arrayToList(Long[] array) {
        if (array == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(array);
    }

    private Long[] listToArray(List<Long> list) {
        if (list == null) {
            return new Long[0];
        }
        return list.toArray(new Long[0]);
    }

    // Reaction conversion: List<ReactionVO> <-> List<Map<String, Object>>
    private List<MessageEntity.ReactionVO> parseReactions(List<Map<String, Object>> reactionsJson) {
        if (reactionsJson == null || reactionsJson.isEmpty()) {
            return new ArrayList<>();
        }

        List<MessageEntity.ReactionVO> reactions = new ArrayList<>();
        for (Map<String, Object> r : reactionsJson) {
            String emoji = (String) r.get("emoji");
            @SuppressWarnings("unchecked")
            List<Number> userIdNumbers = (List<Number>) r.get("userIds");
            List<Long> userIds = new ArrayList<>();
            if (userIdNumbers != null) {
                for (Number n : userIdNumbers) {
                    userIds.add(n.longValue());
                }
            }
            reactions.add(new MessageEntity.ReactionVO(emoji, userIds));
        }
        return reactions;
    }

    private List<Map<String, Object>> serializeReactions(List<MessageEntity.ReactionVO> reactions) {
        if (reactions == null || reactions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (MessageEntity.ReactionVO r : reactions) {
            Map<String, Object> map = new HashMap<>();
            map.put("emoji", r.getEmoji());
            map.put("userIds", r.getUserIds());
            result.add(map);
        }
        return result;
    }
}
