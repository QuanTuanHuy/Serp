/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.algorithm.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Window {
    private Long dateMs; // midnight timestamp
    private Integer startMin;
    private Integer endMin;
    private Boolean isDeepWork; // Whether this is a deep work window
}
