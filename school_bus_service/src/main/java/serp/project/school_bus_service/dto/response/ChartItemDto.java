package serp.project.school_bus_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartItemDto {
    private String name;
    private long count;
    private String label;

    public ChartItemDto(String name, long count) {
        this.name = name;
        this.count = count;
        this.label = name;
    }
}
