/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.entity.OutboxEventEntity;

public interface IOutboxEventService {
    OutboxEventEntity saveEvent(OutboxEventEntity event);
}
