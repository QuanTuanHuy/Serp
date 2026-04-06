package serp.project.school_bus_service.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseCommandRequest {

    private Boolean isActive;

    public Boolean resolveIsActive(Boolean defaultValue) {
        return isActive == null ? defaultValue : isActive;
    }
}
