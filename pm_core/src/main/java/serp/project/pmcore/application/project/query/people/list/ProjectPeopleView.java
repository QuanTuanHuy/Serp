/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.people.list;

import java.util.List;

public record ProjectPeopleView(
        Long userId,
        String name,
        String email,
        String avatarUrl,
        boolean projectLead,
        List<RoleView> roles,
        Long addedAt
) {
    public record RoleView(Long id, String name, boolean system) {
    }
}
