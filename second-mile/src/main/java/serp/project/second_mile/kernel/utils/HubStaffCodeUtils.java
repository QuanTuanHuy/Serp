/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.utils;

import serp.project.second_mile.enums.HubStaffRole;

public final class HubStaffCodeUtils {
    private static final String HUB_STAFF_CODE_PREFIX = "USR_";

    private HubStaffCodeUtils() {
    }

    public static String buildHubStaffCode(Long userId, HubStaffRole role) {
        return HUB_STAFF_CODE_PREFIX + userId + "_" + role.name();
    }
}
