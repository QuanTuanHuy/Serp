/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service;

import serp.project.tms_billing_service.dto.request.admin.UpsertSurchargeRuleRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.response.admin.SurchargeRuleAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;
import serp.project.tms_billing_service.enums.DeliveryService;

import java.util.List;

public interface IAdminPricingService {
    /**
     * Tạo mới hoặc cập nhật biểu phí theo service, loại tuyến và ngày hiệu lực.
     */
    TariffAdminResponse upsertTariff(UpsertTariffRequest request);

    /**
     * Tạo mới hoặc cập nhật quy tắc phụ phí đang được hỗ trợ.
     */
    SurchargeRuleAdminResponse upsertSurchargeRule(UpsertSurchargeRuleRequest request);

    /**
     * Liệt kê biểu phí, có thể lọc theo mã dịch vụ.
     */
    List<TariffAdminResponse> listTariffs(DeliveryService serviceCode);

    /**
     * Liệt kê các quy tắc phụ phí đang được dùng trong tính phí.
     */
    List<SurchargeRuleAdminResponse> listSurchargeRules();
}
