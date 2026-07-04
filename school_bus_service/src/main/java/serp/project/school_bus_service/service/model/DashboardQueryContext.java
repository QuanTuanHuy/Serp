/*
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
package serp.project.school_bus_service.service.model;

import lombok.Data;
import serp.project.school_bus_service.enums.RouteDirection;

import java.time.LocalDate;

@Data
public class DashboardQueryContext {

    private DashboardDataScope dataScope;
    private LocalDate serviceDate;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long schoolId;
    private RouteDirection direction;
}
