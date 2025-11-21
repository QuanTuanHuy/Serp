package serp.project.purchase_service.dto.request;

import lombok.Data;

@Data
public class SupplierUpdateForm {

    private String name;
    private String email;
    private String phone;
    private String statusId;

}
