/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuetype.dto;

public record IssueTypeSchemeUpdateData(
        String name,
        boolean nameProvided,
        String description,
        boolean descriptionProvided,
        Long defaultIssueTypeId,
        boolean defaultIssueTypeIdProvided
) {
}
