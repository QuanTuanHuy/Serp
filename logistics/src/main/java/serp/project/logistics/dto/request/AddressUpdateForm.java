package serp.project.logistics.dto.request;

import lombok.Data;

@Data
public class AddressUpdateForm {

    private String addressType;
    private float latitude;
    private float longitude;
    private boolean isDefault;
    private String fullAddress;

}
