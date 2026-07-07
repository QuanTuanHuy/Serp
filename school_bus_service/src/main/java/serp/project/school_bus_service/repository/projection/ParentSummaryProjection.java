package serp.project.school_bus_service.repository.projection;

public interface ParentSummaryProjection {
    Long getTotalParents();

    Long getWithEmail();

    Long getWithPhone();

    Long getActiveParents();
}
