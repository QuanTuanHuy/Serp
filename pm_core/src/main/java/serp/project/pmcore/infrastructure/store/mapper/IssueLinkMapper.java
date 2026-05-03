/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkDetailEntity;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;
import serp.project.pmcore.infrastructure.store.model.IssueLinkModel;
import serp.project.pmcore.infrastructure.store.repository.IIssueLinkRepository;

import java.util.List;

@Component
public class IssueLinkMapper extends BaseMapper {

    public IssueLinkModel toModel(IssueLinkEntity entity) {
        if (entity == null) {
            return null;
        }
        return IssueLinkModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .sourceId(entity.getSourceId())
                .targetId(entity.getTargetId())
                .linkTypeId(entity.getLinkTypeId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(longToLocalDateTime(entity.getDeletedAt()))
                .build();
    }

    public IssueLinkEntity toEntity(IssueLinkModel model) {
        if (model == null) {
            return null;
        }
        return IssueLinkEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .sourceId(model.getSourceId())
                .targetId(model.getTargetId())
                .linkTypeId(model.getLinkTypeId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .deletedAt(localDateTimeToLong(model.getDeletedAt()))
                .build();
    }

    public List<IssueLinkDetailEntity> toDetailEntities(List<IIssueLinkRepository.IssueLinkDetailRow> rows) {
        return rows.stream().map(this::toDetailEntity).toList();
    }

    public IssueLinkDetailEntity toDetailEntity(IIssueLinkRepository.IssueLinkDetailRow row) {
        return IssueLinkDetailEntity.builder()
                .linkId(row.getLinkId())
                .sourceId(row.getSourceId())
                .targetId(row.getTargetId())
                .linkTypeId(row.getLinkTypeId())
                .linkTypeName(row.getLinkTypeName())
                .outwardDescription(row.getOutwardDesc())
                .inwardDescription(row.getInwardDesc())
                .relatedWorkItemId(row.getRelatedWorkItemId())
                .relatedProjectId(row.getRelatedProjectId())
                .relatedWorkItemKey(row.getRelatedWorkItemKey())
                .relatedWorkItemSummary(row.getRelatedWorkItemSummary())
                .relatedIssueTypeId(row.getRelatedIssueTypeId())
                .relatedIssueTypeName(row.getRelatedIssueTypeName())
                .relatedStatusId(row.getRelatedStatusId())
                .relatedStatusName(row.getRelatedStatusName())
                .createdAt(row.getCreatedAt() == null ? null : row.getCreatedAt().getTime())
                .createdBy(row.getCreatedBy())
                .build();
    }
}
