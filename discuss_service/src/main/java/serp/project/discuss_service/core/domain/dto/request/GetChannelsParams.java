/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.discuss_service.core.domain.enums.ChannelType;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@SuperBuilder
public class GetChannelsParams extends BaseGetParams {
    
    private ChannelType type;
    private Boolean isArchived;
    private String entityType;
    private Long entityId;
    private String searchQuery;

    public ChannelType getType() {
        return type;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
