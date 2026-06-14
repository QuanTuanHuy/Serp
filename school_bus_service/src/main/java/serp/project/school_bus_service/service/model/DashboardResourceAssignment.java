/*
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
package serp.project.school_bus_service.service.model;

import lombok.Data;

@Data
public class DashboardResourceAssignment {

    private boolean busMissing;
    private boolean driverMissing;
    private boolean attendantMissing;
}
