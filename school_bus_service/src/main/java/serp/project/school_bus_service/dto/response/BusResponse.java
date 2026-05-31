package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusResponse extends BaseResponse {

    private String plateNumber;
    private String busType;
    private Integer capacity;
    private String status;
    private Long homeDepotId;
    private String homeDepotName;
}
