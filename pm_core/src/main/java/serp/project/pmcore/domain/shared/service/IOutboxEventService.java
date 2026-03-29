/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.service;

import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;

public interface IOutboxEventService {
    OutboxEventEntity saveEvent(OutboxEventEntity event);
}
