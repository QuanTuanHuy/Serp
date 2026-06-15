/*
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
package serp.project.school_bus_service.dto.params;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class DashboardFilterParamsRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate serviceDate;

    private Long schoolId;

    private String direction;
}
