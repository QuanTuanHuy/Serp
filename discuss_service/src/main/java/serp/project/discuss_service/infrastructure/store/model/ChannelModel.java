/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel JPA model
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
import serp.project.discuss_service.core.domain.enums.ChannelType;

import java.util.Map;

@Entity
@Table(name = "channels", indexes = {
    @Index(name = "idx_channels_tenant_type", columnList = "tenant_id, type"),
    @Index(name = "idx_channels_entity", columnList = "entity_type, entity_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ChannelModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ChannelType type;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @Column(name = "is_archived", nullable = false)
    private Boolean isArchived = false;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0;

    @Column(name = "message_count", nullable = false)
    private Integer messageCount = 0;

    @Column(name = "last_message_at")
    private Long lastMessageAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
