/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.ui.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.tms_billing_service.core.service.IAdminPricingService;
import serp.project.tms_billing_service.dto.ApiResponse;
import serp.project.tms_billing_service.dto.request.admin.UpsertSurchargeRuleRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.response.admin.SurchargeRuleAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;
import serp.project.tms_billing_service.enums.DeliveryService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/pricing")
public class AdminPricingController {
    private final IAdminPricingService adminPricingService;

    /**
     * Lấy danh sách biểu phí hiện hành để quản trị bảng giá.
     *
     * @param serviceCode mã dịch vụ cần lọc, có thể bỏ trống để lấy tất cả
     * @return danh sách biểu phí theo dịch vụ và loại tuyến
     */
    @GetMapping("/tariffs")
    public ApiResponse<List<TariffAdminResponse>> listTariffs(
            @RequestParam(value = "serviceCode", required = false) DeliveryService serviceCode
    ) {
        return ApiResponse.<List<TariffAdminResponse>>builder()
                .message("OK")
                .result(adminPricingService.listTariffs(serviceCode))
                .build();
    }

    /**
     * Tạo mới hoặc cập nhật biểu phí theo khóa service, route type và ngày hiệu lực.
     *
     * @param request dữ liệu biểu phí cần lưu
     * @return biểu phí sau khi lưu
     */
    @PutMapping("/tariffs")
    public ApiResponse<TariffAdminResponse> upsertTariff(@RequestBody @Valid UpsertTariffRequest request) {
        return ApiResponse.<TariffAdminResponse>builder()
                .message("OK")
                .result(adminPricingService.upsertTariff(request))
                .build();
    }

    /**
     * Tạo mới hoặc cập nhật quy tắc phụ phí đang được hỗ trợ.
     *
     * @param request dữ liệu quy tắc phụ phí
     * @return quy tắc phụ phí sau khi lưu
     */
    @PutMapping("/surcharge-rules")
    public ApiResponse<SurchargeRuleAdminResponse> upsertSurchargeRule(
            @RequestBody @Valid UpsertSurchargeRuleRequest request
    ) {
        return ApiResponse.<SurchargeRuleAdminResponse>builder()
                .message("OK")
                .result(adminPricingService.upsertSurchargeRule(request))
                .build();
    }

    /**
     * Lấy các quy tắc phụ phí còn được dùng trong luồng tính phí.
     *
     * @return danh sách quy tắc phụ phí đang hoạt động
     */
    @GetMapping("/surcharge-rules")
    public ApiResponse<List<SurchargeRuleAdminResponse>> listSurchargeRules() {
        return ApiResponse.<List<SurchargeRuleAdminResponse>>builder()
                .message("OK")
                .result(adminPricingService.listSurchargeRules())
                .build();
    }
}
