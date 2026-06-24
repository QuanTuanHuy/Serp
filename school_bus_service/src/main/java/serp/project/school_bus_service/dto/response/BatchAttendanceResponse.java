package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchAttendanceResponse {

    private int updatedCount;
    private int skippedCount;
    private List<UpdatedStudent> updatedStudents;

    @Getter
    @Setter
    public static class UpdatedStudent {
        private Long studentId;
        private String status;
    }
}
