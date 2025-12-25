package serp.project.purchase_service.dto.request;

import lombok.Data;
import serp.project.purchase_service.constant.SupplierStatus;
import serp.project.purchase_service.validator.EnumValidator;

@Data
public class SupplierUpdateForm {

    private String name;
    private String email;
    private String phone;

    @EnumValidator(enumClass = SupplierStatus.class)
    private String statusId;

}
