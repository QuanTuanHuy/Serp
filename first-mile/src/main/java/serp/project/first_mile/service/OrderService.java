package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.dto.response.OrderTimelineResponse;

import java.util.List;

public interface OrderService {
	List<OrderDropOffPostOfficeSuggestionResponse> getDropOffPostOfficeSuggestions(Long orderId, Integer limit, Long tenantId);

	List<OrderTimelineResponse> getOrderTimeline(Long orderId, Long tenantId);

	PickupCheckinResponse checkInPickupOrder(
			Long orderId,
			Double checkinLatitude,
			Double checkinLongitude,
			MultipartFile photo,
			Long tenantId
	);

	void publishOrderEvent(String orderCode);
}
