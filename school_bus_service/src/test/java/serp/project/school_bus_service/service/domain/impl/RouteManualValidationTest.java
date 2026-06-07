package serp.project.school_bus_service.service.domain.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import serp.project.school_bus_service.dto.response.PlanningIssueResponse;
import serp.project.school_bus_service.dto.response.RouteManualValidationResponse;
import serp.project.school_bus_service.entity.RoutePlanEntity;
import serp.project.school_bus_service.entity.RoutePlanningIssueEntity;
import serp.project.school_bus_service.entity.RouteStopEntity;
import serp.project.school_bus_service.enums.PlanningIssueSeverity;
import serp.project.school_bus_service.service.IRouteManualValidationService;
import serp.project.school_bus_service.service.IRoutePlanningIssueService;
import serp.project.school_bus_service.service.IRouteService;
import serp.project.school_bus_service.service.IRouteStopService;
import serp.project.school_bus_service.service.domain.IRouteGeometryService;
import serp.project.school_bus_service.service.impl.RouteManualValidationServiceImpl;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;
import serp.project.school_bus_service.shared.i18n.MessageCommon;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RouteManualValidationTest {

    private IRouteService routeService;
    private IRouteStopService routeStopService;
    private IRouteGeometryService routeGeometryService;
    private IRoutePlanningIssueService issueService;
    private MessageCommon messageCommon;
    private IRouteManualValidationService validationService;

    @BeforeEach
    void setUp() {
        routeService = Mockito.mock(IRouteService.class);
        routeStopService = Mockito.mock(IRouteStopService.class);
        routeGeometryService = Mockito.mock(IRouteGeometryService.class);
        issueService = Mockito.mock(IRoutePlanningIssueService.class);
        messageCommon = Mockito.mock(MessageCommon.class);

        validationService = new RouteManualValidationServiceImpl(
                routeService, routeStopService, routeGeometryService, issueService, messageCommon
        );
    }

    @Test
    void testValidateRouteSuccessNoIssues() {
        RoutePlanEntity route = new RoutePlanEntity();
        route.setId(1L);
        route.setRouteCode("R-001");
        route.setRouteName("Route 1");
        route.setBlockingIssueCount(0);

        RouteStopEntity stop1 = new RouteStopEntity();
        stop1.setId(10L);
        stop1.setStopOrder(0);
        stop1.setLocationType(serp.project.school_bus_service.enums.RouteLocationType.DEPOT);
        stop1.setStopPurpose(serp.project.school_bus_service.enums.RouteStopPurpose.START_TERMINAL);

        RouteStopEntity stop2 = new RouteStopEntity();
        stop2.setId(11L);
        stop2.setStopOrder(1);
        stop2.setLocationType(serp.project.school_bus_service.enums.RouteLocationType.SCHOOL);
        stop2.setStopPurpose(serp.project.school_bus_service.enums.RouteStopPurpose.END_TERMINAL);

        List<RouteStopEntity> stops = Arrays.asList(stop1, stop2);

        when(routeService.getRouteEntity(1L, 1L)).thenReturn(route);
        when(routeStopService.findByRoute(1L, 1L)).thenReturn(stops);
        when(issueService.findByRoute(1L)).thenReturn(Collections.emptyList());

        RouteManualValidationResponse response = validationService.validateRoute(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getRoutePlanId());
        assertEquals("R-001", response.getRouteCode());
        assertEquals("Route 1", response.getRouteName());
        assertTrue(response.isValid());
        assertEquals(0, response.getBlockingIssueCount());
        assertEquals(0, response.getWarningIssueCount());
        assertTrue(response.getIssues().isEmpty());
        assertEquals(2, response.getStops().size());

        verify(routeGeometryService, times(1)).computeAndUpdate(route, stops);
        verify(routeStopService, times(1)).saveAllRouteStops(stops);
        verify(routeService, times(1)).saveRouteEntity(route);
    }

    @Test
    void testValidateRouteWithIssues() {
        RoutePlanEntity route = new RoutePlanEntity();
        route.setId(1L);
        route.setRouteCode("R-001");
        route.setRouteName("Route 1");
        route.setBlockingIssueCount(1);

        RouteStopEntity stop1 = new RouteStopEntity();
        stop1.setId(10L);
        stop1.setStopOrder(0);
        stop1.setLocationType(serp.project.school_bus_service.enums.RouteLocationType.DEPOT);
        stop1.setStopPurpose(serp.project.school_bus_service.enums.RouteStopPurpose.START_TERMINAL);

        List<RouteStopEntity> stops = Collections.singletonList(stop1);

        RoutePlanningIssueEntity issue = new RoutePlanningIssueEntity();
        issue.setId(100L);
        issue.setIssueType("MIN_STOPS_VIOLATION");
        issue.setSeverity(PlanningIssueSeverity.BLOCKING);
        issue.setMessage("Route must have at least 2 stops");
        issue.setIsResolved(false);
        issue.setRoute(route);
        issue.setRouteStop(stop1);

        when(routeService.getRouteEntity(1L, 1L)).thenReturn(route);
        when(routeStopService.findByRoute(1L, 1L)).thenReturn(stops);
        when(issueService.findByRoute(1L)).thenReturn(Collections.singletonList(issue));

        RouteManualValidationResponse response = validationService.validateRoute(1L, 1L);

        assertNotNull(response);
        assertFalse(response.isValid());
        assertEquals(1, response.getBlockingIssueCount());
        assertEquals(0, response.getWarningIssueCount());
        assertEquals(1, response.getIssues().size());

        PlanningIssueResponse issueResponse = response.getIssues().get(0);
        assertEquals("MIN_STOPS_VIOLATION", issueResponse.getIssueType());
        assertEquals("BLOCKING", issueResponse.getSeverity());
        assertEquals(10L, issueResponse.getRouteStopId());
    }

    @Test
    void testValidateBeforeAssignResourcesThrowsException() {
        RoutePlanEntity route = new RoutePlanEntity();
        route.setId(1L);
        route.setBlockingIssueCount(2);

        when(routeService.getRouteEntity(1L, 1L)).thenReturn(route);
        when(messageCommon.getMessage(anyString())).thenReturn("Route has blocking issues");

        AppException exception = assertThrows(AppException.class, () ->
                validationService.validateBeforeAssignResources(1L, 1L)
        );

        assertEquals(AppErrorCode.Route.ROUTE_HAS_BLOCKING_ISSUES, exception.getErrorCode());
        verify(messageCommon, times(1)).getMessage(AppErrorCode.Route.ROUTE_HAS_BLOCKING_ISSUES);
    }

    @Test
    void testValidateBeforeAssignResourcesSuccess() {
        RoutePlanEntity route = new RoutePlanEntity();
        route.setId(1L);
        route.setBlockingIssueCount(0);

        when(routeService.getRouteEntity(1L, 1L)).thenReturn(route);

        assertDoesNotThrow(() -> validationService.validateBeforeAssignResources(1L, 1L));
    }
}
