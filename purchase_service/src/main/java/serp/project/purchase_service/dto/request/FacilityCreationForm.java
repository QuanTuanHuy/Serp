package serp.project.purchase_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.purchase_service.constant.AddressType;
import serp.project.purchase_service.constant.FacilityStatus;
import serp.project.purchase_service.validator.EnumValidator;

@Data
public class FacilityCreationForm {

    @NotBlank(message = "Facility name cannot be empty")
    private String name;

    private String phone;

    @NotNull(message = "statusId cannot be empty")
    @EnumValidator(enumClass = FacilityStatus.class)
    private String statusId;

    @NotBlank(message = "postalCode cannot be empty")
    private String postalCode;

    @Min(value = 0, message = "length must be non-negative")
    private float length;

    @Min(value = 0, message = "width must be non-negative")
    private float width;

    @Min(value = 0, message = "height must be non-negative")
    private float height;

    @NotNull(message = "addressType cannot be empty")
    @EnumValidator(enumClass = AddressType.class)
    private String addressType;

    private float latitude;
    private float longitude;

    @NotBlank(message = "fullAddress cannot be empty")
    private String fullAddress;

}
