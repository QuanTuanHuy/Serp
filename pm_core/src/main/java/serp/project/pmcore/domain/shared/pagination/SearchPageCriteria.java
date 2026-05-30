/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.pagination;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@SuperBuilder
public class SearchPageCriteria extends PageCriteria {
    private String search;

    public String getSearch() {
        if (search == null) {
            return null;
        }

        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public String getSearchPattern() {
        String search = getSearch();
        return search != null ? "%" + search + "%" : null;
    }

    public String getSearchPatternLower() {
        String search = getSearch();
        return search != null ? "%" + search.toLowerCase() + "%" : null;
    }
}
