/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.provisioning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisioningExecutionContext {

    private Long projectId;
    private String projectKey;

    @Builder.Default
    private Map<Long, Long> screenIdMap = new HashMap<>();

    public Long getMappedScreenId(Long sourceScreenId) {
        if (sourceScreenId == null) {
            return null;
        }
        return screenIdMap.get(sourceScreenId);
    }

    public void rememberScreenId(Long sourceScreenId, Long targetScreenId) {
        if (sourceScreenId == null || targetScreenId == null) {
            return;
        }
        screenIdMap.putIfAbsent(sourceScreenId, targetScreenId);
    }
}
