/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.fieldconfig.port;

import java.util.List;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigItemEntity;

public interface IFieldConfigItemPort {
    List<FieldConfigItemEntity> createFieldConfigItems(List<FieldConfigItemEntity> items);

    List<FieldConfigItemEntity> getFieldConfigItemsByFieldConfigIdIncludingSystem(Long fieldConfigId, Long tenantId);

    List<FieldConfigItemEntity> getFieldConfigItemsByFieldConfigId(Long fieldConfigId, Long tenantId);
}
