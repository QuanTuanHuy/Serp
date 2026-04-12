package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.response.ImportHistoryResponse;
import serp.project.first_mile.dto.response.OrderConfirmationResponse;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;

public interface OrderService {
	byte[] exportTemplate(Long tenantId);

	ValidateImportFileDTO<OrderImportDTO> validateImportFile(MultipartFile file, Long tenantId);

	ImportHistoryResponse importOrdersAsync(MultipartFile file, Long tenantId);

	OrderConfirmationResponse confirmOrder(Long orderId, Long tenantId);
}
