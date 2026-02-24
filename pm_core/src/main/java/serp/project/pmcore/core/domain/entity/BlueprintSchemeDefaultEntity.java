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
import serp.project.pmcore.core.domain.enums.SchemeType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BlueprintSchemeDefaultEntity extends BaseEntity {
    private Long tenantId;
    private Long blueprintId;
    private SchemeType schemeType;
    private Long schemeId;
}
