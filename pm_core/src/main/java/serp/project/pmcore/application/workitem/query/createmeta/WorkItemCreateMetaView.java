/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.createmeta;

import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.priority.entity.PriorityEntity;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.workitem.dto.WorkItemFieldPolicy;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record WorkItemCreateMetaView(
        ProjectSummaryView project,
        boolean createAllowed,
        String createBlockedReason,
        List<IssueTypeOptionView> issueTypes,
        Long selectedIssueTypeId,
        StatusOptionView initialStatus,
        Long defaultPriorityId,
        List<PriorityOptionView> priorities,
        Long defaultSecurityLevelId,
        List<SecurityLevelOptionView> securityLevels,
        List<ComponentOptionView> components,
        Map<String, FieldPolicyView> systemFields,
        List<CustomFieldView> customFields
) {

    public record ProjectSummaryView(
            Long id,
            String key,
            String name,
            String projectTypeKey,
            boolean archived
    ) {
        public static ProjectSummaryView from(ProjectEntity entity) {
            return new ProjectSummaryView(
                    entity.getId(),
                    entity.getKey(),
                    entity.getName(),
                    entity.getProjectTypeKey(),
                    Boolean.TRUE.equals(entity.getIsArchived())
            );
        }
    }

    public record IssueTypeOptionView(
            Long id,
            String typeKey,
            String name,
            String description,
            String iconUrl,
            Integer hierarchyLevel
    ) {
        public static IssueTypeOptionView from(IssueTypeEntity entity) {
            return new IssueTypeOptionView(
                    entity.getId(),
                    entity.getTypeKey(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getIconUrl(),
                    entity.getHierarchyLevel()
            );
        }
    }

    public record StatusOptionView(
            Long id,
            String statusKey,
            String name,
            String description,
            String iconUrl,
            Long statusCategoryId
    ) {
        public static StatusOptionView from(StatusEntity entity) {
            return new StatusOptionView(
                    entity.getId(),
                    entity.getStatusKey(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getIconUrl(),
                    entity.getCategoryId()
            );
        }
    }

    public record PriorityOptionView(
            Long id,
            String priorityKey,
            String name,
            String description,
            String iconUrl,
            String color,
            Integer sequence
    ) {
        public static PriorityOptionView from(PriorityEntity entity) {
            return new PriorityOptionView(
                    entity.getId(),
                    entity.getPriorityKey(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getIconUrl(),
                    entity.getColor(),
                    entity.getSequence()
            );
        }
    }

    public record SecurityLevelOptionView(
            Long id,
            String name,
            String description
    ) {
        public static SecurityLevelOptionView from(IssueSecurityLevelEntity entity) {
            return new SecurityLevelOptionView(
                    entity.getId(),
                    entity.getName(),
                    entity.getDescription()
            );
        }
    }

    public record ComponentOptionView(
            Long id,
            String name,
            String description,
            Long leadUserId,
            String assigneeType
    ) {
        public static ComponentOptionView from(ProjectComponentEntity entity) {
            return new ComponentOptionView(
                    entity.getId(),
                    entity.getName(),
                    entity.getDescription(),
                    entity.getLeadUserId(),
                    entity.getAssigneeType()
            );
        }
    }

    public record FieldPolicyView(
            boolean required,
            boolean hidden,
            boolean onScreen,
            boolean clientWritable
    ) {
        public static FieldPolicyView from(WorkItemFieldPolicy policy) {
            return new FieldPolicyView(
                    policy.required(),
                    policy.hidden(),
                    policy.onScreen(),
                    policy.isClientWritable()
            );
        }
    }

    public record CustomFieldView(
            Long id,
            String fieldKey,
            String name,
            String description,
            String typeKey,
            String schemaJson,
            Long contextId,
            String contextName,
            String contextIssueTypeKey,
            boolean required,
            boolean hidden,
            boolean onScreen,
            boolean clientWritable,
            List<CustomFieldOptionView> options,
            List<CustomFieldDefaultValueView> defaultValues
    ) {
        public static CustomFieldView from(CustomFieldEntity field,
                                           CustomFieldContextEntity context,
                                           WorkItemFieldPolicy policy,
                                           List<CustomFieldOptionEntity> options,
                                           List<CustomFieldContextDefaultValueEntity> defaultValues) {
            return new CustomFieldView(
                    field.getId(),
                    field.getFieldKey(),
                    field.getName(),
                    field.getDescription(),
                    field.getTypeKey(),
                    field.getSchemaJson(),
                    context.getId(),
                    context.getName(),
                    context.getIssueTypeKey(),
                    policy.required(),
                    policy.hidden(),
                    policy.onScreen(),
                    policy.isClientWritable(),
                    options.stream().map(CustomFieldOptionView::from).toList(),
                    defaultValues.stream().map(CustomFieldDefaultValueView::from).toList()
            );
        }
    }

    public record CustomFieldOptionView(
            Long id,
            String optionKey,
            String value,
            Integer sequence,
            Long parentOptionId,
            boolean disabled
    ) {
        public static CustomFieldOptionView from(CustomFieldOptionEntity entity) {
            return new CustomFieldOptionView(
                    entity.getId(),
                    entity.getOptionKey(),
                    entity.getValue(),
                    entity.getSequence(),
                    entity.getParentOptionId(),
                    Boolean.TRUE.equals(entity.getIsDisabled())
            );
        }
    }

    public record CustomFieldDefaultValueView(
            String valueType,
            Object value,
            Integer sortOrder
    ) {
        public static CustomFieldDefaultValueView from(CustomFieldContextDefaultValueEntity entity) {
            return new CustomFieldDefaultValueView(
                    entity.getValueType(),
                    extractValue(entity),
                    entity.getSortOrder()
            );
        }

        private static Object extractValue(CustomFieldContextDefaultValueEntity entity) {
            if (entity.getTextValue() != null) {
                return entity.getTextValue();
            }
            if (entity.getNumberValue() != null) {
                return entity.getNumberValue();
            }
            if (entity.getDateValue() != null) {
                return entity.getDateValue();
            }
            if (entity.getDatetimeValue() != null) {
                return entity.getDatetimeValue();
            }
            if (entity.getUserValueId() != null) {
                return entity.getUserValueId();
            }
            if (entity.getGroupValueId() != null) {
                return entity.getGroupValueId();
            }
            if (entity.getOptionValueId() != null) {
                return entity.getOptionValueId();
            }
            if (entity.getJsonValue() != null) {
                return entity.getJsonValue();
            }
            BigDecimal numberValue = entity.getNumberValue();
            return numberValue;
        }
    }
}
