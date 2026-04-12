/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.fieldconfig.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FieldConfigItemEntity extends BaseEntity {
    private Long tenantId;
    private Long fieldConfigId;
    private String fieldRefType;
    private String fieldRef;
    private Boolean isRequired;
    private Boolean isHidden;
    private String rendererKey;
    private Integer sequence;
}
