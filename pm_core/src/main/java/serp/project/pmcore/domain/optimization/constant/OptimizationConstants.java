/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OptimizationConstants {
    public static final long HOUR_MILLIS = 3_600_000L;
    public static final long DAY_MILLIS = 86_400_000L;
    public static final long DAILY_CAPACITY_MILLIS = 8 * HOUR_MILLIS;

    public static final int MAX_SELECTED_WORK_ITEM_IDS = 50;
    public static final int SCORE_DECIMAL_SCALE = 6;
    public static final String DEFAULT_SCOPE = "SELECTED_WORK_ITEMS";

    public static final String TIME_REMAINING_ESTIMATE = "TIME_REMAINING_ESTIMATE";
    public static final String TIME_ORIGINAL_ESTIMATE = "TIME_ORIGINAL_ESTIMATE";
    public static final String DURATION_SOURCE_DEFAULT = "DEFAULT";
    public static final String STATUS_CATEGORY_DONE = "DONE";
    public static final double PRIORITY_NEUTRAL_FACTOR = 0.5D;
    public static final double DUE_DATE_WINDOW_DAYS = 14D;
    public static final double BLOCKER_FACTOR_PER_SUCCESSOR = 0.25D;
    public static final double DEFAULT_ESTIMATE_PENALTY = -0.2D;
    public static final int DEFAULT_DURATION_SUBTASK_HOURS = 2;
    public static final int DEFAULT_DURATION_EPIC_DAYS = 3;
    public static final String FALLBACK_CAPACITY_DETAILS = "8h weekday UTC slots";

    public static final double BASE_ASSIGNMENT_COST = 10D;
    public static final double CURRENT_ASSIGNEE_DISCOUNT = 3D;
    public static final double COMPONENT_LEAD_DISCOUNT = 2D;
    public static final double PROJECT_LEAD_DISCOUNT = 1D;
    public static final double REPORTER_DISCOUNT = 0.5D;
    public static final double PROJECT_MEMBER_DISCOUNT = 0.25D;

    public static final double MINIMAL_REASSIGNMENT_PENALTY = 8D;
    public static final double STANDARD_REASSIGNMENT_PENALTY = 2D;
    public static final double MINIMAL_REASSIGNMENT_CURRENT_ASSIGNEE_BONUS = 5D;
    public static final double OVERLOAD_BASE_PENALTY = 25D;
}
