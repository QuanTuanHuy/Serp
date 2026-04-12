/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelMemberEntity;
import serp.project.pmcore.infrastructure.store.model.IssueSecurityLevelMemberModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssueSecurityLevelMemberMapper extends BaseMapper {

    public IssueSecurityLevelMemberEntity toEntity(IssueSecurityLevelMemberModel model) {
        if (model == null) { return null; }
        return IssueSecurityLevelMemberEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .levelId(model.getLevelId())
                .subjectType(model.getSubjectType())
                .subjectRef(model.getSubjectRef())
                .customFieldId(model.getCustomFieldId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public IssueSecurityLevelMemberModel toModel(IssueSecurityLevelMemberEntity entity) {
        if (entity == null) { return null; }
        return IssueSecurityLevelMemberModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .levelId(entity.getLevelId())
                .subjectType(entity.getSubjectType())
                .subjectRef(entity.getSubjectRef())
                .customFieldId(entity.getCustomFieldId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    public List<IssueSecurityLevelMemberEntity> toEntities(List<IssueSecurityLevelMemberModel> models) {
        if (models == null) { return Collections.emptyList(); }
        return models.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<IssueSecurityLevelMemberModel> toModels(List<IssueSecurityLevelMemberEntity> entities) {
        if (entities == null) { return Collections.emptyList(); }
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }
}
