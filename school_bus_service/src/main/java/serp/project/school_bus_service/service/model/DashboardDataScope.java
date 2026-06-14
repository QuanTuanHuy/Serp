/*
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */
package serp.project.school_bus_service.service.model;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class DashboardDataScope {

    private Long tenantId;
    private boolean tenantWide;
    private Long driverProfileId;
    private Long attendantProfileId;
    private Long parentProfileId;
    private Set<Long> allowedSchoolIds = new LinkedHashSet<>();

    public boolean isEmpty() {
        return !tenantWide
                && driverProfileId == null
                && attendantProfileId == null
                && parentProfileId == null;
    }
}
