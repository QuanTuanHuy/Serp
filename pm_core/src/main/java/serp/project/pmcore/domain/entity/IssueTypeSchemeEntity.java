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

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class IssueTypeSchemeEntity extends BaseEntity {
    private Long tenantId;
    private String name;
    private String description;
    private Long defaultIssueTypeId;

    private List<IssueTypeSchemeItemEntity> items;
}
