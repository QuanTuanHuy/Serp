package serp.project.school_bus_service.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.application.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.application.dto.response.BusResponse;
import serp.project.school_bus_service.application.dto.response.DepotResponse;
import serp.project.school_bus_service.application.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.application.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.application.dto.response.PickupPointResponse;
import serp.project.school_bus_service.application.dto.response.SchoolResponse;
import serp.project.school_bus_service.application.dto.response.StudentResponse;
import serp.project.school_bus_service.infrastructure.store.model.BusAttendantProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.BusEntity;
import serp.project.school_bus_service.infrastructure.store.model.DepotEntity;
import serp.project.school_bus_service.infrastructure.store.model.DriverProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.ParentProfileEntity;
import serp.project.school_bus_service.infrastructure.store.model.PickupPointEntity;
import serp.project.school_bus_service.infrastructure.store.model.SchoolEntity;
import serp.project.school_bus_service.infrastructure.store.model.StudentEntity;
import serp.project.school_bus_service.kernel.shared.base.BaseMapper;

@Component
public class MasterDataMapper extends BaseMapper {

    public SchoolResponse toSchoolResponse(SchoolEntity entity) {
        SchoolResponse response = enrich(new SchoolResponse(), entity);
        response.setName(entity.getName());
        response.setCode(entity.getCode());
        response.setAddress(entity.getAddress());
        response.setContactPhone(entity.getContactPhone());
        response.setContactEmail(entity.getContactEmail());
        response.setLatitude(entity.getLatitude());
        response.setLongitude(entity.getLongitude());
        return response;
    }

    public ParentProfileResponse toParentProfileResponse(ParentProfileEntity entity) {
        ParentProfileResponse response = enrich(new ParentProfileResponse(), entity);
        response.setUserId(entity.getUserId());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setEmail(entity.getEmail());
        response.setAddress(entity.getAddress());
        return response;
    }

    public StudentResponse toStudentResponse(StudentEntity entity) {
        StudentResponse response = enrich(new StudentResponse(), entity);
        response.setSchoolId(entity.getSchool().getId());
        response.setSchoolName(entity.getSchool().getName());
        response.setParentProfileId(entity.getParentProfile().getId());
        response.setParentProfileName(entity.getParentProfile().getFullName());
        response.setPickupPointId(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getId());
        response.setPickupPointName(entity.getPickupPoint() == null ? null : entity.getPickupPoint().getName());
        response.setFullName(entity.getFullName());
        response.setStudentCode(entity.getStudentCode());
        response.setGrade(entity.getGrade());
        response.setHomeAddress(entity.getHomeAddress());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setGender(entity.getGender());
        response.setEmergencyContactName(entity.getEmergencyContactName());
        response.setEmergencyContactPhone(entity.getEmergencyContactPhone());
        response.setSpecialNote(entity.getSpecialNote());
        return response;
    }

    public BusResponse toBusResponse(BusEntity entity) {
        BusResponse response = enrich(new BusResponse(), entity);
        response.setPlateNumber(entity.getPlateNumber());
        response.setBusType(entity.getBusType());
        response.setCapacity(entity.getCapacity());
        response.setStatus(entity.getStatus());
        return response;
    }

    public DriverProfileResponse toDriverProfileResponse(DriverProfileEntity entity) {
        DriverProfileResponse response = enrich(new DriverProfileResponse(), entity);
        response.setUserId(entity.getUserId());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setLicenseNumber(entity.getLicenseNumber());
        response.setStatus(entity.getStatus());
        return response;
    }

    public AttendantProfileResponse toAttendantProfileResponse(BusAttendantProfileEntity entity) {
        AttendantProfileResponse response = enrich(new AttendantProfileResponse(), entity);
        response.setUserId(entity.getUserId());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setStatus(entity.getStatus());
        return response;
    }

    public PickupPointResponse toPickupPointResponse(PickupPointEntity entity) {
        PickupPointResponse response = enrich(new PickupPointResponse(), entity);
        response.setSchoolId(entity.getSchool().getId());
        response.setSchoolName(entity.getSchool().getName());
        response.setName(entity.getName());
        response.setAddress(entity.getAddress());
        response.setLatitude(entity.getLatitude());
        response.setLongitude(entity.getLongitude());
        response.setPickupWindowStart(entity.getPickupWindowStart());
        response.setPickupWindowEnd(entity.getPickupWindowEnd());
        return response;
    }

    public DepotResponse toDepotResponse(DepotEntity entity) {
        DepotResponse response = enrich(new DepotResponse(), entity);
        response.setName(entity.getName());
        response.setAddress(entity.getAddress());
        response.setLatitude(entity.getLatitude());
        response.setLongitude(entity.getLongitude());
        response.setContactPhone(entity.getContactPhone());
        response.setDescription(entity.getDescription());
        return response;
    }
}
