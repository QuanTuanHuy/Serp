/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.infrastructure.store.model.OrganizationModel;

@Component
public class OrganizationMapper extends BaseMapper {
    
    public OrganizationEntity toEntity(OrganizationModel model) {
        if (model == null) {
            return null;
        }
        
        return OrganizationEntity.builder()
                .id(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .address(model.getAddress())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public OrganizationModel toModel(OrganizationEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return OrganizationModel.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .address(entity.getAddress())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<OrganizationEntity> toEntityList(List<OrganizationModel> models) {
        if (models == null) {
            return null;
        }
        
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<OrganizationModel> toModelList(List<OrganizationEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }
}
