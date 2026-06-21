/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

import java.util.Set;

public record VisibleWorkItemSearchCriteria(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        String keyword,
        Long excludedProjectId,
        int limit
) {
}
