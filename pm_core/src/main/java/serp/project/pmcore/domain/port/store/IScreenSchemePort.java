/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.ScreenSchemeEntity;

import java.util.Optional;

public interface IScreenSchemePort {
    ScreenSchemeEntity createScreenScheme(ScreenSchemeEntity scheme);

    Optional<ScreenSchemeEntity> getScreenSchemeById(Long schemeId, Long tenantId);

    Optional<ScreenSchemeEntity> getScreenSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
