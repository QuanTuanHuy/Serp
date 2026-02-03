/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Storage provider enumeration
 */


package serp.project.discuss_service.core.domain.entity;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.enums.UserStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserPresenceEntity {
    private Long userId;
    private Long tenantId;

    @Builder.Default
    private UserStatus status = UserStatus.OFFLINE;
    private String statusMessage;
    private Long lastSeenAt;
    private Long statusChangedAt;
    private Long lastHeartbeatAt;

    public static UserPresenceEntity online(Long userId, Long tenantId) {
        long now = Instant.now().toEpochMilli();
        return UserPresenceEntity.builder()
                .userId(userId)
                .tenantId(tenantId)
                .status(UserStatus.ONLINE)
                .lastSeenAt(now)
                .statusChangedAt(now)
                .lastHeartbeatAt(now)
                .build();
    }

    public static UserPresenceEntity offline(Long userId, Long tenantId) {
        long now = Instant.now().toEpochMilli();
        return UserPresenceEntity.builder()
                .userId(userId)
                .tenantId(tenantId)
                .status(UserStatus.OFFLINE)
                .lastSeenAt(now)
                .statusChangedAt(now)
                .lastHeartbeatAt(now)
                .build();
    }


    public void goOnline() {
        long now = Instant.now().toEpochMilli();
        this.status = UserStatus.ONLINE;
        this.lastSeenAt = now;
        this.statusChangedAt = now;
        this.lastHeartbeatAt = now;
    }

    public void goOffline() {
        long now = Instant.now().toEpochMilli();
        this.status = UserStatus.OFFLINE;
        this.lastSeenAt = now;
        this.statusChangedAt = now;
    }

    public void setCustomStatus(UserStatus status, String message) {
        long now = Instant.now().toEpochMilli();
        this.status = status;
        this.statusMessage = message;
        this.statusChangedAt = now;
    }

    public boolean isOnline() {
        return this.status != null && this.status.isAvailable();
    }
}
