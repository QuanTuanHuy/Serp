/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Channel presence response DTO
 */


package serp.project.discuss_service.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ChannelPresenceResponse {
    private Long channelId;
    private Integer totalMembers;
    private Integer onlineCount;
    private Map<String, List<UserPresenceResponse>> statusGroups;

    public static ChannelPresenceResponse of(
            Long channelId,
            Integer totalMembers,
            Integer onlineCount,
            Map<String, List<UserPresenceResponse>> statusGroups) {
        return ChannelPresenceResponse.builder()
                .channelId(channelId)
                .totalMembers(totalMembers)
                .onlineCount(onlineCount)
                .statusGroups(statusGroups)
                .build();
    }
}
