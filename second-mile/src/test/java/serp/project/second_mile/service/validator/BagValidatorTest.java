/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.second_mile.caller.dto.tms_order.PlannedOrderRoute;
import serp.project.second_mile.caller.dto.tms_order.TmsOrderOperationView;
import serp.project.second_mile.enums.BagDestinationType;
import serp.project.second_mile.enums.OrderStatus;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.HubPostOfficeMappingRepository;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.RouteRepository;
import serp.project.second_mile.repository.VehicleRepository;
import serp.project.second_mile.service.dto.BagDestinationTarget;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BagValidatorTest {
    private static final Long TENANT_ID = 1L;
    private static final Long ORIGIN_HUB_ID = 10L;
    private static final Long TRANSIT_HUB_ID = 11L;
    private static final String DESTINATION_POST_OFFICE_CODE = "PO-DST";

    @Mock
    private HubRepository hubRepository;

    @Mock
    private HubPostOfficeMappingRepository hubPostOfficeMappingRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private HubStaffAssignmentRepository hubStaffAssignmentRepository;

    @Mock
    private SecondMileAccessUtils secondMileAccessUtils;

    @Test
    void resolveDestinationTargetUsesNextHubFromPlannedRoute() {
        BagValidator validator = validator();
        TmsOrderOperationView order = order();

        BagDestinationTarget target = validator.resolveDestinationTargetForOrder(
                TENANT_ID,
                order,
                ORIGIN_HUB_ID
        );

        assertEquals(BagDestinationType.HUB, target.destinationType());
        assertEquals(TRANSIT_HUB_ID, target.destinationHubId());
        assertEquals(DESTINATION_POST_OFFICE_CODE, target.destinationPostOfficeCode());
        verifyNoInteractions(hubPostOfficeMappingRepository);
    }

    @Test
    void resolveDestinationTargetUsesPostOfficeFinalLegFromPlannedRoute() {
        BagValidator validator = validator();
        TmsOrderOperationView order = order();

        BagDestinationTarget target = validator.resolveDestinationTargetForOrder(
                TENANT_ID,
                order,
                TRANSIT_HUB_ID
        );

        assertEquals(BagDestinationType.POST_OFFICE, target.destinationType());
        assertEquals(DESTINATION_POST_OFFICE_CODE, target.destinationPostOfficeCode());
        verifyNoInteractions(hubPostOfficeMappingRepository);
    }

    @Test
    void validateOrderForBagAssignmentAcceptsPlannedNextHub() {
        BagValidator validator = validator();
        TmsOrderOperationView order = order();

        validator.validateOrderDestinationMatchesTarget(
                TENANT_ID,
                ORIGIN_HUB_ID,
                BagDestinationType.HUB,
                TRANSIT_HUB_ID,
                DESTINATION_POST_OFFICE_CODE,
                order
        );

        verifyNoInteractions(hubPostOfficeMappingRepository);
    }

    private BagValidator validator() {
        return new BagValidator(
                hubRepository,
                hubPostOfficeMappingRepository,
                routeRepository,
                vehicleRepository,
                hubStaffAssignmentRepository,
                secondMileAccessUtils
        );
    }

    private TmsOrderOperationView order() {
        return TmsOrderOperationView.builder()
                .id(1L)
                .orderCode("ORD-001")
                .status(OrderStatus.INBOUND_AT_ORIGIN_HUB)
                .originPostOfficeCode("PO-ORG")
                .destinationPostOfficeCode(DESTINATION_POST_OFFICE_CODE)
                .plannedRoute(PlannedOrderRoute.builder()
                        .destinationPostOfficeCode(DESTINATION_POST_OFFICE_CODE)
                        .legs(List.of(
                                PlannedOrderRoute.Leg.builder()
                                        .sequence(1)
                                        .originType("HUB")
                                        .originHubId(ORIGIN_HUB_ID)
                                        .destinationType("HUB")
                                        .destinationHubId(TRANSIT_HUB_ID)
                                        .build(),
                                PlannedOrderRoute.Leg.builder()
                                        .sequence(2)
                                        .originType("HUB")
                                        .originHubId(TRANSIT_HUB_ID)
                                        .destinationType("POST_OFFICE")
                                        .destinationPostOfficeCode(DESTINATION_POST_OFFICE_CODE)
                                        .build()
                        ))
                        .build())
                .tenantId(TENANT_ID)
                .build();
    }
}
