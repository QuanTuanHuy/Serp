/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.shared.dto.user;

import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;

public record UserSummary(Long id, String displayName, String avatarUrl) {
    public static UserSummary from(UserProfileDto profile) {
        if (profile == null) {
            return null;
        }
        String displayName = profile.getFullName();
        if (displayName == null || displayName.isBlank()) {
            displayName = profile.getEmail();
        }
        return new UserSummary(profile.getId(), displayName, profile.getAvatarUrl());
    }

    public static UserSummary missing(Long id) {
        return new UserSummary(id, null, null);
    }
}
