/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User presence response DTO
 */


package serp.project.discuss_service.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.domain.enums.UserStatus;

import java.time.Duration;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserPresenceResponse {
    private Long userId;
    private String userName;
    private String avatarUrl;
    private UserStatus status;
    private String statusMessage;
    private Long lastSeenAt;
    private boolean isOnline;

    private String lastSeenText;

    public static UserPresenceResponse fromEntity(UserPresenceEntity presence, String userName, String avatarUrl) {
        return UserPresenceResponse.builder()
                .userId(presence.getUserId())
                .userName(userName)
                .avatarUrl(avatarUrl)
                .status(presence.getStatus())
                .statusMessage(presence.getStatusMessage())
                .lastSeenAt(presence.getLastSeenAt())
                .isOnline(presence.isOnline())
                .lastSeenText(formatLastSeen(presence.getLastSeenAt(), presence.isOnline()))
                .build();
    }


    public static String formatLastSeen(long lastSeenAt, boolean isOnline) {
        Instant lastSeen = lastSeenAt == 0 ? null : Instant.ofEpochMilli(lastSeenAt);
        if (isOnline) {
            return "Active now";
        }

        if (lastSeen == null) {
            return "Unknown";
        }

        Duration duration = Duration.between(lastSeen, Instant.now());

        if (duration.toMinutes() < 1) {
            return "Just now";
        } else if (duration.toMinutes() < 60) {
            long minutes = duration.toMinutes();
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (duration.toHours() < 24) {
            long hours = duration.toHours();
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (duration.toDays() < 7) {
            long days = duration.toDays();
            return days == 1 ? "Yesterday" : days + " days ago";
        } else {
            return lastSeen.toString().substring(0, 10);
        }
    }
}
