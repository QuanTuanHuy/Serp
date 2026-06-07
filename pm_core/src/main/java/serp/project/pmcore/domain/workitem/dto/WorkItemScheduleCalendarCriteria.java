/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.pagination.PageCriteria;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemScheduleCalendarCriteria extends PageCriteria {
    private Long projectId;
    private Long viewportStart;
    private Long viewportEnd;
    private List<Long> assigneeIds;
    private List<Long> issueTypeIds;
    private List<Long> statusIds;
    private String keyword;
}
