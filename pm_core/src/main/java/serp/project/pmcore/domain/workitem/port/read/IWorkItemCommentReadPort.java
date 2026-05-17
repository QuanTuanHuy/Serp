/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.port.read;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;

import java.util.Optional;

public interface IWorkItemCommentReadPort {
    Optional<WorkItemCommentEntity> findById(Long id, Long tenantId);

    Page<WorkItemCommentEntity> listByWorkItemId(Long workItemId, Long tenantId, Pageable pageable);

    long countByWorkItemId(Long workItemId, Long tenantId);
}
