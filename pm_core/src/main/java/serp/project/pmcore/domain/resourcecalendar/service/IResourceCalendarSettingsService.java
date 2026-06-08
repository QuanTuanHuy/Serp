/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service;

import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;

import java.util.List;

public interface IResourceCalendarSettingsService {
    void validateBlocks(List<ResourceCalendarProfileBlockEntity> blocks);

    void validateException(ResourceCalendarExceptionEntity exception);

    void validateAssignments(List<ResourceCalendarAssignmentEntity> assignments);
}
