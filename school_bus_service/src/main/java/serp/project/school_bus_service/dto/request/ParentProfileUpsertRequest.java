package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParentProfileUpsertRequest extends BaseCommandRequest {

    @NotNull
    private Long accountUserId;

    @NotBlank
    private String fullName;

    private String phone;

    private String email;

    private String address;
}
