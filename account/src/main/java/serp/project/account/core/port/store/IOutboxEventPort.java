/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.util.List;

import serp.project.account.core.domain.entity.OutboxEventEntity;
import serp.project.account.core.domain.enums.OutboxEventStatus;

public interface IOutboxEventPort {
    OutboxEventEntity save(OutboxEventEntity event);
    void batchUpdateStatus(List<OutboxEventEntity> events);
    List<OutboxEventEntity> getEventsByStatuses(List<OutboxEventStatus> statuses, int limit);
    int deletePublishedEventsBefore(long timestamp);
}
