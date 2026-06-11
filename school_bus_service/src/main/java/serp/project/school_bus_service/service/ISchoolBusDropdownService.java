package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.DropdownOptionResponse;

import java.util.List;

public interface ISchoolBusDropdownService {
    List<DropdownOptionResponse> getSchoolsDropdown(Long tenantId);
    List<DropdownOptionResponse> getSchoolPickupPointsDropdown(Long schoolId, Long tenantId);
    List<DropdownOptionResponse> getParentsDropdown(Long tenantId);
    List<DropdownOptionResponse> getDriversDropdown(Long tenantId);
    List<DropdownOptionResponse> getAttendantsDropdown(Long tenantId);
    List<DropdownOptionResponse> getBusesDropdown(Long depotId, Long tenantId);
}
