package serp.project.school_bus_service.dto.params;

import serp.project.school_bus_service.dto.request.BaseParamsRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusParamsRequest extends BaseParamsRequest {
    private Long depotId;
    private Long homeDepotId;
}
