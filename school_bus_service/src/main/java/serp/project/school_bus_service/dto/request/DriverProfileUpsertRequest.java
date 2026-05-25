package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DriverProfileUpsertRequest extends BaseCommandRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String fullName;

    private String phone;

    @NotBlank
    private String licenseNumber;

    @NotBlank
    private String licenseClass;

    @NotNull
    private LocalDate licenseExpiryDate;

    @NotBlank
    private String status;
}
