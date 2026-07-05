package serp.project.school_bus_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReorderStopsRequest extends BaseCommandRequest {

    @NotEmpty(message = "Vui lòng chọn thứ tự điểm dừng")
    private List<Long> orderedStopIds;
}

