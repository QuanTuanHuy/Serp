/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.fieldconfig.port;

import java.util.List;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeItemEntity;

public interface IFieldConfigSchemeItemPort {
    List<FieldConfigSchemeItemEntity> createFieldConfigSchemeItems(List<FieldConfigSchemeItemEntity> items);

    List<FieldConfigSchemeItemEntity> getFieldConfigSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<FieldConfigSchemeItemEntity> getFieldConfigSchemeItemsBySchemeId(Long schemeId, Long tenantId);
}
