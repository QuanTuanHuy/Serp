/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.constant;

import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class WorkItemFieldConstants {
    public static final String CREATE_OPERATION_KEY = "CREATE";
    public static final String EDIT_OPERATION_KEY = "EDIT";
    public static final String FIELD_REF_TYPE_SYSTEM = "SYSTEM";
    public static final String FIELD_REF_TYPE_CUSTOM = "CUSTOM";

    public static final String ISSUE_TYPE_ID = "issue_type_id";
    public static final String SUMMARY = "summary";
    public static final String DESCRIPTION = "description";
    public static final String PRIORITY_ID = "priority_id";
    public static final String ASSIGNEE_ID = "assignee_id";
    public static final String PARENT_ID = "parent_id";
    public static final String START_DATE = "start_date";
    public static final String DUE_DATE = "due_date";
    public static final String TIME_ORIGINAL_ESTIMATE = "time_original_estimate";
    public static final String SECURITY_LEVEL_ID = "security_level_id";
    public static final String RESOLUTION_ID = "resolution_id";

    public static final Set<String> ALWAYS_WRITABLE_ON_CREATE_SYSTEM_FIELDS = Set.of(
            ISSUE_TYPE_ID,
            SUMMARY
    );

    public static final Set<String> SUPPORTED_CREATE_SYSTEM_FIELDS = Set.of(
            ISSUE_TYPE_ID,
            SUMMARY,
            DESCRIPTION,
            PRIORITY_ID,
            ASSIGNEE_ID,
            PARENT_ID,
            START_DATE,
            DUE_DATE,
            TIME_ORIGINAL_ESTIMATE,
            SECURITY_LEVEL_ID
    );

    public static final Set<String> SUPPORTED_TRANSITION_SYSTEM_FIELDS = Set.of(
            SUMMARY,
            DESCRIPTION,
            PRIORITY_ID,
            ASSIGNEE_ID,
            START_DATE,
            DUE_DATE,
            TIME_ORIGINAL_ESTIMATE,
            SECURITY_LEVEL_ID
    );

    public static final Set<String> SUPPORTED_UPDATE_SYSTEM_FIELDS = Set.of(
            SUMMARY,
            DESCRIPTION,
            PRIORITY_ID,
            ASSIGNEE_ID,
            START_DATE,
            DUE_DATE,
            TIME_ORIGINAL_ESTIMATE,
            SECURITY_LEVEL_ID
    );
}
