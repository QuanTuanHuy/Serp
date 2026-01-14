/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Request DTO for getting messages with filters
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@SuperBuilder
public class GetMessagesParams extends BaseGetParams {
    
    private Long beforeId;
    private Long afterId;
    private Long parentId;
    private String searchQuery;

    public Long getBeforeId() {
        return beforeId;
    }

    public Long getAfterId() {
        return afterId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
