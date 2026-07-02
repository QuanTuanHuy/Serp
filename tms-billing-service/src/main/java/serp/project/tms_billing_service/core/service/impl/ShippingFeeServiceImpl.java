/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.impl;

import org.springframework.stereotype.Service;
import serp.project.tms_billing_service.core.service.IDeliveryPricingStrategy;
import serp.project.tms_billing_service.core.service.IShippingFeeService;
import serp.project.tms_billing_service.dto.request.CalculateShippingFeeRequest;
import serp.project.tms_billing_service.dto.response.CalculateShippingFeeResponse;
import serp.project.tms_billing_service.enums.DeliveryService;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ShippingFeeServiceImpl implements IShippingFeeService {
    private final Map<DeliveryService, IDeliveryPricingStrategy> strategyByServiceCode;

    /**
     * Khởi tạo registry strategy theo từng mã dịch vụ để tránh switch/case khi mở rộng dịch vụ mới.
     *
     * @param pricingStrategies danh sách strategy được Spring inject
     */
    public ShippingFeeServiceImpl(List<IDeliveryPricingStrategy> pricingStrategies) {
        this.strategyByServiceCode = new EnumMap<>(DeliveryService.class);
        for (IDeliveryPricingStrategy strategy : pricingStrategies) {
            strategyByServiceCode.put(strategy.getSupportedService(), strategy);
        }
    }

    /**
     * Chọn strategy theo serviceCode và ủy quyền tính phí cho strategy tương ứng.
     *
     * @param request thông tin kiện hàng và tuyến cần tính phí
     * @return kết quả tính phí từ strategy của dịch vụ
     */
    @Override
    public CalculateShippingFeeResponse calculateShippingFee(CalculateShippingFeeRequest request) {
        IDeliveryPricingStrategy strategy = strategyByServiceCode.get(request.getServiceCode());
        if (strategy == null) {
            throw new AppException(
                    ErrorCode.BILLING_RULE_NOT_FOUND,
                    "Chưa cấu hình strategy tính phí cho service: " + request.getServiceCode()
            );
        }

        return strategy.calculate(request);
    }
}
