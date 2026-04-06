package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusResponse extends BaseResponse {

    private String plateNumber;
    private String busType;
    private Integer capacity;
    private String status;
}
