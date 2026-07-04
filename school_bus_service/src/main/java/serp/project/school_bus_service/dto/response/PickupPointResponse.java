package serp.project.school_bus_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PickupPointResponse extends BaseResponse {

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String code;
    private String usageType;
    private String pickupInstruction;

    private List<LinkedSchoolDto> schools;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkedSchoolDto {
        private Long id;
        private String code;
        private String name;
    }
}
