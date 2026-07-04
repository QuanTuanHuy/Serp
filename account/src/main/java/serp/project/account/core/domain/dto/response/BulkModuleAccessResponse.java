/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
public class BulkModuleAccessResponse {
    private Long moduleId;
    private Integer requestedCount;
    private Integer grantedCount;
    private Integer revokedCount;
    private Integer skippedCount;

    @Builder.Default
    private List<Long> grantedUserIds = new ArrayList<>();

    @Builder.Default
    private List<Long> revokedUserIds = new ArrayList<>();

    @Builder.Default
    private List<BulkModuleAccessSkippedUser> skippedUsers = new ArrayList<>();

    @Builder.Default
    private Map<String, Integer> skippedReasons = new LinkedHashMap<>();

    public static BulkModuleAccessResponse empty(Long moduleId, int requestedCount) {
        return BulkModuleAccessResponse.builder()
                .moduleId(moduleId)
                .requestedCount(requestedCount)
                .grantedCount(0)
                .revokedCount(0)
                .skippedCount(0)
                .grantedUserIds(new ArrayList<>())
                .revokedUserIds(new ArrayList<>())
                .skippedUsers(new ArrayList<>())
                .skippedReasons(new LinkedHashMap<>())
                .build();
    }

    public void markGranted(Long userId) {
        this.grantedCount = safeCount(this.grantedCount) + 1;
        this.grantedUserIds.add(userId);
    }

    public void markRevoked(Long userId) {
        this.revokedCount = safeCount(this.revokedCount) + 1;
        this.revokedUserIds.add(userId);
    }

    public void markSkipped(Long userId, String reason) {
        this.skippedCount = safeCount(this.skippedCount) + 1;
        this.skippedUsers.add(BulkModuleAccessSkippedUser.builder()
                .userId(userId)
                .reason(reason)
                .build());
        this.skippedReasons.merge(reason, 1, Integer::sum);
    }

    private int safeCount(Integer value) {
        return value == null ? 0 : value;
    }
}
