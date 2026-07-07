package serp.project.school_bus_service.repository.projection;

public interface StudentSummaryProjection {
    Long getTotalStudents();

    Long getLinkedSchools();

    Long getLinkedParents();

    Long getActiveStudents();
}
