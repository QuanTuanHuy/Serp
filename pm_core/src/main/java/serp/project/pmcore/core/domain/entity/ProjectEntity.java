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
public class ProjectEntity extends BaseEntity {
    private Long tenantId;
    private String key;
    private String name;
    private String description;
    private String url;
    private Long leadUserId;
    private Long categoryId;
    private String projectTypeKey;
    private Boolean isArchived;
    private Long archivedAt;
    private Long issueTypeSchemeId;
    private Long workflowSchemeId;
    private Long prioritySchemeId;
}
