package serp.project.school_bus_service.repository.projection;

public interface GreedyFillCandidateProjection {

    Long getSubscriptionId();

    Long getStudentId();

    Long getPointId();

    String getPointName();

    Double getLatitude();

    Double getLongitude();

    String getUsageType();

    Boolean getPointActive();

    Boolean getPointDeleted();

    Boolean getLinkedToSchool();
}
