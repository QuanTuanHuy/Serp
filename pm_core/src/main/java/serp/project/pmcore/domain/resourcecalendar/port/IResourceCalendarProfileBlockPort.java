/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.port;

import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;

import java.util.List;

public interface IResourceCalendarProfileBlockPort {
    List<ResourceCalendarProfileBlockEntity> listByProfileId(Long profileId);

    List<ResourceCalendarProfileBlockEntity> replaceBlocks(Long profileId, List<ResourceCalendarProfileBlockEntity> blocks);
}
