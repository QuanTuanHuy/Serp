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
public class FieldConfigItemEntity {
    private Long tenantId;
    private Long fieldConfigId;
    private String fieldRefType;
    private String fieldRef;
    private Boolean isRequired;
    private Boolean isHidden;
    private String rendererKey;
    private Integer sequence;
}
