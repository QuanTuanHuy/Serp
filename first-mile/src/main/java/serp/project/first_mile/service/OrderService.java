package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CancelOrderRequest;
import serp.project.first_mile.dto.request.CreateOrderRequest;
import serp.project.first_mile.dto.request.OrderFilterRequest;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.request.UpdateOrderRequest;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.OrderDetailResponse;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.dto.response.PickupCheckinResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;

import java.util.List;

public interface OrderService {
	byte[] exportTemplate(Long tenantId);

	ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId);

	ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId);

	PageResponse<OrderDetailResponse> getOrders(int page, int size, OrderFilterRequest filterRequest, Long tenantId);

	OrderConfirmationResponse confirmOrder(Long orderId, Long tenantId);

	List<OrderDropOffPostOfficeSuggestionResponse> getDropOffPostOfficeSuggestions(Long orderId, Integer limit, Long tenantId);

	OrderConfirmationResponse confirmDropOffOrderAtPostOffice(Long orderId, Long postOfficeId, Long tenantId);

	OrderDetailResponse createOrder(CreateOrderRequest request, Long tenantId);

	OrderDetailResponse getOrderById(Long orderId, Long tenantId);

	OrderDetailResponse updateOrder(Long orderId, UpdateOrderRequest request, Long tenantId);

	OrderDetailResponse cancelOrder(Long orderId, Long tenantId, CancelOrderRequest request);

	PickupCheckinResponse checkInPickupOrder(
			Long orderId,
			Double checkinLatitude,
			Double checkinLongitude,
			MultipartFile photo,
			Long tenantId
	);
}
