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
public class IssueSecurityLevelMemberEntity extends BaseEntity {
    private Long tenantId;
    private Long levelId;
    private String subjectType;
    private String subjectRef;
    private Long customFieldId;
}
