/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.ActivityStatus;
import serp.project.crm.core.domain.enums.ActivityType;
import serp.project.crm.core.domain.enums.TaskPriority;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ActivityFilterRequest extends BaseFilterRequest {

    private String keyword;
    private List<ActivityType> types;
    private List<ActivityStatus> statuses;
    private List<TaskPriority> priorities;
    private Long assignedTo;
    private Boolean unassignedOnly;

    private Long leadId;
    private Long accountId;
    private Long opportunityId;
    private Long contactId;

    private Long activityDateFrom;
    private Long activityDateTo;

    private Long dueDateFrom;
    private Long dueDateTo;

    private Boolean overdueOnly;
    private Boolean completedOnly;

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
