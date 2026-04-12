/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.fieldconfig.port;

import java.util.Optional;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigEntity;

public interface IFieldConfigPort {
    FieldConfigEntity createFieldConfig(FieldConfigEntity fieldConfig);

    Optional<FieldConfigEntity> getFieldConfigById(Long fieldConfigId, Long tenantId);

    Optional<FieldConfigEntity> getFieldConfigByIdIncludingSystem(Long fieldConfigId, Long tenantId);
}
