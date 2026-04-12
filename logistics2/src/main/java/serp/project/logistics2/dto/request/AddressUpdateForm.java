package serp.project.logistics2.dto.request;

import lombok.Data;
import serp.project.logistics2.constant.AddressType;
import serp.project.logistics2.validator.EnumValidator;

@Data
public class AddressUpdateForm {

    @EnumValidator(enumClass = AddressType.class)
    private String addressType;
    private float latitude;
    private float longitude;
    private boolean isDefault;
    private String fullAddress;

}
