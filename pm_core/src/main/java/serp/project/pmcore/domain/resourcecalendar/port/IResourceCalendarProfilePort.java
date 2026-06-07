/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.port;

import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileEntity;

import java.util.List;
import java.util.Optional;

public interface IResourceCalendarProfilePort {
    List<ResourceCalendarProfileEntity> listProfiles(Long tenantId);

    Optional<ResourceCalendarProfileEntity> getProfileById(Long tenantId, Long profileId);

    ResourceCalendarProfileEntity saveProfile(ResourceCalendarProfileEntity profile);

    void deleteProfile(Long tenantId, Long profileId);
}
