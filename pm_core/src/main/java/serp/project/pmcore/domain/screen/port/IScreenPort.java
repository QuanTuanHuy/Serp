/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.screen.port;

import java.util.Optional;

import serp.project.pmcore.domain.screen.entity.ScreenEntity;

public interface IScreenPort {
    ScreenEntity createScreen(ScreenEntity screen);

    Optional<ScreenEntity> getScreenById(Long screenId, Long tenantId);

    Optional<ScreenEntity> getScreenByIdIncludingSystem(Long screenId, Long tenantId);
}
