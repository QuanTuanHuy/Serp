package serp.project.purchase_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCancellationForm {

    @NotBlank(message = "note must not be blank")
    private String note;
}
