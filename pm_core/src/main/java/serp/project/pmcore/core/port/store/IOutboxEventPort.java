package serp.project.pmcore.core.port.store;

import java.util.List;

import serp.project.pmcore.core.domain.entity.OutboxEventEntity;
import serp.project.pmcore.core.domain.enums.OutboxEventStatus;

public interface IOutboxEventPort {
    OutboxEventEntity save(OutboxEventEntity event);
    void batchUpdateStatus(List<OutboxEventEntity> events);
    List<OutboxEventEntity> getEventsByStatuses(List<OutboxEventStatus> statuses, int limit);
    int deletePublishedEventsBefore(long timestamp);
}
