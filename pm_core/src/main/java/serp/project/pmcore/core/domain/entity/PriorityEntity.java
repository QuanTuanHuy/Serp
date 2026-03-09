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
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityEntity extends BaseEntity {
    private Long tenantId;
    private String name;
    private String description;
    private String iconUrl;
    private String color;
    private Integer sequence;
    private boolean isSystem;
}
