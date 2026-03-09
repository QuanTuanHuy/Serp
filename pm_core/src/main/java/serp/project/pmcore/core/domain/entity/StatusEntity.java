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
public class StatusEntity extends BaseEntity {
    private Long tenantId;
    private String statusKey;
    private String name;
    private String description;
    private String iconUrl;
    private Long categoryId;
    private Boolean isSystem;
}
