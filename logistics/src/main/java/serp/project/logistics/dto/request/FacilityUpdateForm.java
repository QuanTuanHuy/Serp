package serp.project.logistics.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;
import serp.project.logistics.constant.FacilityStatus;
import serp.project.logistics.validator.EnumValidator;

@Data
public class FacilityUpdateForm {
    private String name;
    private boolean isDefault;

    @EnumValidator(enumClass = FacilityStatus.class)
    private String statusId;

    private String phone;
    private String postalCode;

    @Min(value = 0, message = "length must be non-negative")
    private float length;

    @Min(value = 0, message = "width must be non-negative")
    private float width;

    @Min(value = 0, message = "height must be non-negative")
    private float height;
}
