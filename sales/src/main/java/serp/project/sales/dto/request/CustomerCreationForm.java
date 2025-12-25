package serp.project.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import serp.project.sales.constant.AddressType;
import serp.project.sales.constant.CustomerStatus;
import serp.project.sales.validator.EnumValidator;

@Data
public class CustomerCreationForm {

    @NotBlank(message = "name is mandatory")
    private String name;

    private String email;
    private String phone;

    @NotNull(message = "statusId is mandatory")
    @EnumValidator(enumClass = CustomerStatus.class)
    private String statusId;

    @NotNull(message = "addressType is mandatory")
    @EnumValidator(enumClass = AddressType.class)
    private String addressType;

    private float latitude;
    private float longitude;

    @NotBlank(message = "fullAddress is mandatory")
    private String fullAddress;

}
