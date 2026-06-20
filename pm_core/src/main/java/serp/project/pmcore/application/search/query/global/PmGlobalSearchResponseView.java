/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import java.util.List;

public record PmGlobalSearchResponseView(
        String query,
        int limit,
        List<PmGlobalSearchGroupView> groups
) {
}
