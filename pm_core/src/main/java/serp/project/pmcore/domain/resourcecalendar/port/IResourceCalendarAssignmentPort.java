/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.port;

import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;

import java.util.List;

public interface IResourceCalendarAssignmentPort {
    List<ResourceCalendarAssignmentEntity> replaceProfileAssignments(Long tenantId,
                                                                     Long profileId,
                                                                     List<ResourceCalendarAssignmentEntity> assignments);

    List<ResourceCalendarAssignmentEntity> listByProfileId(Long tenantId, Long profileId);

    List<ResourceCalendarAssignmentEntity> listActiveAssignments(Long tenantId);
}
