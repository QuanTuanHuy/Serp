package serp.project.school_bus_service.service;

public interface ISchoolBusDataScopeService {
    void assertCanAccessTrip(Long tripId);
    void assertCanOperateTrip(Long tripId);
    void assertCanAccessAttendance(Long tripId);
    void assertCanMarkAttendance(Long tripId);

    void assertCanAccessStudent(Long studentId);
    void assertCanAccessParentProfile(Long parentProfileId);
    void assertCanAccessTransportRequest(Long requestId);
    void assertCanAccessSubscription(Long subscriptionId);

    Long getCurrentParentProfileIdRequired();
    Long getCurrentDriverProfileIdRequired();
    Long getCurrentAttendantProfileIdRequired();
}
