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

/**
 * Reusable sort specification with null-handling control.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SortField {

    /**
     * Column name to sort by. Must pass whitelist validation in the query builder.
     */
    private String field;

    /**
     * ASC or DESC. Defaults to ASC if null/invalid.
     */
    @Builder.Default
    private String direction = "ASC";

    /**
     * NULLS FIRST or NULLS LAST. Defaults to LAST.
     */
    @Builder.Default
    private String nullsPosition = "LAST";
}
