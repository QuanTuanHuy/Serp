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

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemBoardCriteria {

    private Long projectId;
    private String keyword;
    private List<Long> statusIds;
    private List<Long> assigneeIds;
    private List<Long> issueTypeIds;
    private List<Long> priorityIds;
}
