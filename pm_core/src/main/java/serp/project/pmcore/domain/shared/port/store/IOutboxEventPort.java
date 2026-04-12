package serp.project.pmcore.domain.shared.port.store;

import java.util.List;

import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;

public interface IOutboxEventPort {
    OutboxEventEntity save(OutboxEventEntity event);
    void batchUpdateStatus(List<OutboxEventEntity> events);
    List<OutboxEventEntity> getEventsByStatuses(List<OutboxEventStatus> statuses, int limit);
    int deletePublishedEventsBefore(long timestamp);
}
