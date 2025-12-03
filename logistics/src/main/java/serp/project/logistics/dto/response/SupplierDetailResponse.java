package serp.project.logistics.dto.response;

import lombok.Builder;
import lombok.Data;
import serp.project.logistics.entity.AddressEntity;
import serp.project.logistics.entity.SupplierEntity;

@Data
@Builder
public class SupplierDetailResponse {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String statusId;
    private AddressEntity address;

    public static SupplierDetailResponse fromEntity(
            SupplierEntity supplier,
            AddressEntity address
    ) {
        return SupplierDetailResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .statusId(supplier.getStatusId())
                .address(address)
                .build();
    }

}
