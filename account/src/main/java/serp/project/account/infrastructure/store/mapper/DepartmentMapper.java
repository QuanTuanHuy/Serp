/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.dto.request.CreateDepartmentRequest;
import serp.project.account.core.domain.dto.request.UpdateDepartmentRequest;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.infrastructure.store.model.DepartmentModel;
import serp.project.account.infrastructure.store.repository.IDepartmentRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepartmentMapper extends BaseMapper {

    private final IDepartmentRepository departmentRepository;

    public DepartmentEntity toEntity(DepartmentModel model) {
        if (model == null) {
            return null;
        }

        return DepartmentEntity.builder()
                .id(model.getId())
                .organizationId(model.getOrganizationId())
                .name(model.getName())
                .code(model.getCode())
                .description(model.getDescription())
                .parentDepartmentId(model.getParentDepartmentId())
                .managerId(model.getManagerId())
                .defaultModuleIds(model.getDefaultModuleIds() != null
                        ? Arrays.asList(model.getDefaultModuleIds())
                        : Collections.emptyList())
                .defaultRoleIds(model.getDefaultRoleIds() != null
                        ? Arrays.asList(model.getDefaultRoleIds())
                        : Collections.emptyList())
                .isActive(model.getIsActive())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public DepartmentModel toModel(DepartmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return DepartmentModel.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .parentDepartmentId(entity.getParentDepartmentId())
                .managerId(entity.getManagerId())
                .defaultModuleIds(entity.getDefaultModuleIds() != null
                        ? entity.getDefaultModuleIds().toArray(new Long[0])
                        : new Long[0])
                .defaultRoleIds(entity.getDefaultRoleIds() != null
                        ? entity.getDefaultRoleIds().toArray(new Long[0])
                        : new Long[0])
                .isActive(entity.getIsActive())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<DepartmentEntity> toEntityList(List<DepartmentModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .toList();
    }

    public List<DepartmentModel> toModelList(List<DepartmentEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .toList();
    }

    public DepartmentEntity createMapper(CreateDepartmentRequest request, Long organizationId) {
        if (request == null || organizationId == null) {
            return null;
        }

        String code = generateNextCode(organizationId);

        return DepartmentEntity.builder()
                .organizationId(organizationId)
                .name(request.getName())
                .code(code)
                .description(request.getDescription())
                .parentDepartmentId(request.getParentDepartmentId())
                .managerId(request.getManagerId())
                .defaultModuleIds(request.getDefaultModuleIds() != null
                        ? request.getDefaultModuleIds()
                        : Collections.emptyList())
                .defaultRoleIds(request.getDefaultRoleIds() != null
                        ? request.getDefaultRoleIds()
                        : Collections.emptyList())
                .isActive(true)
                .build();
    }

    public DepartmentEntity updateMapper(DepartmentEntity department, UpdateDepartmentRequest request) {
        if (department == null || request == null) {
            return null;
        }

        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }
        if (request.getManagerId() != null) {
            department.setManagerId(request.getManagerId());
        }
        if (request.getParentDepartmentId() != null) {
            department.setParentDepartmentId(request.getParentDepartmentId());
        }
        if (request.getDefaultModuleIds() != null) {
            department.setDefaultModuleIds(request.getDefaultModuleIds());
        }
        if (request.getDefaultRoleIds() != null) {
            department.setDefaultRoleIds(request.getDefaultRoleIds());
        }
        if (request.getIsActive() != null) {
            department.setIsActive(request.getIsActive());
        }
        return department;
    }

    public String generateNextCode(Long organizationId) {
        try {
            var lastDept = departmentRepository.findTopByOrganizationIdOrderByIdDesc(organizationId);

            if (lastDept.isPresent() && lastDept.get().getCode() != null) {
                String lastCode = lastDept.get().getCode();
                String numPart = lastCode.substring(4);
                int nextNum = Integer.parseInt(numPart) + 1;
                return String.format("DEPT%06d", nextNum);
            }

            return "DEPT000001";
        } catch (Exception e) {
            log.error("Error generating department code for organization {}: {}",
                    organizationId, e.getMessage());
            return "DEPT" + String.format("%06d", System.currentTimeMillis() % 1000000);
        }
    }
}
