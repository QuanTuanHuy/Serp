/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issyetype.entity;

import java.util.List;

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
public class IssueTypeScreenSchemeEntity extends BaseEntity {
    private Long tenantId;
    private String name;
    private String description;
    private Long defaultScreenSchemeId;

    private List<IssueTypeScreenSchemeItemEntity> items;
}
