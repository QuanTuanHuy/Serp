/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel member JPA model
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
import serp.project.discuss_service.core.domain.enums.MemberRole;
import serp.project.discuss_service.core.domain.enums.MemberStatus;
import serp.project.discuss_service.core.domain.enums.NotificationLevel;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "channel_members", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_channel_members", columnNames = {"channel_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_channel_members_user", columnList = "user_id, status"),
        @Index(name = "idx_channel_members_channel", columnList = "channel_id, status")
    }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class ChannelMemberModel extends BaseModel {

    @Column(name = "channel_id", nullable = false)
    private Long channelId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "removed_by")
    private Long removedBy;

    @Column(name = "last_read_msg_id")
    private Long lastReadMsgId;

    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;

    @Column(name = "is_muted", nullable = false)
    private Boolean isMuted = false;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "notification_level", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationLevel notificationLevel = NotificationLevel.ALL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
