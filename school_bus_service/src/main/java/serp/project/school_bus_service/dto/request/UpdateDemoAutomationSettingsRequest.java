package serp.project.school_bus_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDemoAutomationSettingsRequest {

    private Boolean autoAdvanceStops;
    private Boolean autoAttendance;
}
