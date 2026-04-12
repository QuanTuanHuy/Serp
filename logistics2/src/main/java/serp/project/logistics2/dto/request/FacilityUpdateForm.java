package serp.project.logistics2.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;
import serp.project.logistics2.constant.FacilityStatus;
import serp.project.logistics2.validator.EnumValidator;

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
