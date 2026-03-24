/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.ScreenEntity;

import java.util.Optional;

public interface IScreenPort {
    ScreenEntity createScreen(ScreenEntity screen);

    Optional<ScreenEntity> getScreenById(Long screenId, Long tenantId);

    Optional<ScreenEntity> getScreenByIdIncludingSystem(Long screenId, Long tenantId);
}
