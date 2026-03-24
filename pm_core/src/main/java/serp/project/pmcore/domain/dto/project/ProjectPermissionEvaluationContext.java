/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPermissionEvaluationContext {
    private Long userId;
    @Builder.Default
    private Set<String> groupKeys = new HashSet<>();
    private Long reporterUserId;
    private Long assigneeUserId;
}
