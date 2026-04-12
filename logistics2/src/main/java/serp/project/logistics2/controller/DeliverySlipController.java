package serp.project.logistics2.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.logistics2.dto.request.DeliveryItemUpdateForm;
import serp.project.logistics2.dto.request.DeliverySlipCreationForm;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.DeliverySlipEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.service.DeliverySlipService;
import serp.project.logistics2.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics2/api/v1/delivery-slips")
@Validated
@Slf4j
public class DeliverySlipController {

    private final DeliverySlipService deliverySlipService;
    private final AuthUtils authUtils;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<?>> createDeliverySlip(
            @Valid @RequestBody DeliverySlipCreationForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[DeliverySlipController] Create delivery slip for shipment {} by user id {} and tenant id {}",
                form.getOutboundShipmentId(), userId, tenantId);
        deliverySlipService.createSlip(form, tenantId, userId);
        return ResponseEntity.ok(GeneralResponse.success("Tạo phiếu giao hàng thành công"));
    }

    @PutMapping("/return/{slipId}")
    public ResponseEntity<GeneralResponse<?>> returnDeliverySlip(
            @PathVariable String slipId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[DeliverySlipController] Return delivery slip ID {}", slipId);
        deliverySlipService.returnSlip(slipId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Trả hàng thành công"));
    }

    @DeleteMapping("/delete/{slipId}")
    public ResponseEntity<GeneralResponse<?>> deleteDeliverySlip(
            @PathVariable String slipId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[DeliverySlipController] Delete delivery slip ID {}", slipId);
        deliverySlipService.deleteSlip(slipId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Xóa phiếu giao hàng thành công"));
    }

    @GetMapping("/search/{slipId}")
    public ResponseEntity<GeneralResponse<DeliverySlipEntity>> getDeliverySlipDetail(
            @PathVariable String slipId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[DeliverySlipController] Get detail for delivery slip ID {}", slipId);
        DeliverySlipEntity slip = deliverySlipService.getSlip(slipId, tenantId);
        return ResponseEntity
                .ok(GeneralResponse.success("Truy xuất chi tiết phiếu giao hàng thành công", slip));
    }

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<PageResponse<DeliverySlipEntity>>> searchDeliverySlips(
            @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String outboundShipmentId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String facilityId,
            @RequestParam(required = false) String query) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info(
                "[DeliverySlipController] Search delivery slips with status {}, outboundShipmentId {}, customerId {}, facilityId {}, query {}, page {}, size {}, sortBy {}, sortDirection {}",
                status, outboundShipmentId, customerId, facilityId, query, page, size, sortBy,
                sortDirection);
        Page<DeliverySlipEntity> slips = deliverySlipService.findSlips(
                status,
                outboundShipmentId,
                customerId,
                facilityId,
                query,
                tenantId,
                page,
                size,
                sortBy,
                sortDirection);
        return ResponseEntity
                .ok(GeneralResponse.success("Tìm kiếm phiếu giao hàng thành công", PageResponse.of(slips)));
    }

    @PutMapping("/update/{slipId}/add")
    public ResponseEntity<GeneralResponse<?>> addDeliveryItems(
            @PathVariable String slipId,
            @Valid @RequestBody DeliverySlipCreationForm.ItemForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[DeliverySlipController] Add items to delivery slip ID {}", slipId);
        deliverySlipService.addDeliveryItem(form, slipId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Thêm hàng vào phiếu giao hàng thành công"));
    }

    @PutMapping("/update/{slipId}/update/{itemId}")
    public ResponseEntity<GeneralResponse<?>> updateDeliveryItem(
            @PathVariable String slipId,
            @PathVariable String itemId,
            @Valid @RequestBody DeliveryItemUpdateForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[DeliverySlipController] Update item {} in delivery slip ID {}", itemId, slipId);
        deliverySlipService.updateDeliveryItem(itemId, form, slipId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Cập nhật hàng trong phiếu giao hàng thành công"));
    }

    @DeleteMapping("/update/{slipId}/remove/{itemId}")
    public ResponseEntity<GeneralResponse<?>> removeDeliveryItem(
            @PathVariable String slipId,
            @PathVariable String itemId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("[DeliverySlipController] Remove item {} from delivery slip ID {}", itemId, slipId);
        deliverySlipService.deleteItem(itemId, slipId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Xóa hàng khỏi phiếu giao hàng thành công"));
    }

}
