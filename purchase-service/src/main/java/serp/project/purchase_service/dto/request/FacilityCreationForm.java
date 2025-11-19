package serp.project.purchase_service.dto.request;

import lombok.Data;

@Data
public class FacilityCreationForm {

    private String name;
    private String phone;
    private String statusId;
    private String postalCode;
    private float length;
    private float weight;
    private float height;

    private String addressType;
    private float latitude;
    private float longitude;
    private boolean isAddressDefault;
    private String fullAddress;

}
