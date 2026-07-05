package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchAttendanceRequest extends BaseCommandRequest {

    @NotNull(message = "Vui lòng chọn thao tác điểm danh")
    private String action;

    @NotEmpty(message = "Vui lòng chọn học sinh")
    private List<Long> studentIds;

    private String note;
}
