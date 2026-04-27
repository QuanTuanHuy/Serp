/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.dto;

public record IssueTypeUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        String iconUrl,
        boolean iconUrlProvided,
        Integer hierarchyLevel,
        boolean hierarchyLevelProvided
) {
}
