/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service;

import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;

public interface IShippingFeeService {
    /**
     * Tính phí vận chuyển bằng strategy tương ứng với mã dịch vụ trong request.
     *
     * @param request thông tin dịch vụ, tuyến gửi/nhận, khối lượng và kích thước
     * @return kết quả tính phí vận chuyển
     */
    CalculateShippingFeeResponse calculateShippingFee(CalculateShippingFeeRequest request);
}
