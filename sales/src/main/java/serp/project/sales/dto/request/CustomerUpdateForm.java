package serp.project.sales.dto.request;

import lombok.Data;
import serp.project.sales.constant.CustomerStatus;
import serp.project.sales.validator.EnumValidator;

@Data
public class CustomerUpdateForm {

    private String name;
    private String email;
    private String phone;

    @EnumValidator(enumClass = CustomerStatus.class)
    private String statusId;

}
