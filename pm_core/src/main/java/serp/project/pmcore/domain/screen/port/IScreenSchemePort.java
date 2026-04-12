/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.screen.port;

import java.util.Optional;

import serp.project.pmcore.domain.screen.entity.ScreenSchemeEntity;

public interface IScreenSchemePort {
    ScreenSchemeEntity createScreenScheme(ScreenSchemeEntity scheme);

    Optional<ScreenSchemeEntity> getScreenSchemeById(Long schemeId, Long tenantId);

    Optional<ScreenSchemeEntity> getScreenSchemeByIdIncludingSystem(Long schemeId, Long tenantId);
}
