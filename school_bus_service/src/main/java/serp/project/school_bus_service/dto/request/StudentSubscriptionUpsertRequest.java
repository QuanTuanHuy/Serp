package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentSubscriptionUpsertRequest extends BaseCommandRequest {

    @NotNull(message = "Vui lòng chọn học sinh")
    private Long studentId;

    @NotNull(message = "Vui lòng chọn trường")
    private Long schoolId;

    private Long pickupPointId;
    private Long dropoffPointId;

    @NotBlank(message = "Vui lòng chọn loại chuyến")
    private String tripOption;

    private Boolean monday = Boolean.TRUE;
    private Boolean tuesday = Boolean.TRUE;
    private Boolean wednesday = Boolean.TRUE;
    private Boolean thursday = Boolean.TRUE;
    private Boolean friday = Boolean.TRUE;
    private Boolean saturday = Boolean.FALSE;
    private Boolean sunday = Boolean.FALSE;

    @NotNull(message = "Vui lòng chọn ngày bắt đầu hiệu lực")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
    private String status;
    private Long sourceRequestId;
}

