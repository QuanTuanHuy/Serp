package serp.project.sales.dto.request;

import lombok.Data;
import serp.project.sales.constant.AddressType;
import serp.project.sales.validator.EnumValidator;

@Data
public class AddressUpdateForm {

    @EnumValidator(enumClass = AddressType.class)
    private String addressType;
    private float latitude;
    private float longitude;
    private boolean isDefault;
    private String fullAddress;

}
