/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityStatsResponse {
    private Long total;
    private Long overdue;
    private Long upcoming;
    private Map<String, Long> byStatus;
    private Map<String, Long> byType;
    private Map<String, Long> byPriority;
}
