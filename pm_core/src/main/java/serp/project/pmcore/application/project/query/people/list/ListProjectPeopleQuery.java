/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.project.query.people.list;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.List;
import java.util.Set;

public record ListProjectPeopleQuery(
        Long projectId,
        Long tenantId,
        Long userId,
        Set<String> groupKeys
) implements IQuery<List<ProjectPeopleView>> {
}
