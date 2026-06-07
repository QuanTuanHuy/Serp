package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MapLocationResponse {

    private String displayName;
    private Double latitude;
    private Double longitude;
    private Map<String, String> addressParts;
}
