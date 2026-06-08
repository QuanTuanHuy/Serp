/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.service;

import serp.project.pmcore.domain.resourcecalendar.model.GeneratedResourceCalendarSlot;
import serp.project.pmcore.domain.resourcecalendar.model.ResourceCalendarMaterializationInput;

import java.util.List;

public interface IResourceCalendarMaterializationService {
    List<GeneratedResourceCalendarSlot> materialize(ResourceCalendarMaterializationInput input);
}
