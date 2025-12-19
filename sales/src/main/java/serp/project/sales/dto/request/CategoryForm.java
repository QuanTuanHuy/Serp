package serp.project.logistics.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryForm {

    @NotBlank(message = "Category name cannot be empty")
    private String name;

}
