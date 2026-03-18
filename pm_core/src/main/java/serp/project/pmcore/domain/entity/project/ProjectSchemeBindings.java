/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.entity.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.dto.request.project.CreateProjectRequest;
import serp.project.pmcore.domain.enums.SchemeType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSchemeBindings {
    private Long issueTypeSchemeId;
    private Long workflowSchemeId;
    private Long fieldConfigSchemeId;
    private Long issueTypeScreenSchemeId;
    private Long permissionSchemeId;
    private Long notificationSchemeId;
    private Long prioritySchemeId;
    private Long issueSecuritySchemeId;

    public static ProjectSchemeBindings fromRequest(CreateProjectRequest request) {
        return ProjectSchemeBindings.builder()
                .issueTypeSchemeId(request.getIssueTypeSchemeId())
                .workflowSchemeId(request.getWorkflowSchemeId())
                .fieldConfigSchemeId(request.getFieldConfigSchemeId())
                .issueTypeScreenSchemeId(request.getIssueTypeScreenSchemeId())
                .permissionSchemeId(request.getPermissionSchemeId())
                .notificationSchemeId(request.getNotificationSchemeId())
                .prioritySchemeId(request.getPrioritySchemeId())
                .issueSecuritySchemeId(request.getIssueSecuritySchemeId())
                .build();
    }

    public List<String> getMissingRequiredFields() {
        List<String> missing = new ArrayList<>();

        if (issueTypeSchemeId == null) missing.add("issueTypeSchemeId");
        if (workflowSchemeId == null) missing.add("workflowSchemeId");
        if (fieldConfigSchemeId == null) missing.add("fieldConfigSchemeId");
        if (issueTypeScreenSchemeId == null) missing.add("issueTypeScreenSchemeId");
        if (permissionSchemeId == null) missing.add("permissionSchemeId");
        if (notificationSchemeId == null) missing.add("notificationSchemeId");
        if (prioritySchemeId == null) missing.add("prioritySchemeId");
        if (issueSecuritySchemeId == null) missing.add("issueSecuritySchemeId");

        return missing;
    }

    public Map<SchemeType, Long> toSchemeMap() {
        Map<SchemeType, Long> schemeMap = new EnumMap<>(SchemeType.class);
        schemeMap.put(SchemeType.ISSUE_TYPE, issueTypeSchemeId);
        schemeMap.put(SchemeType.WORKFLOW, workflowSchemeId);
        schemeMap.put(SchemeType.FIELD_CONFIG, fieldConfigSchemeId);
        schemeMap.put(SchemeType.SCREEN, issueTypeScreenSchemeId);
        schemeMap.put(SchemeType.PERMISSION, permissionSchemeId);
        schemeMap.put(SchemeType.NOTIFICATION, notificationSchemeId);
        schemeMap.put(SchemeType.PRIORITY, prioritySchemeId);
        schemeMap.put(SchemeType.ISSUE_SECURITY, issueSecuritySchemeId);
        return schemeMap;
    }

    public void applyTo(ProjectEntity project) {
        project.setIssueTypeSchemeId(issueTypeSchemeId);
        project.setWorkflowSchemeId(workflowSchemeId);
        project.setFieldConfigSchemeId(fieldConfigSchemeId);
        project.setIssueTypeScreenSchemeId(issueTypeScreenSchemeId);
        project.setPermissionSchemeId(permissionSchemeId);
        project.setNotificationSchemeId(notificationSchemeId);
        project.setPrioritySchemeId(prioritySchemeId);
        project.setIssueSecuritySchemeId(issueSecuritySchemeId);
    }
}
