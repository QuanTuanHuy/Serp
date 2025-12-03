package serp.project.logistics.dto.response;

import lombok.Builder;
import lombok.Data;
import serp.project.logistics.entity.AddressEntity;
import serp.project.logistics.entity.FacilityEntity;

import java.time.LocalDateTime;

@Data
@Builder
public class FacilityDetailResponse {
    private String id;
    private String name;
    private String statusId;
    private LocalDateTime createdStamp;
    private LocalDateTime lastUpdatedStamp;
    private boolean isDefault;
    private String phone;
    private String postalCode;
    private float length;
    private float width;
    private float height;
    private AddressEntity address;

    public static FacilityDetailResponse fromEntity(
            FacilityEntity facility,
            AddressEntity address
    ) {
        return FacilityDetailResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .statusId(facility.getStatusId())
                .createdStamp(facility.getCreatedStamp())
                .lastUpdatedStamp(facility.getLastUpdatedStamp())
                .isDefault(facility.isDefault())
                .phone(facility.getPhone())
                .postalCode(facility.getPostalCode())
                .length(facility.getLength())
                .width(facility.getWidth())
                .height(facility.getHeight())
                .address(address)
                .build();
    }
}
