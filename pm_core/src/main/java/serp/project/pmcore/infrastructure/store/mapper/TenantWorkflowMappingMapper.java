/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.TenantWorkflowMappingEntity;
import serp.project.pmcore.infrastructure.store.model.TenantWorkflowMappingModel;

@Component
public class TenantWorkflowMappingMapper extends BaseMapper {

    public TenantWorkflowMappingEntity toEntity(TenantWorkflowMappingModel model) {
        if (model == null) {
            return null;
        }
        return TenantWorkflowMappingEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .sourceWorkflowId(model.getSourceWorkflowId())
                .tenantWorkflowId(model.getTenantWorkflowId())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    public TenantWorkflowMappingModel toModel(TenantWorkflowMappingEntity entity) {
        if (entity == null) {
            return null;
        }
        return TenantWorkflowMappingModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .sourceWorkflowId(entity.getSourceWorkflowId())
                .tenantWorkflowId(entity.getTenantWorkflowId())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .createdBy(entity.getCreatedBy())
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
