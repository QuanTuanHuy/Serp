package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.response.DropdownOptionResponse;
import serp.project.school_bus_service.entity.BusAttendantProfileEntity;
import serp.project.school_bus_service.entity.BusEntity;
import serp.project.school_bus_service.entity.DriverProfileEntity;
import serp.project.school_bus_service.entity.ParentProfileEntity;
import serp.project.school_bus_service.entity.SchoolEntity;
import serp.project.school_bus_service.entity.SchoolPickupPointEntity;
import serp.project.school_bus_service.repository.BusAttendantProfileRepository;
import serp.project.school_bus_service.repository.BusRepository;
import serp.project.school_bus_service.repository.DriverProfileRepository;
import serp.project.school_bus_service.repository.ParentProfileRepository;
import serp.project.school_bus_service.repository.SchoolPickupPointRepository;
import serp.project.school_bus_service.repository.SchoolRepository;
import serp.project.school_bus_service.service.ISchoolBusDropdownService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SchoolBusDropdownServiceImpl implements ISchoolBusDropdownService {

    private final SchoolRepository schoolRepository;
    private final SchoolPickupPointRepository schoolPickupPointRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAttendantProfileRepository busAttendantProfileRepository;
    private final BusRepository busRepository;

    public SchoolBusDropdownServiceImpl(
            SchoolRepository schoolRepository,
            SchoolPickupPointRepository schoolPickupPointRepository,
            ParentProfileRepository parentProfileRepository,
            DriverProfileRepository driverProfileRepository,
            BusAttendantProfileRepository busAttendantProfileRepository,
            BusRepository busRepository) {
        this.schoolRepository = schoolRepository;
        this.schoolPickupPointRepository = schoolPickupPointRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.busAttendantProfileRepository = busAttendantProfileRepository;
        this.busRepository = busRepository;
    }

    @Override
    public List<DropdownOptionResponse> getSchoolsDropdown(Long tenantId) {
        List<SchoolEntity> schools = schoolRepository.findByTenantIdAndIsDeletedFalseOrderByNameAsc(tenantId);
        return schools.stream()
                .filter(s -> s.getIsActive() == Boolean.TRUE)
                .map(s -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("latitude", s.getLatitude() != null ? s.getLatitude() : 0.0);
                    metadata.put("longitude", s.getLongitude() != null ? s.getLongitude() : 0.0);

                    return DropdownOptionResponse.builder()
                            .id(s.getId())
                            .label(s.getName())
                            .code(s.getCode())
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DropdownOptionResponse> getSchoolPickupPointsDropdown(Long schoolId, Long tenantId) {
        List<SchoolPickupPointEntity> links = schoolPickupPointRepository.findActiveLinkedPickupPoints(schoolId, tenantId);
        return links.stream()
                .map(link -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("latitude", link.getPickupPoint().getLatitude() != null ? link.getPickupPoint().getLatitude() : 0.0);
                    metadata.put("longitude", link.getPickupPoint().getLongitude() != null ? link.getPickupPoint().getLongitude() : 0.0);
                    metadata.put("usageType", link.getPickupPoint().getUsageType() != null ? link.getPickupPoint().getUsageType() : "");

                    return DropdownOptionResponse.builder()
                            .id(link.getPickupPoint().getId())
                            .label(link.getPickupPoint().getName())
                            .code(link.getPickupPoint().getCode())
                            .description(link.getPickupPoint().getAddress())
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DropdownOptionResponse> getParentsDropdown(Long tenantId) {
        List<ParentProfileEntity> parents = parentProfileRepository.findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(tenantId);
        return parents.stream()
                .filter(p -> p.getIsActive() == Boolean.TRUE)
                .map(p -> DropdownOptionResponse.builder()
                        .id(p.getId())
                        .label(p.getFullName())
                        .code(p.getPhone())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DropdownOptionResponse> getDriversDropdown(Long tenantId) {
        List<DriverProfileEntity> drivers = driverProfileRepository.findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(tenantId);
        return drivers.stream()
                .filter(d -> d.getIsActive() == Boolean.TRUE)
                .map(d -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("status", d.getStatus() != null ? d.getStatus() : "ACTIVE");

                    return DropdownOptionResponse.builder()
                            .id(d.getId())
                            .label(d.getFullName())
                            .code(d.getPhone())
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DropdownOptionResponse> getAttendantsDropdown(Long tenantId) {
        List<BusAttendantProfileEntity> attendants = busAttendantProfileRepository.findByTenantIdAndIsDeletedFalseOrderByFullNameAsc(tenantId);
        return attendants.stream()
                .filter(a -> a.getIsActive() == Boolean.TRUE)
                .map(a -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("status", a.getStatus() != null ? a.getStatus() : "ACTIVE");

                    return DropdownOptionResponse.builder()
                            .id(a.getId())
                            .label(a.getFullName())
                            .code(a.getPhone())
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DropdownOptionResponse> getBusesDropdown(Long depotId, Long tenantId) {
        List<BusEntity> buses;
        if (depotId != null) {
            buses = busRepository.findByTenantIdAndHomeDepotIdAndIsDeletedFalseOrderByPlateNumberAsc(tenantId, depotId);
        } else {
            buses = busRepository.findByTenantIdAndIsDeletedFalseOrderByPlateNumberAsc(tenantId);
        }
        return buses.stream()
                .filter(b -> b.getIsActive() == Boolean.TRUE)
                .map(b -> {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("status", b.getStatus() != null ? b.getStatus() : "ACTIVE");
                    metadata.put("capacity", b.getCapacity() != null ? b.getCapacity() : 0);

                    return DropdownOptionResponse.builder()
                            .id(b.getId())
                            .label(b.getPlateNumber())
                            .code(b.getBusType())
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
