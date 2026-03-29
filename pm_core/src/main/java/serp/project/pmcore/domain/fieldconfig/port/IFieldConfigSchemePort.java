/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.fieldconfig.port;

import java.util.Optional;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeEntity;

public interface IFieldConfigSchemePort {
    FieldConfigSchemeEntity createFieldConfigScheme(FieldConfigSchemeEntity scheme);

    Optional<FieldConfigSchemeEntity> getFieldConfigSchemeById(Long schemeId, Long tenantId);

    Optional<FieldConfigSchemeEntity> getFieldConfigSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
