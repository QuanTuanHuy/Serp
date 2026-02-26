/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import serp.project.pmcore.core.domain.entity.OutboxEventEntity;

public interface IOutboxEventService {
    OutboxEventEntity saveEvent(OutboxEventEntity event);
}
