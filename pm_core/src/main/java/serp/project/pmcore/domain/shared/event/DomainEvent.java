package serp.project.pmcore.domain.shared.event;

public interface DomainEvent {
    String getEventType();
    Long getTenantId();
    Long getActorId();
    String getAggregateId();
    String getAggregateType();
}