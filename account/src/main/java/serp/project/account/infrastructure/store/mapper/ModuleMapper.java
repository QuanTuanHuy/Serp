/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.CreateModuleDto;
import serp.project.account.core.domain.dto.request.UpdateModuleDto;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.enums.ModuleEnum;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.ModuleType;
import serp.project.account.core.domain.enums.PricingModel;
import serp.project.account.infrastructure.store.model.ModuleModel;
import serp.project.account.kernel.utils.ConvertUtils;

@Component
@RequiredArgsConstructor
public class ModuleMapper extends BaseMapper {

    private final ConvertUtils convertUtils;

    public ModuleEntity toEntity(ModuleModel model) {
        if (model == null) {
            return null;
        }

        return ModuleEntity.builder()
                .id(model.getId())
                .moduleName(model.getModuleName())
                .code(model.getCode())
                .description(model.getDescription())
                .keycloakClientId(model.getKeycloakClientId())
                .category(model.getCategory())
                .icon(model.getIcon())
                .displayOrder(model.getDisplayOrder())
                .moduleType(model.getModuleType())
                .isGlobal(model.getIsGlobal())
                .organizationId(model.getOrganizationId())
                .isFree(model.getIsFree())
                .pricingModel(model.getPricingModel())
                .dependsOnModuleIds(model.getDependsOnModuleIds())
                .status(model.getStatus())
                .version(model.getVersion())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public ModuleModel toModel(ModuleEntity entity) {
        if (entity == null) {
            return null;
        }

        return ModuleModel.builder()
                .id(entity.getId())
                .moduleName(entity.getModuleName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .keycloakClientId(entity.getKeycloakClientId())
                .category(entity.getCategory())
                .icon(entity.getIcon())
                .displayOrder(entity.getDisplayOrder())
                .moduleType(entity.getModuleType())
                .isGlobal(entity.getIsGlobal())
                .organizationId(entity.getOrganizationId())
                .isFree(entity.getIsFree())
                .pricingModel(entity.getPricingModel())
                .dependsOnModuleIds(entity.getDependsOnModuleIds())
                .status(entity.getStatus())
                .version(entity.getVersion())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<ModuleEntity> toEntityList(List<ModuleModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<ModuleModel> toModelList(List<ModuleEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public ModuleEntity createModuleMapper(CreateModuleDto request) {
        if (request == null) {
            return null;
        }

        return ModuleEntity.builder()
                .moduleName(request.getName())
                .description(request.getDescription())
                .keycloakClientId(request.getKeycloakClientId())
                .category(request.getCategory())
                .icon(request.getIcon())
                .displayOrder(request.getDisplayOrder())
                .moduleType(convertUtils.convertStringToEnum(request.getModuleType(), ModuleType.class))
                .isGlobal(request.getIsGlobal())
                .organizationId(request.getOrganizationId())
                .isFree(request.getIsFree())
                .pricingModel(convertUtils.convertStringToEnum(request.getPricingModel(), PricingModel.class))
                .status(convertUtils.convertStringToEnum(request.getStatus(), ModuleStatus.class))
                .build();
    }

    public ModuleEntity updateModuleMapper(ModuleEntity entity, UpdateModuleDto request) {
        if (entity == null || request == null) {
            return entity;
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            entity.setModuleName(request.getName());
        }

        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }

        if (request.getKeycloakClientId() != null && !request.getKeycloakClientId().trim().isEmpty()) {
            entity.setKeycloakClientId(request.getKeycloakClientId());
        }

        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            entity.setCategory(request.getCategory());
        }

        if (request.getIcon() != null && !request.getIcon().trim().isEmpty()) {
            entity.setIcon(request.getIcon());
        }

        if (request.getDisplayOrder() != null) {
            entity.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getModuleType() != null && !request.getModuleType().trim().isEmpty()) {
            entity.setModuleType(convertUtils.convertStringToEnum(request.getModuleType(), ModuleType.class));
        }

        if (request.getIsGlobal() != null) {
            entity.setIsGlobal(request.getIsGlobal());
        }

        if (request.getOrganizationId() != null) {
            entity.setOrganizationId(request.getOrganizationId());
        }

        if (request.getIsFree() != null) {
            entity.setIsFree(request.getIsFree());
        }

        if (request.getPricingModel() != null && !request.getPricingModel().trim().isEmpty()) {
            entity.setPricingModel(convertUtils.convertStringToEnum(request.getPricingModel(), PricingModel.class));
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            entity.setStatus(convertUtils.convertStringToEnum(request.getStatus(), ModuleStatus.class));
        }

        return entity;
    }

    public ModuleEntity buildFromModuleEnum(ModuleEnum moduleEnum, List<Long> dependsOnModuleIds) {
        if (moduleEnum == null) {
            return null;
        }

        return ModuleEntity.builder()
                .moduleName(moduleEnum.getModuleName())
                .code(moduleEnum.getCode())
                .description("System module: " + moduleEnum.getModuleName())
                .keycloakClientId(moduleEnum.getKeycloakClientId())
                .category(moduleEnum.getCategory())
                .icon(moduleEnum.getIcon())
                .displayOrder(moduleEnum.getDisplayOrder())
                .moduleType(moduleEnum.getModuleType())
                .isGlobal(moduleEnum.getIsGlobal())
                .organizationId(null)
                .isFree(moduleEnum.getIsFree())
                .pricingModel(moduleEnum.getPricingModel())
                .dependsOnModuleIds(dependsOnModuleIds)
                .status(moduleEnum.getStatus())
                .version(moduleEnum.getVersion())
                .build();
    }
}
