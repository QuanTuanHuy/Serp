package serp.project.tms_billing_service.core.service.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.tms_billing_service.domain.Province;
import serp.project.tms_billing_service.domain.Ward;
import serp.project.tms_billing_service.enums.PhanLoai;
import serp.project.tms_billing_service.enums.RouteType;
import serp.project.tms_billing_service.repository.ProvinceRepository;
import serp.project.tms_billing_service.repository.WardRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteClassificationServiceTest {
    @Mock
    private WardRepository wardRepository;
    @Mock
    private ProvinceRepository provinceRepository;

    @InjectMocks
    private RouteClassificationService routeClassificationService;

    @Test
    void shouldClassifyNoiTinhNoiCum() {
        Ward sender = ward("790001", "79", PhanLoai.CUM_1);
        Ward receiver = ward("790002", "79", PhanLoai.CUM_1);

        when(wardRepository.findByWardCode("790001")).thenReturn(Optional.of(sender));
        when(wardRepository.findByWardCode("790002")).thenReturn(Optional.of(receiver));

        RouteType routeType = routeClassificationService.classify("790001", "790002");

        assertEquals(RouteType.NOI_TINH_NOI_CUM, routeType);
    }

    @Test
    void shouldClassifyLienMienDacBiet() {
        Ward sender = ward("010001", "01", PhanLoai.CUM_1);
        Ward receiver = ward("790002", "79", PhanLoai.CUM_2);

        when(wardRepository.findByWardCode("010001")).thenReturn(Optional.of(sender));
        when(wardRepository.findByWardCode("790002")).thenReturn(Optional.of(receiver));

        RouteType routeType = routeClassificationService.classify("010001", "790002");

        assertEquals(RouteType.LIEN_MIEN_DAC_BIET, routeType);
    }

    @Test
    void shouldClassifyNoiMien() {
        Ward sender = ward("010001", "01", PhanLoai.CUM_1);
        Ward receiver = ward("310001", "31", PhanLoai.CUM_2);
        Province province1 = province("01", 1L);
        Province province2 = province("31", 1L);

        when(wardRepository.findByWardCode("010001")).thenReturn(Optional.of(sender));
        when(wardRepository.findByWardCode("310001")).thenReturn(Optional.of(receiver));
        when(provinceRepository.findByProvinceCode("01")).thenReturn(Optional.of(province1));
        when(provinceRepository.findByProvinceCode("31")).thenReturn(Optional.of(province2));

        RouteType routeType = routeClassificationService.classify("010001", "310001");

        assertEquals(RouteType.NOI_MIEN, routeType);
    }

    private Ward ward(String wardCode, String provinceCode, PhanLoai phanLoai) {
        Ward ward = new Ward();
        ward.setWardCode(wardCode);
        ward.setProvinceCode(provinceCode);
        ward.setPhanLoai(phanLoai);
        return ward;
    }

    private Province province(String provinceCode, Long mien) {
        Province province = new Province();
        province.setProvinceCode(provinceCode);
        province.setMien(mien);
        return province;
    }
}
