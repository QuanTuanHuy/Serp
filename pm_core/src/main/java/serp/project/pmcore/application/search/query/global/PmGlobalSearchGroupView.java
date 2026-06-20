/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import java.util.List;

public record PmGlobalSearchGroupView(
        PmGlobalSearchType type,
        String title,
        long total,
        List<PmGlobalSearchItemView> items
) {
}
