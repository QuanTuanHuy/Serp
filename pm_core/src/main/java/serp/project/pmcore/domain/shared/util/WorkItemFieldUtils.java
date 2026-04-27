/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.util;

import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;

import java.util.Locale;

public class WorkItemFieldUtils {
    public static String normalizeFieldRef(String fieldRef) {
        if (fieldRef == null) {
            return null;
        }
        String normalized = normalizeToken(fieldRef);
        return switch (normalized) {
            case "summary" -> WorkItemFieldConstants.SUMMARY;
            case "description" -> WorkItemFieldConstants.DESCRIPTION;
            case "priority_id", "priority" -> WorkItemFieldConstants.PRIORITY_ID;
            case "assignee_id", "assignee" -> WorkItemFieldConstants.ASSIGNEE_ID;
            case "due_date", "due" -> WorkItemFieldConstants.DUE_DATE;
            case "time_original_estimate", "original_estimate" -> WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE;
            case "security_level_id", "security_level", "security" -> WorkItemFieldConstants.SECURITY_LEVEL_ID;
            case "resolution_id", "resolution" -> WorkItemFieldConstants.RESOLUTION_ID;
            default -> normalized;
        };
    }

    private static String normalizeToken(String value) {
        String normalized = value.trim().replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        normalized = normalized.replace('-', '_').replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }

}
