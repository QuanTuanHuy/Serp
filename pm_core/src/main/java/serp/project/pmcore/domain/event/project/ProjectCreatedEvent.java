package serp.project.pmcore.domain.event.project;

import lombok.Builder;
import lombok.Getter;
import serp.project.pmcore.domain.event.DomainEvent;

@Getter
@Builder
public class ProjectCreatedEvent implements DomainEvent {
    private static final String EVENT_TYPE      = "PROJECT_CREATED";
    private static final String AGGREGATE_TYPE  = "PROJECT";

    private final Long   projectId;
    private final String projectKey;
    private final String projectName;
    private final String projectTypeKey;
    private final Long   tenantId;
    private final Long   actorId;

    @Override
    public String getEventType() {
        return EVENT_TYPE;
    }

    @Override
    public String getAggregateId() {
        return String.valueOf(projectId);
    }

    @Override
    public String getAggregateType() {
        return AGGREGATE_TYPE;
    }
}
