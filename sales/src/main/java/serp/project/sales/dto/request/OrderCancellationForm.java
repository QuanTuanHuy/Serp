package serp.project.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderCancellationForm {

    @NotBlank(message = "note must not be blank")
    private String note;
}
