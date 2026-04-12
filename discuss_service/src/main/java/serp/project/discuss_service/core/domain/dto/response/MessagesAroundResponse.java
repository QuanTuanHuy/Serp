/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Messages around response DTO
 */

package serp.project.discuss_service.core.domain.dto.response;

import java.util.List;

public record MessagesAroundResponse(
        List<MessageResponse> messages,
        boolean hasBefore,
        boolean hasAfter
) {
}
