package serp.project.school_bus_service.service.domain.impl;

import org.springframework.stereotype.Service;
import serp.project.school_bus_service.dto.response.RoutingRuntimeConfig;
import serp.project.school_bus_service.shared.code.AppConfigCode;
import serp.project.school_bus_service.service.ISchoolBusAppConfigService;
import serp.project.school_bus_service.service.domain.IRoutingConfigResolver;

import java.math.BigDecimal;

@Service
public class RoutingConfigResolverImpl implements IRoutingConfigResolver {

    private final ISchoolBusAppConfigService appConfigService;

    public RoutingConfigResolverImpl(ISchoolBusAppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }

    /**
     * Resolves the global application routing runtime parameters, including
     * average vehicle speed, default stop dwell time, road distance multiplier,
     * and OSRM engine toggles.
     * These configurations are applied globally for all routing operations.
     */
    @Override
    public RoutingRuntimeConfig resolve() {
        BigDecimal speed = appConfigService.getDecimal(
                AppConfigCode.ROUTING_AVERAGE_SPEED_KMPH, BigDecimal.valueOf(25.0));
        Integer dwell = appConfigService.getInteger(
                AppConfigCode.ROUTING_DWELL_TIME_MINUTES, 2);
        BigDecimal roadFactor = appConfigService.getDecimal(
                AppConfigCode.ROUTING_ROAD_FACTOR, BigDecimal.valueOf(1.3));
        Boolean osrmEnabled = appConfigService.getBoolean(
                AppConfigCode.ROUTING_OSRM_ENABLED, Boolean.TRUE);

        return RoutingRuntimeConfig.builder()
                .averageSpeedKmph(speed.doubleValue())
                .dwellTimeMinutes(dwell)
                .roadFactor(roadFactor.doubleValue())
                .osrmEnabled(osrmEnabled)
                .build();
    }
}
