package serp.project.logistics.dto.request;

import lombok.Data;
import serp.project.logistics.constant.AddressType;
import serp.project.logistics.validator.EnumValidator;

@Data
public class AddressUpdateForm {

    @EnumValidator(enumClass = AddressType.class)
    private String addressType;
    private float latitude;
    private float longitude;
    private boolean isDefault;
    private String fullAddress;

}
