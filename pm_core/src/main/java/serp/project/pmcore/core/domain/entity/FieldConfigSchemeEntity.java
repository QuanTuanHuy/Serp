/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.entity;

import java.util.List;

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
public class FieldConfigSchemeEntity extends BaseEntity{
    private Long tenantId;
    private String name;
    private String description;
    private Long defaultFieldConfigId;

    private List<FieldConfigSchemeItemEntity> items;
}
