package serp.project.logistics2.controller;

import java.time.LocalDate;

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
import serp.project.logistics2.dto.request.DeliveryPlanCreationForm;
import serp.project.logistics2.dto.request.DeliveryPlanUpdateForm;
import serp.project.logistics2.dto.response.GeneralResponse;
import serp.project.logistics2.dto.response.PageResponse;
import serp.project.logistics2.entity.DeliveryPlanEntity;
import serp.project.logistics2.exception.AppErrorCode;
import serp.project.logistics2.exception.AppException;
import serp.project.logistics2.service.DeliveryPlanService;
import serp.project.logistics2.util.AuthUtils;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logistics2/api/v1/delivery-plans")
@Validated
@Slf4j
public class DeliveryPlanController {

    private final DeliveryPlanService deliveryPlanService;
    private final AuthUtils authUtils;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<?>> createDeliveryPlan(
            @Valid @RequestBody DeliveryPlanCreationForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("Received request to create delivery plan on: {}", form.getDeliveryDate());
        deliveryPlanService.createDeliveryPlan(form, userId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Tạo kế hoạch giao hàng thành công"));
    }

    @PutMapping("/update/{planId}")
    public ResponseEntity<GeneralResponse<?>> updateDeliveryPlan(
            @PathVariable String planId,
            @Valid @RequestBody DeliveryPlanUpdateForm form) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("Received request to update delivery plan with id: {}", planId);
        deliveryPlanService.updateDeliveryPlan(planId, form, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Cập nhật kế hoạch giao hàng thành công"));
    }

    @PutMapping("/optimize/{planId}")
    public ResponseEntity<GeneralResponse<?>> optimizeDeliveryPlan(
            @PathVariable String planId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("Received request to optimize delivery plan with id: {}", planId);
        deliveryPlanService.optimizeDeliveryPlan(planId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Bắt đầu tối ưu kế hoạch giao hàng"));
    }

    @DeleteMapping("/delete/{planId}")
    public ResponseEntity<GeneralResponse<?>> deleteDeliveryPlan(
            @PathVariable String planId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("Received request to delete delivery plan with id: {}", planId);
        deliveryPlanService.deleteDeliveryPlan(planId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Xóa kế hoạch giao hàng thành công"));
    }

    @GetMapping("/search")
    public ResponseEntity<GeneralResponse<PageResponse<DeliveryPlanEntity>>> searchDeliveryPlans(
            @Min(0) @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String facilityId,
            @RequestParam(required = false) LocalDate deliveryDate,
            @RequestParam(required = false) String optimizationStatus) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info(
                "Received request to search delivery plans with query: {}, facilityId: {}, deliveryDate: {}, optimizationStatus: {}",
                query, facilityId, deliveryDate, optimizationStatus);
        Page<DeliveryPlanEntity> result = deliveryPlanService.searchDeliveryPlans(query, facilityId, deliveryDate,
                optimizationStatus, tenantId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(GeneralResponse.success("Tìm kiếm kế hoạch giao hàng thành công", PageResponse.of(result)));
    }

    @GetMapping("/search/{planId}")
    public ResponseEntity<GeneralResponse<DeliveryPlanEntity>> getDeliveryPlanDetails(
            @PathVariable String planId) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new AppException(AppErrorCode.UNAUTHORIZED));
        log.info("Received request to get details of delivery plan with id: {}", planId);
        DeliveryPlanEntity result = deliveryPlanService.getDeliveryPlanDetails(planId, tenantId);
        return ResponseEntity.ok(GeneralResponse.success("Lấy thông tin kế hoạch giao hàng thành công", result));
    }

}
