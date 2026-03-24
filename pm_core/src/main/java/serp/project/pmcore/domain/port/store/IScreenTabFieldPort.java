/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.ScreenTabFieldEntity;

import java.util.List;

public interface IScreenTabFieldPort {
    List<ScreenTabFieldEntity> createScreenTabFields(List<ScreenTabFieldEntity> fields);

    List<ScreenTabFieldEntity> getScreenTabFieldsByScreenTabIdIncludingSystem(Long screenTabId, Long tenantId);
}
