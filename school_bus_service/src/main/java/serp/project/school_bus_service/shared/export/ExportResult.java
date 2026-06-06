package serp.project.school_bus_service.shared.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResult {
    private String fileName;
    private String contentType;
    private byte[] content;
}
