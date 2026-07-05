/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import serp.project.tms_billing_service.core.service.IAdminPricingService;
import serp.project.tms_billing_service.domain.ChargeableWeightConfig;
import serp.project.tms_billing_service.domain.DeliveryServiceConfig;
import serp.project.tms_billing_service.domain.SurchargeRule;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.dto.request.admin.UpsertChargeableWeightConfigRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertDeliveryServiceConfigRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertSurchargeRuleRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.response.admin.ChargeableWeightConfigAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.DeliveryServiceConfigAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.SurchargeRuleAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;
import serp.project.tms_billing_service.enums.SurchargeRuleEnum;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;
import serp.project.tms_billing_service.mapper.AdminPricingMapper;
import serp.project.tms_billing_service.repository.ChargeableWeightConfigRepository;
import serp.project.tms_billing_service.repository.DeliveryServiceConfigRepository;
import serp.project.tms_billing_service.repository.SurchargeRuleRepository;
import serp.project.tms_billing_service.repository.TariffRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminPricingServiceImpl implements IAdminPricingService {
    private final TariffRepository tariffRepository;
    private final SurchargeRuleRepository surchargeRuleRepository;
    private final ChargeableWeightConfigRepository chargeableWeightConfigRepository;
    private final DeliveryServiceConfigRepository deliveryServiceConfigRepository;

    /**
     * Lưu biểu phí theo khóa service, loại tuyến và ngày hiệu lực.
     *
     * @param request dữ liệu biểu phí cần tạo hoặc cập nhật
     * @return biểu phí sau khi lưu
     */
    @Override
    @Transactional
    public TariffAdminResponse upsertTariff(UpsertTariffRequest request) {
        validateDateRange(request.getEffectiveDate(), request.getExpirationDate());
        String serviceCode = normalizeServiceCode(request.getServiceCode());

        Tariff tariff = tariffRepository
                .findByServiceCodeAndRouteTypeCodeAndEffectiveDate(
                        serviceCode,
                        request.getRouteTypeCode(),
                        request.getEffectiveDate()
                )
                .orElseGet(Tariff::new);

        tariff.setServiceCode(serviceCode);
        tariff.setRouteTypeCode(request.getRouteTypeCode());
        tariff.setBaseWeight(request.getBaseWeight());
        tariff.setBasePrice(request.getBasePrice());
        tariff.setStepWeight(request.getStepWeight());
        tariff.setStepPrice(request.getStepPrice());
        tariff.setEffectiveDate(request.getEffectiveDate());
        tariff.setExpirationDate(request.getExpirationDate());

        Tariff saved = tariffRepository.save(tariff);
        return AdminPricingMapper.toTariffResponse(saved);
    }

    /**
     * Lưu quy tắc phụ phí nếu mã phụ phí còn nằm trong danh sách hỗ trợ.
     *
     * @param request dữ liệu quy tắc phụ phí cần tạo hoặc cập nhật
     * @return quy tắc phụ phí sau khi lưu
     */
    @Override
    @Transactional
    public SurchargeRuleAdminResponse upsertSurchargeRule(UpsertSurchargeRuleRequest request) {
        validateActiveSurchargeCode(request.getCode());
        validateDateRange(request.getEffectiveDate(), request.getExpirationDate());

        SurchargeRule rule = surchargeRuleRepository.findByCode(request.getCode()).orElseGet(SurchargeRule::new);
        rule.setCode(request.getCode());
        rule.setName(request.getName());
        rule.setCalculationType(request.getCalculationType());
        rule.setRatePercent(request.getRatePercent());
        rule.setFixedAmount(request.getFixedAmount());
        rule.setMinAmount(request.getMinAmount());
        rule.setBaseWeight(request.getBaseWeight());
        rule.setBasePrice(request.getBasePrice());
        rule.setStepWeight(request.getStepWeight());
        rule.setStepPrice(request.getStepPrice());
        rule.setEffectiveDate(request.getEffectiveDate());
        rule.setExpirationDate(request.getExpirationDate());

        SurchargeRule saved = surchargeRuleRepository.save(rule);
        return AdminPricingMapper.toSurchargeResponse(saved);
    }

    /**
     * Lưu tham số quy đổi khối lượng tính cước theo từng dịch vụ vận chuyển.
     */
    @Override
    @Transactional
    public ChargeableWeightConfigAdminResponse upsertChargeableWeightConfig(
            UpsertChargeableWeightConfigRequest request
    ) {
        validateChargeableWeightConfig(request);
        String serviceCode = normalizeServiceCode(request.getServiceCode());

        ChargeableWeightConfig config = chargeableWeightConfigRepository
                .findByServiceCode(serviceCode)
                .orElseGet(ChargeableWeightConfig::new);
        config.setServiceCode(serviceCode);
        config.setMinDimensionCm(request.getMinDimensionCm());
        config.setSmallBulkyThresholdCm(request.getSmallBulkyThresholdCm());
        config.setBaseWeightGram(request.getBaseWeightGram());
        config.setStepWeightGram(request.getStepWeightGram());
        config.setMaxWeightGram(request.getMaxWeightGram());
        config.setVolumetricDivisor(request.getVolumetricDivisor());

        ChargeableWeightConfig saved = chargeableWeightConfigRepository.save(config);
        return AdminPricingMapper.toChargeableWeightConfigResponse(saved);
    }

    /**
     * Lưu hình thức vận chuyển để admin có thể bật/tắt và đặt tên hiển thị trên UI.
     */
    @Override
    @Transactional
    public DeliveryServiceConfigAdminResponse upsertDeliveryServiceConfig(
            UpsertDeliveryServiceConfigRequest request
    ) {
        String serviceCode = normalizeServiceCode(request.getServiceCode());
        DeliveryServiceConfig config = deliveryServiceConfigRepository
                .findByServiceCode(serviceCode)
                .orElseGet(DeliveryServiceConfig::new);
        config.setServiceCode(serviceCode);
        config.setName(request.getName().trim());
        config.setDescription(normalizeNullableText(request.getDescription()));
        config.setActive(request.getActive());
        config.setSortOrder(request.getSortOrder());

        DeliveryServiceConfig saved = deliveryServiceConfigRepository.save(config);
        return AdminPricingMapper.toDeliveryServiceConfigResponse(saved);
    }

    /**
     * Liệt kê biểu phí phục vụ màn hình quản trị, có thể lọc theo service.
     */
    @Override
    public List<TariffAdminResponse> listTariffs(String serviceCode) {
        List<Tariff> tariffs = serviceCode == null
                ? tariffRepository.findAllByOrderByServiceCodeAscRouteTypeCodeAscEffectiveDateDesc()
                : tariffRepository.findAllByServiceCodeOrderByRouteTypeCodeAscEffectiveDateDesc(
                        normalizeServiceCode(serviceCode)
                );
        return tariffs.stream()
                .map(AdminPricingMapper::toTariffResponse)
                .toList();
    }

    /**
     * Trả về các quy tắc phụ phí đã cấu hình để admin quản lý và calculator có thể áp dụng theo request.
     */
    @Override
    public List<SurchargeRuleAdminResponse> listSurchargeRules() {
        return surchargeRuleRepository.findAll().stream()
                .filter(rule -> rule.getCode() != null)
                .map(AdminPricingMapper::toSurchargeResponse)
                .toList();
    }

    /**
     * Liệt kê tham số quy đổi khối lượng tính cước theo dịch vụ.
     */
    @Override
    public List<ChargeableWeightConfigAdminResponse> listChargeableWeightConfigs() {
        return chargeableWeightConfigRepository.findAllByOrderByServiceCodeAsc().stream()
                .map(AdminPricingMapper::toChargeableWeightConfigResponse)
                .toList();
    }

    /**
     * Liệt kê danh mục dịch vụ vận chuyển cho màn hình quản trị và dropdown tính phí.
     */
    @Override
    public List<DeliveryServiceConfigAdminResponse> listDeliveryServiceConfigs(boolean activeOnly) {
        List<DeliveryServiceConfig> configs = activeOnly
                ? deliveryServiceConfigRepository.findAllByActiveTrueOrderBySortOrderAscServiceCodeAsc()
                : deliveryServiceConfigRepository.findAllByOrderBySortOrderAscServiceCodeAsc();
        return configs.stream()
                .map(AdminPricingMapper::toDeliveryServiceConfigResponse)
                .toList();
    }

    /**
     * Đảm bảo mã phụ phí hợp lệ trước khi lưu cấu hình.
     */
    private void validateActiveSurchargeCode(SurchargeRuleEnum code) {
        if (code == null) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "Surcharge rule code is required"
            );
        }
    }

    /**
     * Đảm bảo khoảng ngày hiệu lực hợp lệ trước khi lưu cấu hình giá.
     */
    private void validateDateRange(java.time.LocalDate effectiveDate, java.time.LocalDate expirationDate) {
        if (Objects.nonNull(effectiveDate) && Objects.nonNull(expirationDate) && expirationDate.isBefore(effectiveDate)) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "expirationDate phải lớn hơn hoặc bằng effectiveDate"
            );
        }
    }

    /**
     * Kiểm tra các ngưỡng khối lượng có thứ tự hợp lệ trước khi lưu.
     */
    private void validateChargeableWeightConfig(UpsertChargeableWeightConfigRequest request) {
        if (request.getBaseWeightGram() >= request.getMaxWeightGram()) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST,
                    "baseWeightGram phải nhỏ hơn maxWeightGram"
            );
        }
    }

    private String normalizeServiceCode(String serviceCode) {
        return serviceCode.trim().toUpperCase();
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
