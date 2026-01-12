/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Message JPA model
 */

package serp.project.discuss_service.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import serp.project.discuss_service.core.domain.enums.MessageType;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "messages", indexes = {
    @Index(name = "idx_messages_channel_created", columnList = "channel_id, created_at DESC"),
    @Index(name = "idx_messages_parent", columnList = "parent_id"),
    @Index(name = "idx_messages_sender", columnList = "sender_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class MessageModel extends BaseModel {

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "mentions", columnDefinition = "BIGINT[]")
    private Long[] mentions;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "thread_count", nullable = false)
    private Integer threadCount = 0;

    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;

    @Column(name = "edited_at")
    private Long editedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Long deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reactions", columnDefinition = "jsonb")
    private List<Map<String, Object>> reactions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    // Full-text search vector (generated column - read only)
    @Column(name = "search_vector", insertable = false, updatable = false)
    private String searchVector;
}
