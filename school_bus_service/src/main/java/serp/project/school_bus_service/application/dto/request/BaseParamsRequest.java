package serp.project.school_bus_service.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseParamsRequest {

    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "id";
    private String sortDirection = "DESC";
    private String keyword;
}
