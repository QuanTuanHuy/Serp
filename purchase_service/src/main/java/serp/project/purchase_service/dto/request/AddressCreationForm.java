package serp.project.purchase_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.purchase_service.constant.AddressType;
import serp.project.purchase_service.constant.EntityType;
import serp.project.purchase_service.validator.EnumValidator;

@Data
public class AddressCreationForm {

    @NotBlank(message = "Entity ID cannot be empty")
    private String entityId;

    @NotNull(message = "Entity Type cannot be empty")
    @EnumValidator(enumClass = EntityType.class)
    private String entityType;

    @NotNull(message = "Address Type cannot be empty")
    @EnumValidator(enumClass = AddressType.class)
    private String addressType;

    private float latitude;
    private float longitude;

    private boolean isDefault;

    @NotBlank(message = "Full Address cannot be empty")
    private String fullAddress;

}
