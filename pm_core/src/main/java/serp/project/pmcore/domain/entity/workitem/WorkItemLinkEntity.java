/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.entity.workitem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WorkItemLinkEntity extends BaseEntity {
    private Long tenantId;
    private Long sourceId;
    private Long targetId;
    private Long linkTypeId;
    private Integer sequence;

    private WorkItemLinkTypeEntity linkType;
}
