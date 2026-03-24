/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.ScreenTabEntity;

import java.util.List;

public interface IScreenTabPort {
    List<ScreenTabEntity> createScreenTabs(List<ScreenTabEntity> tabs);

    List<ScreenTabEntity> getScreenTabsByScreenIdIncludingSystem(Long screenId, Long tenantId);
}
