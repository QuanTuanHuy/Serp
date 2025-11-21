package serp.project.purchase_service.dto.request;

import lombok.Data;

@Data
public class AddressCreationForm {

    private String entityId;
    private String entityType;
    private String addressType;
    private float latitude;
    private float longitude;
    private boolean isDefault;
    private String fullAddress;

}
