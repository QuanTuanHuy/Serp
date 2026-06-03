/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.first_mile.domain.Order;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.enums.OrderPickupMethod;
import serp.project.first_mile.enums.OrderStatus;
import serp.project.first_mile.enums.PostOfficeStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kafka.impl.order.SyncOrder;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.repository.OrderRepository;
import serp.project.first_mile.repository.PickupCheckinRepository;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.repository.TripOrderRepository;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.OrderTimelineService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PostOfficeRepository postOfficeRepository;

    @Mock
    private FirstMileAccessUtils firstMileAccessUtils;

    @Mock
    private PostOfficeStaffRepository postOfficeStaffRepository;

    @Mock
    private PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;

    @Mock
    private TripOrderRepository tripOrderRepository;

    @Mock
    private PickupCheckinRepository pickupCheckinRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private SyncOrder syncOrder;

    @Mock
    private OrderTimelineService orderTimelineService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        lenient().when(firstMileAccessUtils.isAdmin()).thenReturn(true);
        lenient().when(firstMileAccessUtils.isCustomer()).thenReturn(false);
        lenient().when(firstMileAccessUtils.isPostOfficerManager()).thenReturn(false);
        lenient().when(firstMileAccessUtils.isCourier()).thenReturn(false);
    }

    @Test
    void checkInPickupOrderShouldThrowDetailedErrorWhenOrderIdIsMissing() {
        Long tenantId = 1L;

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.checkInPickupOrder(null, 10.7721, 106.6983, null, tenantId)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        assertEquals("orderId is required.", exception.getDetail());
    }

    @Test
    void checkInPickupOrderShouldThrowDetailedErrorWhenLatitudeIsMissing() {
        Long tenantId = 1L;

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.checkInPickupOrder(2L, null, 106.6983, null, tenantId)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        assertEquals("latitude is required.", exception.getDetail());
    }

    @Test
    void checkInPickupOrderShouldThrowDetailedErrorWhenCoordinateIsOutOfRange() {
        Long tenantId = 1L;

        AppException exception = assertThrows(
                AppException.class,
                () -> orderService.checkInPickupOrder(2L, 95.0, 106.6983, null, tenantId)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        assertTrue(exception.getDetail().contains("Invalid check-in coordinates"));
        assertTrue(exception.getDetail().contains("latitude=95.0"));
        assertTrue(exception.getDetail().contains("longitude=106.6983"));
    }

    @Test
    void getDropOffPostOfficeSuggestionsShouldReturnSortedSuggestions() {
        Long tenantId = 1L;
        Long orderId = 108L;

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.CREATED);
        order.setPickupMethod(OrderPickupMethod.DROP_OFF_AT_POST_OFFICE);
        order.setSenderLocation(point(10.77371, 106.70098));

        PostOffice preferred = new PostOffice();
        preferred.setId(1L);
        preferred.setCode("PO-HCM-01");
        preferred.setName("Preferred");
        preferred.setStatus(PostOfficeStatus.ACTIVE);
        preferred.setDailyCapacity(20);
        preferred.setCurrentLoad(5);
        preferred.setPriority(1);
        preferred.setServiceRadiusM(5000);
        preferred.setLocation(point(10.77400, 106.70110));

        PostOffice secondary = new PostOffice();
        secondary.setId(2L);
        secondary.setCode("PO-HCM-02");
        secondary.setName("Secondary");
        secondary.setStatus(PostOfficeStatus.ACTIVE);
        secondary.setDailyCapacity(20);
        secondary.setCurrentLoad(1);
        secondary.setPriority(2);
        secondary.setServiceRadiusM(5000);
        secondary.setLocation(point(10.77600, 106.70300));

        PostOffice overloaded = new PostOffice();
        overloaded.setId(3L);
        overloaded.setCode("PO-HCM-03");
        overloaded.setName("Overloaded");
        overloaded.setStatus(PostOfficeStatus.ACTIVE);
        overloaded.setDailyCapacity(10);
        overloaded.setCurrentLoad(10);
        overloaded.setPriority(0);
        overloaded.setServiceRadiusM(5000);
        overloaded.setLocation(point(10.77380, 106.70100));

        when(orderRepository.findByIdAndTenantId(orderId, tenantId)).thenReturn(Optional.of(order));
        when(postOfficeRepository.findAllByTenantId(tenantId)).thenReturn(List.of(secondary, preferred, overloaded));

        List<OrderDropOffPostOfficeSuggestionResponse> suggestions =
                orderService.getDropOffPostOfficeSuggestions(orderId, 5, tenantId);

        assertEquals(2, suggestions.size());
        assertEquals("PO-HCM-01", suggestions.get(0).code());
        assertEquals("PO-HCM-02", suggestions.get(1).code());
    }

    private Point point(double latitude, double longitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
}
