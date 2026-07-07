package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ManualDispatchRequest extends BaseCommandRequest {

    @NotNull(message = "Vui lòng chọn xe")
    private Long busId;

    @NotNull(message = "Vui lòng chọn tài xế")
    private Long driverId;

    private Long attendantId;

    private List<Long> orderedStopIds;

    private String notes;
}

