/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.FieldConfigSchemeEntity;

import java.util.Optional;

public interface IFieldConfigSchemePort {
    Optional<FieldConfigSchemeEntity> getFieldConfigSchemeById(Long schemeId, Long tenantId);

    Optional<FieldConfigSchemeEntity> getFieldConfigSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
