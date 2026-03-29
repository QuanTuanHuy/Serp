/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.screen.port;

import java.util.List;

import serp.project.pmcore.domain.screen.entity.ScreenTabEntity;

public interface IScreenTabPort {
    List<ScreenTabEntity> createScreenTabs(List<ScreenTabEntity> tabs);

    List<ScreenTabEntity> getScreenTabsByScreenIdIncludingSystem(Long screenId, Long tenantId);

    List<ScreenTabEntity> getScreenTabsByScreenId(Long screenId, Long tenantId);
}
