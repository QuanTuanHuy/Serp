/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.FieldConfigItemEntity;

import java.util.List;

public interface IFieldConfigItemPort {
    List<FieldConfigItemEntity> createFieldConfigItems(List<FieldConfigItemEntity> items);

    List<FieldConfigItemEntity> getFieldConfigItemsByFieldConfigIdIncludingSystem(Long fieldConfigId, Long tenantId);

    List<FieldConfigItemEntity> getFieldConfigItemsByFieldConfigId(Long fieldConfigId, Long tenantId);
}
