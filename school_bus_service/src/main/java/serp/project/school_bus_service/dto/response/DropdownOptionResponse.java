package serp.project.school_bus_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DropdownOptionResponse {
    private Long id;
    private String label;
    private String code;
    private String description;
    private Map<String, Object> metadata;
}
