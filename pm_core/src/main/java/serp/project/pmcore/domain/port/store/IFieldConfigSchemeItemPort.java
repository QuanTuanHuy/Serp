/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.FieldConfigSchemeItemEntity;

import java.util.List;

public interface IFieldConfigSchemeItemPort {
    List<FieldConfigSchemeItemEntity> createFieldConfigSchemeItems(List<FieldConfigSchemeItemEntity> items);

    List<FieldConfigSchemeItemEntity> getFieldConfigSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);
}
