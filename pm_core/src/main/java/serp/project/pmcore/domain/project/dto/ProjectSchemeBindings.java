/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.shared.enums.SchemeType;

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

    public static ProjectSchemeBindings fromSchemeMap(Map<SchemeType, Long> schemeMap) {
        if (schemeMap == null) {
            return ProjectSchemeBindings.builder().build();
        }

        return ProjectSchemeBindings.builder()
                .issueTypeSchemeId(schemeMap.get(SchemeType.ISSUE_TYPE))
                .workflowSchemeId(schemeMap.get(SchemeType.WORKFLOW))
                .fieldConfigSchemeId(schemeMap.get(SchemeType.FIELD_CONFIG))
                .issueTypeScreenSchemeId(schemeMap.get(SchemeType.SCREEN))
                .permissionSchemeId(schemeMap.get(SchemeType.PERMISSION))
                .notificationSchemeId(schemeMap.get(SchemeType.NOTIFICATION))
                .prioritySchemeId(schemeMap.get(SchemeType.PRIORITY))
                .issueSecuritySchemeId(schemeMap.get(SchemeType.ISSUE_SECURITY))
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
        if (issueTypeSchemeId != null) {
            schemeMap.put(SchemeType.ISSUE_TYPE, issueTypeSchemeId);
        }
        if (workflowSchemeId != null) {
            schemeMap.put(SchemeType.WORKFLOW, workflowSchemeId);
        }
        if (fieldConfigSchemeId != null) {
            schemeMap.put(SchemeType.FIELD_CONFIG, fieldConfigSchemeId);
        }
        if (issueTypeScreenSchemeId != null) {
            schemeMap.put(SchemeType.SCREEN, issueTypeScreenSchemeId);
        }
        if (permissionSchemeId != null) {
            schemeMap.put(SchemeType.PERMISSION, permissionSchemeId);
        }
        if (notificationSchemeId != null) {
            schemeMap.put(SchemeType.NOTIFICATION, notificationSchemeId);
        }
        if (prioritySchemeId != null) {
            schemeMap.put(SchemeType.PRIORITY, prioritySchemeId);
        }
        if (issueSecuritySchemeId != null) {
            schemeMap.put(SchemeType.ISSUE_SECURITY, issueSecuritySchemeId);
        }
        return schemeMap;
    }

    public Long getSchemeId(SchemeType schemeType) {
        return switch (schemeType) {
            case ISSUE_TYPE -> issueTypeSchemeId;
            case WORKFLOW -> workflowSchemeId;
            case FIELD_CONFIG -> fieldConfigSchemeId;
            case SCREEN -> issueTypeScreenSchemeId;
            case PERMISSION -> permissionSchemeId;
            case NOTIFICATION -> notificationSchemeId;
            case PRIORITY -> prioritySchemeId;
            case ISSUE_SECURITY -> issueSecuritySchemeId;
        };
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
