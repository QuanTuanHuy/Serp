/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Storage provider enumeration
 */


package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    ONLINE("Online", true),
    BUSY("Do Not Disturb", true),
    OFFLINE("Offline", false);
    
    private final String displayName;
    private final boolean isAvailable;

    public boolean shouldReceiveNotifications() {
        return this != BUSY && this != OFFLINE;
    }

    public static UserStatus fromString(String status) {
        if (status == null) {
            return OFFLINE;
        }
        for (UserStatus userStatus : UserStatus.values()) {
            if (userStatus.name().equalsIgnoreCase(status)) {
                return userStatus;
            }
        }
        return OFFLINE;
    }
}
