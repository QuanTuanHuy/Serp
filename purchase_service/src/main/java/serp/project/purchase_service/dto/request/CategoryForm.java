package serp.project.purchase_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryForm {

    @NotBlank(message = "Category name cannot be empty")
    private String name;

}
