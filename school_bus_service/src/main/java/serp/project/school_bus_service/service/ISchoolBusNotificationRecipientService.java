package serp.project.school_bus_service.service;

import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;

import java.util.Collection;
import java.util.Set;

public interface ISchoolBusNotificationRecipientService {

    Set<Long> findOperatorAccountUserIds(Long tenantId);

    Set<Long> findParentAccountUserIds(ParentProfileEntity parent);

    Set<Long> findDriverAccountUserIds(DriverProfileEntity driver);

    Set<Long> findAttendantAccountUserIds(BusAttendantProfileEntity attendant);

    Set<Long> findTripParentAccountUserIds(TripExecutionEntity trip);

    Set<Long> findParentAccountUserIds(Collection<TripStudentEntity> tripStudents);
}
