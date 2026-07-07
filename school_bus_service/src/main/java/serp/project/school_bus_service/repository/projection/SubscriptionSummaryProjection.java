package serp.project.school_bus_service.repository.projection;

public interface SubscriptionSummaryProjection {
    Long getTotalSubscriptions();

    Long getActiveSubscriptions();

    Long getInactiveSubscriptions();
}
