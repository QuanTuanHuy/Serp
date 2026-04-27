/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.constant;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class TeamMemberRole {
    public static final String MANAGER = "MANAGER";
    public static final String SALES_REP = "SALES_REP";
    public static final String VIEWER = "VIEWER";

    public static final List<String> ALL = List.of(MANAGER, SALES_REP, VIEWER);
    public static final List<String> MANAGER_DEMOTION_TARGETS = List.of(SALES_REP, VIEWER);
}
