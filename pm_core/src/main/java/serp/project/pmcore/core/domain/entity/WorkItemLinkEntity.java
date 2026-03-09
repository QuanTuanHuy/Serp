/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
