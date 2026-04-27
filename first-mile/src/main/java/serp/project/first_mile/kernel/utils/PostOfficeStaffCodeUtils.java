/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kernel.utils;

import serp.project.first_mile.enums.PostOfficeStaffRole;

public final class PostOfficeStaffCodeUtils {

    private static final String STAFF_CODE_PREFIX = "USR_";

    private PostOfficeStaffCodeUtils() {
    }

    public static String buildStaffCode(Long userId, PostOfficeStaffRole role) {
        return STAFF_CODE_PREFIX + userId + "_" + role.name();
    }
}