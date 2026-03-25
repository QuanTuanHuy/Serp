/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.FieldConfigEntity;

import java.util.Optional;

public interface IFieldConfigPort {
    FieldConfigEntity createFieldConfig(FieldConfigEntity fieldConfig);

    Optional<FieldConfigEntity> getFieldConfigById(Long fieldConfigId, Long tenantId);

    Optional<FieldConfigEntity> getFieldConfigByIdIncludingSystem(Long fieldConfigId, Long tenantId);
}
