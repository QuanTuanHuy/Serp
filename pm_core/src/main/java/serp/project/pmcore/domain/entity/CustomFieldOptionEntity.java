/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.entity;

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
public class CustomFieldOptionEntity extends BaseEntity {
    private Long tenantId;
    private Long customFieldContextId;
    private String optionKey;
    private String value;
    private Integer sequence;
    private Long parentOptionId;
    private Boolean isDisabled;
}
