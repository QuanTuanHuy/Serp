package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.dto.response.FleetSummaryResponse;
import serp.project.school_bus_service.repository.BusAttendantProfileRepository;
import serp.project.school_bus_service.repository.BusRepository;
import serp.project.school_bus_service.repository.DepotRepository;
import serp.project.school_bus_service.repository.DriverProfileRepository;
import serp.project.school_bus_service.repository.projection.AttendantFleetSummaryProjection;
import serp.project.school_bus_service.repository.projection.BusFleetSummaryProjection;
import serp.project.school_bus_service.repository.projection.DepotFleetSummaryProjection;
import serp.project.school_bus_service.repository.projection.DriverFleetSummaryProjection;
import serp.project.school_bus_service.service.IFleetSummaryService;

@Service
public class FleetSummaryServiceImpl implements IFleetSummaryService {

    private final BusRepository busRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final BusAttendantProfileRepository attendantProfileRepository;
    private final DepotRepository depotRepository;

    public FleetSummaryServiceImpl(
            BusRepository busRepository,
            DriverProfileRepository driverProfileRepository,
            BusAttendantProfileRepository attendantProfileRepository,
            DepotRepository depotRepository) {
        this.busRepository = busRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.attendantProfileRepository = attendantProfileRepository;
        this.depotRepository = depotRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FleetSummaryResponse getSummary(Long tenantId) {
        BusFleetSummaryProjection busSummary = busRepository.getFleetSummary(tenantId);
        DriverFleetSummaryProjection driverSummary = driverProfileRepository.getFleetSummary(tenantId);
        AttendantFleetSummaryProjection attendantSummary = attendantProfileRepository.getFleetSummary(tenantId);
        DepotFleetSummaryProjection depotSummary = depotRepository.getFleetSummary(tenantId);

        long totalDrivers = value(driverSummary.getTotalDrivers());
        long availableDrivers = value(driverSummary.getAvailableDrivers());
        long totalAttendants = value(attendantSummary.getTotalAttendants());
        long availableAttendants = value(attendantSummary.getAvailableAttendants());

        return new FleetSummaryResponse(
                value(busSummary.getTotalBuses()),
                value(busSummary.getAvailableBuses()),
                totalDrivers,
                availableDrivers,
                Math.max(totalDrivers - availableDrivers, 0),
                totalAttendants,
                availableAttendants,
                Math.max(totalAttendants - availableAttendants, 0),
                value(depotSummary.getTotalDepots()),
                value(depotSummary.getDepotsWithCoordinates()));
    }

    private long value(Long value) {
        return value == null ? 0L : value;
    }
}
