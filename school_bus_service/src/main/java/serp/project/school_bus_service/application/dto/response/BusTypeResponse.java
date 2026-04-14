package serp.project.school_bus_service.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BusTypeResponse {

    private String code;
    private Integer value;
    private String description;
}
