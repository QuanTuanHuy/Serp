/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AutoGrantBackfillResponse {
    private Long organizationId;
    private Long moduleId;
    private Integer grantedCount;
    private Integer skippedCount;
    @Builder.Default
    private Map<String, Integer> skippedReasons = new LinkedHashMap<>();

    public static AutoGrantBackfillResponse empty(Long organizationId, Long moduleId) {
        return AutoGrantBackfillResponse.builder()
                .organizationId(organizationId)
                .moduleId(moduleId)
                .grantedCount(0)
                .skippedCount(0)
                .skippedReasons(new LinkedHashMap<>())
                .build();
    }

    public void markGranted() {
        this.grantedCount = safeCount(this.grantedCount) + 1;
    }

    public void markGranted(long count) {
        if (count <= 0) {
            return;
        }
        int boundedCount = count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
        this.grantedCount = safeCount(this.grantedCount) + boundedCount;
    }

    public void markSkipped(String reason) {
        this.skippedCount = safeCount(this.skippedCount) + 1;
        this.skippedReasons.merge(reason, 1, Integer::sum);
    }

    public void markSkipped(String reason, long count) {
        if (count <= 0) {
            return;
        }
        int boundedCount = count > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) count;
        this.skippedCount = safeCount(this.skippedCount) + boundedCount;
        this.skippedReasons.merge(reason, boundedCount, Integer::sum);
    }

    private int safeCount(Integer value) {
        return value == null ? 0 : value;
    }
}
