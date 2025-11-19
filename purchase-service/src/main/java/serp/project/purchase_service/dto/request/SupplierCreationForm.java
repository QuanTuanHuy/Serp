package serp.project.purchase_service.dto.request;

import lombok.Data;

@Data
public class SupplierCreationForm {

    private String name;
    private String email;
    private String phone;
    private String statusId;

    private String addressType;
    private float latitude;
    private float longitude;
    private boolean isDefault;
    private String fullAddress;

}
