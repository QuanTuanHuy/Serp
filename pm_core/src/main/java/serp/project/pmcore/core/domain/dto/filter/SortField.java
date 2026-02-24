/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortField {

    private String field;

    @Builder.Default
    private String direction = "ASC";

    /**
     * NULLS FIRST or NULLS LAST. Defaults to LAST.
     */
    @Builder.Default
    private String nullsPosition = "LAST";
}
