/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.enums;

public enum OptimizationChangeScope {
    ASSIGNMENT_ONLY,
    SCHEDULE_ONLY,
    ASSIGNMENT_AND_SCHEDULE;

    public boolean includesAssignment() {
        return this == ASSIGNMENT_ONLY || this == ASSIGNMENT_AND_SCHEDULE;
    }

    public boolean includesScheduling() {
        return this == SCHEDULE_ONLY || this == ASSIGNMENT_AND_SCHEDULE;
    }
}
