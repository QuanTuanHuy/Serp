/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.entity.OutboxEventEntity;

public interface IOutboxEventService {
    OutboxEventEntity saveEvent(OutboxEventEntity event);
}
