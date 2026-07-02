/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service;

import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.enums.DeliveryService;

public interface IDeliveryPricingStrategy {
    /**
     * Cho biết mã dịch vụ mà strategy này chịu trách nhiệm tính phí.
     *
     * @return mã dịch vụ giao hàng được hỗ trợ
     */
    DeliveryService getSupportedService();

    /**
     * Tính phí vận chuyển cho một yêu cầu đã được validate ở tầng controller.
     *
     * @param request dữ liệu tuyến, khối lượng và kích thước kiện hàng
     * @return kết quả tính phí kèm chi tiết từng dòng phí
     */
    CalculateShippingFeeResponse calculate(CalculateShippingFeeRequest request);
}
