/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.validator;

import lombok.experimental.UtilityClass;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

@UtilityClass
public class WorkItemScheduleValidator {

    public static void validateRange(Long startDate, Long dueDate) {
        if (startDate == null || dueDate == null) {
            return;
        }
        if (startDate > dueDate) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.WORK_ITEM_SCHEDULE_INVALID,
                    "start_date must be less than or equal to due_date"
            );
        }
    }
}
