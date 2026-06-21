/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import serp.project.pmcore.application.shared.cqrs.query.IQuery;

import java.util.Set;

public record PmGlobalSearchQuery(
        Long tenantId,
        Long userId,
        Set<String> groupKeys,
        String query,
        Integer limit,
        Long currentProjectId
) implements IQuery<PmGlobalSearchResponseView> {
}
