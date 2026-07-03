/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service;

import serp.project.tms_billing_service.dto.request.admin.UpsertChargeableWeightConfigRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertSurchargeRuleRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.response.admin.ChargeableWeightConfigAdminResponse;
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
     * Tạo mới hoặc cập nhật cấu hình khối lượng tính cước theo dịch vụ.
     */
    ChargeableWeightConfigAdminResponse upsertChargeableWeightConfig(UpsertChargeableWeightConfigRequest request);

    /**
     * Liệt kê biểu phí, có thể lọc theo mã dịch vụ.
     */
    List<TariffAdminResponse> listTariffs(DeliveryService serviceCode);

    /**
     * Liệt kê các quy tắc phụ phí đang được dùng trong tính phí.
     */
    List<SurchargeRuleAdminResponse> listSurchargeRules();

    /**
     * Liệt kê cấu hình khối lượng tính cước theo dịch vụ.
     */
    List<ChargeableWeightConfigAdminResponse> listChargeableWeightConfigs();
}
