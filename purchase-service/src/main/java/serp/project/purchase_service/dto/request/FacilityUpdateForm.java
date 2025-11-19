package serp.project.purchase_service.dto.request;

import lombok.Data;

@Data
public class FacilityUpdateForm {
    private String name;
    private boolean isDefault;
    private String statusId;
    private String phone;
    private String postalCode;
    private float length;
    private float weight;
    private float height;
}
