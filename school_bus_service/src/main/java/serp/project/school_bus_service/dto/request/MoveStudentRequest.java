package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveStudentRequest {

    @NotNull(message = "Vui lòng chọn học sinh")
    private Long studentId;

    @NotNull(message = "Vui lòng chọn đăng ký")
    private Long subscriptionId;

    @NotNull(message = "Vui lòng chọn tuyến đích")
    private Long targetRouteId;
}
