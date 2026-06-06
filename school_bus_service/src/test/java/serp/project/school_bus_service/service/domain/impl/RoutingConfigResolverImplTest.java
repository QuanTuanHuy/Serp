package serp.project.school_bus_service.service.domain.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import serp.project.school_bus_service.dto.response.RoutingRuntimeConfig;
import serp.project.school_bus_service.shared.code.AppConfigCode;
import serp.project.school_bus_service.service.ISchoolBusAppConfigService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class RoutingConfigResolverImplTest {

    private ISchoolBusAppConfigService appConfigService;
    private RoutingConfigResolverImpl resolver;

    @BeforeEach
    void setUp() {
        appConfigService = Mockito.mock(ISchoolBusAppConfigService.class);
        resolver = new RoutingConfigResolverImpl(appConfigService);
    }

    @Test
    void testResolveAllConfigured() {
        when(appConfigService.getDecimal(eq(AppConfigCode.ROUTING_AVERAGE_SPEED_KMPH), any()))
                .thenReturn(BigDecimal.valueOf(45.5));
        when(appConfigService.getInteger(eq(AppConfigCode.ROUTING_DWELL_TIME_MINUTES), any()))
                .thenReturn(3);
        when(appConfigService.getDecimal(eq(AppConfigCode.ROUTING_ROAD_FACTOR), any()))
                .thenReturn(BigDecimal.valueOf(1.5));
        when(appConfigService.getBoolean(eq(AppConfigCode.ROUTING_OSRM_ENABLED), any()))
                .thenReturn(Boolean.FALSE);

        RoutingRuntimeConfig config = resolver.resolve();

        assertEquals(45.5, config.getAverageSpeedKmph());
        assertEquals(3, config.getDwellTimeMinutes());
        assertEquals(1.5, config.getRoadFactor());
        assertTrue(!config.isOsrmEnabled());
    }

    @Test
    void testResolveWithDefaultValues() {
        // Mock returning defaults
        when(appConfigService.getDecimal(eq(AppConfigCode.ROUTING_AVERAGE_SPEED_KMPH), any()))
                .thenAnswer(inv -> inv.getArgument(1));
        when(appConfigService.getInteger(eq(AppConfigCode.ROUTING_DWELL_TIME_MINUTES), any()))
                .thenAnswer(inv -> inv.getArgument(1));
        when(appConfigService.getDecimal(eq(AppConfigCode.ROUTING_ROAD_FACTOR), any()))
                .thenAnswer(inv -> inv.getArgument(1));
        when(appConfigService.getBoolean(eq(AppConfigCode.ROUTING_OSRM_ENABLED), any()))
                .thenAnswer(inv -> inv.getArgument(1));

        RoutingRuntimeConfig config = resolver.resolve();

        assertEquals(25.0, config.getAverageSpeedKmph());
        assertEquals(2, config.getDwellTimeMinutes());
        assertEquals(1.3, config.getRoadFactor());
        assertTrue(config.isOsrmEnabled());
    }
}
