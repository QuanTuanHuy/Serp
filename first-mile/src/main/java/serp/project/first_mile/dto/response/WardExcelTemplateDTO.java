package serp.project.first_mile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardExcelTemplateDTO {
    private String wardCode;
    private String wardName;
}
