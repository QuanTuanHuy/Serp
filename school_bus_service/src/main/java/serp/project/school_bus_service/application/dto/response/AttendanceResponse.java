package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AttendanceResponse extends BaseResponse {

    private Long routeId;
    private String routeCode;
    private Long studentId;
    private String studentName;
    private String attendanceType;
    private String status;
    private LocalDateTime recordedAt;
    private Long recordedBy;
    private String notes;
}
