package serp.project.purchase_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.purchase_service.constant.AddressType;
import serp.project.purchase_service.constant.SupplierStatus;
import serp.project.purchase_service.validator.EnumValidator;

@Data
public class SupplierCreationForm {

    @NotBlank(message = "name is mandatory")
    private String name;

    private String email;
    private String phone;

    @NotNull(message = "statusId is mandatory")
    @EnumValidator(enumClass = SupplierStatus.class)
    private String statusId;

    @NotNull(message = "addressType is mandatory")
    @EnumValidator(enumClass = AddressType.class)
    private String addressType;

    private float latitude;
    private float longitude;

    @NotBlank(message = "fullAddress is mandatory")
    private String fullAddress;

}
