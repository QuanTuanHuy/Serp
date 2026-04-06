package serp.project.school_bus_service.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SchoolUpsertRequest extends BaseCommandRequest {

    @NotBlank
    private String name;

    private String code;

    private String address;

    private String contactPhone;

    private String contactEmail;
}
