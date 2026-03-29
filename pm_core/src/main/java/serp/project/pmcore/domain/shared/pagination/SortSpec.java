/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.pagination;

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
public class SortSpec {

    private String field;

    @Builder.Default
    private String direction = "ASC";

    @Builder.Default
    private String nullsPosition = "LAST";
}
