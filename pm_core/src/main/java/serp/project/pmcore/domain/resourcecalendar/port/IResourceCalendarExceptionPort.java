/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.resourcecalendar.port;

import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;

import java.util.List;
import java.util.Optional;

public interface IResourceCalendarExceptionPort {
    List<ResourceCalendarExceptionEntity> listExceptions(Long tenantId,
                                                         List<Long> userIds,
                                                         Long windowStart,
                                                         Long windowEnd);

    Optional<ResourceCalendarExceptionEntity> getExceptionById(Long tenantId, Long exceptionId);

    ResourceCalendarExceptionEntity saveException(ResourceCalendarExceptionEntity exception);

    void deleteException(Long tenantId, Long exceptionId);
}
