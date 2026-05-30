/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.tms_billing_service.domain.Province;
import serp.project.tms_billing_service.domain.Ward;
import serp.project.tms_billing_service.enums.LoaiTuyen;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.exception.AppException;
import serp.project.tms_billing_service.exception.ErrorCode;
import serp.project.tms_billing_service.repository.ProvinceRepository;
import serp.project.tms_billing_service.repository.WardRepository;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RouteClassificationService {
    private static final Set<String> SPECIAL_AXIS_PROVINCES = Set.of("01", "48", "79");

    private final WardRepository wardRepository;
    private final ProvinceRepository provinceRepository;

    public RouteType classify(String senderWardCode, String receiverWardCode) {
        Ward senderWard = wardRepository.findByWardCode(senderWardCode)
                .orElseThrow(() -> new AppException(
                        ErrorCode.LOCATION_NOT_FOUND,
                        "Không tìm thấy phường/xã gửi: " + senderWardCode
                ));
        Ward receiverWard = wardRepository.findByWardCode(receiverWardCode)
                .orElseThrow(() -> new AppException(
                        ErrorCode.LOCATION_NOT_FOUND,
                        "Không tìm thấy phường/xã nhận: " + receiverWardCode
                ));

        if (senderWard.getProvinceCode().equals(receiverWard.getProvinceCode())) {
            if (senderWard.getPhanLoai() == receiverWard.getPhanLoai()) {
                return RouteType.NOI_TINH_NOI_CUM;
            }
            return RouteType.NOI_TINH_LIEN_CUM;
        }

        if (isSpecialAxisRoute(senderWard.getProvinceCode(), receiverWard.getProvinceCode())) {
            return RouteType.LIEN_MIEN_DAC_BIET;
        }

        Province senderProvince = provinceRepository.findByProvinceCode(senderWard.getProvinceCode())
                .orElseThrow(() -> new AppException(
                        ErrorCode.LOCATION_NOT_FOUND,
                        "Không tìm thấy tỉnh/thành gửi: " + senderWard.getProvinceCode()
                ));
        Province receiverProvince = provinceRepository.findByProvinceCode(receiverWard.getProvinceCode())
                .orElseThrow(() -> new AppException(
                        ErrorCode.LOCATION_NOT_FOUND,
                        "Không tìm thấy tỉnh/thành nhận: " + receiverWard.getProvinceCode()
                ));

        if (senderProvince.getMien().equals(receiverProvince.getMien())) {
            return RouteType.NOI_MIEN;
        }

        return RouteType.LIEN_MIEN;
    }

    public boolean isRemoteArea(String receiverWardCode) {
        Ward receiverWard = wardRepository.findByWardCode(receiverWardCode)
                .orElseThrow(() -> new AppException(
                        ErrorCode.LOCATION_NOT_FOUND,
                        "Không tìm thấy phường/xã nhận: " + receiverWardCode
                ));
        return receiverWard.getLoaiTuyen() == LoaiTuyen.VUNG_XA;
    }

    private boolean isSpecialAxisRoute(String senderProvinceCode, String receiverProvinceCode) {
        return !senderProvinceCode.equals(receiverProvinceCode)
                && SPECIAL_AXIS_PROVINCES.contains(senderProvinceCode)
                && SPECIAL_AXIS_PROVINCES.contains(receiverProvinceCode);
    }
}
