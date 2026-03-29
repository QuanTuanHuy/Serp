/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.screen.port;

import java.util.List;

import serp.project.pmcore.domain.screen.entity.ScreenTabFieldEntity;

public interface IScreenTabFieldPort {
    List<ScreenTabFieldEntity> createScreenTabFields(List<ScreenTabFieldEntity> fields);

    List<ScreenTabFieldEntity> getScreenTabFieldsByScreenTabIdIncludingSystem(Long screenTabId, Long tenantId);

    List<ScreenTabFieldEntity> getScreenTabFieldsByScreenTabId(Long screenTabId, Long tenantId);
}
