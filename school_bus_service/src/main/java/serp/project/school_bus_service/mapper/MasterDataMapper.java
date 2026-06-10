package serp.project.school_bus_service.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.response.AttendantProfileResponse;
import serp.project.school_bus_service.dto.response.BusResponse;
import serp.project.school_bus_service.dto.response.DepotResponse;
import serp.project.school_bus_service.dto.response.DriverProfileResponse;
import serp.project.school_bus_service.dto.response.ParentProfileResponse;
import serp.project.school_bus_service.dto.response.PickupPointResponse;
import serp.project.school_bus_service.dto.response.SchoolPickupPointResponse;
import serp.project.school_bus_service.dto.response.SchoolResponse;
import serp.project.school_bus_service.dto.response.StudentResponse;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.entity.PickupPointEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.entity.StudentEntity;
import serp.project.school_bus_service.shared.base.BaseMapper;

import java.util.List;

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
        response.setDefaultDropoffPointId(entity.getDefaultDropoffPoint() == null ? null : entity.getDefaultDropoffPoint().getId());
        response.setDefaultDropoffPointName(entity.getDefaultDropoffPoint() == null ? null : entity.getDefaultDropoffPoint().getName());
        response.setFullName(entity.getFullName());
        response.setStudentCode(entity.getStudentCode());
        response.setGrade(entity.getGrade());
        response.setClassName(entity.getClassName());
        response.setHomeAddress(entity.getHomeAddress());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setGender(entity.getGender());
        response.setSpecialNote(entity.getSpecialNote());
        return response;
    }

    public BusResponse toBusResponse(BusEntity entity) {
        BusResponse response = enrich(new BusResponse(), entity);
        response.setPlateNumber(entity.getPlateNumber());
        response.setBusType(entity.getBusType());
        response.setCapacity(entity.getCapacity());
        response.setStatus(entity.getStatus());
        if (entity.getHomeDepot() != null) {
            response.setHomeDepotId(entity.getHomeDepot().getId());
            response.setHomeDepotName(entity.getHomeDepot().getName());
        }
        return response;
    }

    public DriverProfileResponse toDriverProfileResponse(DriverProfileEntity entity) {
        DriverProfileResponse response = enrich(new DriverProfileResponse(), entity);
        response.setUserId(entity.getUserId());
        response.setFullName(entity.getFullName());
        response.setPhone(entity.getPhone());
        response.setLicenseNumber(entity.getLicenseNumber());
        response.setLicenseClass(entity.getLicenseClass());
        response.setLicenseExpiryDate(entity.getLicenseExpiryDate());
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
        response.setName(entity.getName());
        response.setAddress(entity.getAddress());
        response.setLatitude(entity.getLatitude());
        response.setLongitude(entity.getLongitude());
        response.setCode(entity.getCode());
        response.setUsageType(entity.getUsageType());
        response.setPickupInstruction(entity.getPickupInstruction());
        response.setSchools(java.util.List.of());
        return response;
    }

    public DepotResponse toDepotResponse(DepotEntity entity) {
        DepotResponse response = enrich(new DepotResponse(), entity);
        response.setCode(entity.getCode());
        response.setName(entity.getName());
        response.setAddress(entity.getAddress());
        response.setLatitude(entity.getLatitude());
        response.setLongitude(entity.getLongitude());
        response.setContactPhone(entity.getContactPhone());
        response.setDescription(entity.getDescription());
        return response;
    }

    public SchoolPickupPointResponse toSchoolPickupPointResponse(SchoolPickupPointEntity entity) {
        SchoolPickupPointResponse response = enrich(new SchoolPickupPointResponse(), entity);
        response.setSchoolId(entity.getSchool().getId());
        response.setSchoolName(entity.getSchool().getName());
        response.setPickupPointId(entity.getPickupPoint().getId());
        response.setPickupPointName(entity.getPickupPoint().getName());
        response.setPickupPointAddress(entity.getPickupPoint().getAddress());
        response.setPickupPointLatitude(entity.getPickupPoint().getLatitude());
        response.setPickupPointLongitude(entity.getPickupPoint().getLongitude());
        response.setPickupPointUsageType(entity.getPickupPoint().getUsageType());
        response.setIsDefault(entity.getIsDefaultPoint());
        return response;
    }
}
