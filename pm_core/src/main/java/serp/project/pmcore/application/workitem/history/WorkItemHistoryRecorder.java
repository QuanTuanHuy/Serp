/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.history;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.priority.port.IPriorityPort;
import serp.project.pmcore.domain.shared.constant.WorkItemFieldConstants;
import serp.project.pmcore.domain.shared.dto.user.UserProfileDto;
import serp.project.pmcore.domain.user.service.IUserService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.entity.WorkItemHistoryEntity;
import serp.project.pmcore.domain.workitem.port.IStatusPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemHistoryWritePort;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkItemHistoryRecorder {

    private static final String STATUS_ID = "status_id";
    private static final String WORKFLOW_STEP_ID = "workflow_step_id";

    private final IWorkItemHistoryWritePort historyWritePort;
    private final IPriorityPort priorityPort;
    private final IStatusPort statusPort;
    private final IUserService userService;

    public void recordChanges(Long tenantId,
                              Long workItemId,
                              Long actorId,
                              Map<String, Object> before,
                              Map<String, Object> after,
                              Collection<String> changedFields) {
        if (changedFields == null || changedFields.isEmpty()) {
            return;
        }

        Long now = System.currentTimeMillis();
        List<WorkItemHistoryEntity> histories = changedFields.stream()
                .distinct()
                .map(fieldKey -> buildHistory(tenantId, workItemId, actorId, fieldKey, before, after, now))
                .toList();
        historyWritePort.saveAll(histories);
    }

    public Map<String, Object> snapshotTrackedFields(WorkItemEntity workItem) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put(WorkItemFieldConstants.SUMMARY, workItem.getSummary());
        snapshot.put(WorkItemFieldConstants.DESCRIPTION, workItem.getDescription());
        snapshot.put(WorkItemFieldConstants.PRIORITY_ID, workItem.getPriorityId());
        snapshot.put(WorkItemFieldConstants.ASSIGNEE_ID, workItem.getAssigneeId());
        snapshot.put(WorkItemFieldConstants.START_DATE, workItem.getStartDate());
        snapshot.put(WorkItemFieldConstants.DUE_DATE, workItem.getDueDate());
        snapshot.put(WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE, workItem.getTimeOriginalEstimate());
        snapshot.put(WorkItemFieldConstants.TIME_REMAINING_ESTIMATE, workItem.getTimeRemainingEstimate());
        snapshot.put(WorkItemFieldConstants.SECURITY_LEVEL_ID, workItem.getSecurityLevelId());
        snapshot.put(WorkItemFieldConstants.RESOLUTION_ID, workItem.getResolutionId());
        snapshot.put(WORKFLOW_STEP_ID, workItem.getWorkflowStepId());
        snapshot.put(STATUS_ID, workItem.getStatusId());
        return snapshot;
    }

    private WorkItemHistoryEntity buildHistory(Long tenantId,
                                               Long workItemId,
                                               Long actorId,
                                               String fieldKey,
                                               Map<String, Object> before,
                                               Map<String, Object> after,
                                               Long now) {
        Object fromValue = before == null ? null : before.get(fieldKey);
        boolean hasAfterValue = after != null && after.containsKey(fieldKey);
        Object toValue = hasAfterValue ? after.get(fieldKey) : "[updated]";

        return WorkItemHistoryEntity.builder()
                .tenantId(tenantId)
                .workItemId(workItemId)
                .actorId(actorId)
                .fieldKey(fieldKey)
                .fieldName(fieldName(fieldKey))
                .fromValue(toRawValue(fromValue))
                .toValue(toRawValue(toValue))
                .fromDisplayValue(displayValue(tenantId, fieldKey, fromValue))
                .toDisplayValue(displayValue(tenantId, fieldKey, toValue))
                .createdAt(now)
                .createdBy(actorId)
                .updatedAt(now)
                .updatedBy(actorId)
                .build();
    }

    private String displayValue(Long tenantId, String fieldKey, Object value) {
        if (value == null) {
            return null;
        }
        if (WorkItemFieldConstants.PRIORITY_ID.equals(fieldKey)) {
            return priorityPort.getPriorityByIdIncludingSystem(asLong(value), tenantId)
                    .map(priority -> priority.getName() == null ? toRawValue(value) : priority.getName())
                    .orElse(toRawValue(value));
        }
        if (STATUS_ID.equals(fieldKey)) {
            return statusPort.getStatusByIdIncludingSystem(asLong(value), tenantId)
                    .map(status -> status.getName() == null ? toRawValue(value) : status.getName())
                    .orElse(toRawValue(value));
        }
        if (WorkItemFieldConstants.ASSIGNEE_ID.equals(fieldKey)) {
            return userDisplayName(asLong(value));
        }
        return toRawValue(value);
    }

    private String userDisplayName(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            UserProfileDto profile = userService.getUserById(userId);
            if (profile == null) {
                return String.valueOf(userId);
            }
            String fullName = profile.getFullName();
            if (fullName != null && !fullName.isBlank()) {
                return fullName;
            }
            return profile.getEmail() == null || profile.getEmail().isBlank()
                    ? String.valueOf(userId)
                    : profile.getEmail();
        } catch (Exception ex) {
            log.warn("[WorkItemHistoryRecorder] Failed to resolve user profile id={}: {}", userId, ex.getMessage());
            return String.valueOf(userId);
        }
    }

    private String toRawValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    private String fieldName(String fieldKey) {
        return switch (fieldKey) {
            case WorkItemFieldConstants.SUMMARY -> "Summary";
            case WorkItemFieldConstants.DESCRIPTION -> "Description";
            case WorkItemFieldConstants.PRIORITY_ID -> "Priority";
            case WorkItemFieldConstants.ASSIGNEE_ID -> "Assignee";
            case WorkItemFieldConstants.START_DATE -> "Start date";
            case WorkItemFieldConstants.DUE_DATE -> "Due date";
            case WorkItemFieldConstants.TIME_ORIGINAL_ESTIMATE -> "Original estimate";
            case WorkItemFieldConstants.TIME_REMAINING_ESTIMATE -> "Remaining estimate";
            case WorkItemFieldConstants.SECURITY_LEVEL_ID -> "Security level";
            case WorkItemFieldConstants.RESOLUTION_ID -> "Resolution";
            case STATUS_ID -> "Status";
            case WORKFLOW_STEP_ID -> "Workflow step";
            default -> fieldKey;
        };
    }
}
