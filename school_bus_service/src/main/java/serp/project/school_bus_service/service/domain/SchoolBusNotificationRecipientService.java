package serp.project.school_bus_service.service.domain;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import serp.project.school_bus_service.entity.TripExecutionEntity;
import serp.project.school_bus_service.entity.TripStudentEntity;
import serp.project.school_bus_service.service.ISchoolBusNotificationRecipientService;
import serp.project.school_bus_service.service.ISchoolBusUserRoleService;
import serp.project.school_bus_service.service.ISchoolBusUserService;
import serp.project.school_bus_service.service.ITripStudentService;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class SchoolBusNotificationRecipientService implements ISchoolBusNotificationRecipientService {

    private static final List<String> OPERATOR_ROLES =
            List.of("SCHOOL_BUS_ADMIN", "SCHOOL_BUS_DISPATCHER");

    private final ISchoolBusUserService schoolBusUserService;
    private final ISchoolBusUserRoleService schoolBusUserRoleService;
    private final ITripStudentService tripStudentService;

    public SchoolBusNotificationRecipientService(
            ISchoolBusUserService schoolBusUserService,
            ISchoolBusUserRoleService schoolBusUserRoleService,
            ITripStudentService tripStudentService) {
        this.schoolBusUserService = schoolBusUserService;
        this.schoolBusUserRoleService = schoolBusUserRoleService;
        this.tripStudentService = tripStudentService;
    }

    @Override
    public Set<Long> findOperatorAccountUserIds(Long tenantId) {
        List<Long> internalUserIds =
                schoolBusUserRoleService.findActiveUserIdsByTenantAndRoleNames(tenantId, OPERATOR_ROLES);
        return accountUserIds(schoolBusUserService.findAllByIds(internalUserIds), tenantId);
    }

    @Override
    public Set<Long> findParentAccountUserIds(ParentProfileEntity parent) {
        return profileAccountUserIds(parent == null ? null : parent.getUserId());
    }

    @Override
    public Set<Long> findDriverAccountUserIds(DriverProfileEntity driver) {
        return profileAccountUserIds(driver == null ? null : driver.getUserId());
    }

    @Override
    public Set<Long> findAttendantAccountUserIds(BusAttendantProfileEntity attendant) {
        return profileAccountUserIds(attendant == null ? null : attendant.getUserId());
    }

    @Override
    public Set<Long> findTripParentAccountUserIds(TripExecutionEntity trip) {
        if (trip == null || trip.getId() == null || trip.getTenantId() == null) {
            return new LinkedHashSet<>();
        }
        return findParentAccountUserIds(tripStudentService.findByTrip(trip.getId(), trip.getTenantId()));
    }

    @Override
    public Set<Long> findParentAccountUserIds(Collection<TripStudentEntity> tripStudents) {
        Set<Long> recipients = new LinkedHashSet<>();
        if (tripStudents == null) {
            return recipients;
        }
        for (TripStudentEntity tripStudent : tripStudents) {
            if (tripStudent != null && tripStudent.getStudent() != null) {
                recipients.addAll(findParentAccountUserIds(tripStudent.getStudent().getParentProfile()));
            }
        }
        return recipients;
    }

    private Set<Long> profileAccountUserIds(Long internalUserId) {
        if (internalUserId == null) {
            return new LinkedHashSet<>();
        }
        return schoolBusUserService.findById(internalUserId)
                .filter(this::isNotifiableUser)
                .map(user -> new LinkedHashSet<>(List.of(user.getAccountUserId())))
                .orElseGet(LinkedHashSet::new);
    }

    private Set<Long> accountUserIds(Collection<SchoolBusUserEntity> users, Long tenantId) {
        Set<Long> recipients = new LinkedHashSet<>();
        if (users == null || tenantId == null) {
            return recipients;
        }
        users.stream()
                .filter(user -> tenantId.equals(user.getTenantId()))
                .filter(this::isNotifiableUser)
                .map(SchoolBusUserEntity::getAccountUserId)
                .forEach(recipients::add);
        return recipients;
    }

    private boolean isNotifiableUser(SchoolBusUserEntity user) {
        return user != null
                && !Boolean.TRUE.equals(user.getIsDeleted())
                && Boolean.TRUE.equals(user.getIsActive())
                && user.getAccountUserId() != null
                && user.getAccountUserId() > 0;
    }
}
