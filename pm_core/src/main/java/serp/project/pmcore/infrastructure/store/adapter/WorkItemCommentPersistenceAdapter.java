/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.workitem.entity.WorkItemCommentEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemCommentReadPort;
import serp.project.pmcore.domain.workitem.port.write.IWorkItemCommentWritePort;
import serp.project.pmcore.infrastructure.store.mapper.WorkItemCommentMapper;
import serp.project.pmcore.infrastructure.store.repository.IWorkItemCommentRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WorkItemCommentPersistenceAdapter implements IWorkItemCommentReadPort, IWorkItemCommentWritePort {

    private final IWorkItemCommentRepository repository;
    private final WorkItemCommentMapper mapper;

    @Override
    public Optional<WorkItemCommentEntity> findById(Long id, Long tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(mapper::toEntity);
    }

    @Override
    public Page<WorkItemCommentEntity> listByWorkItemId(Long workItemId, Long tenantId, Pageable pageable) {
        return repository.findAllActiveByWorkItemId(tenantId, workItemId, pageable).map(mapper::toEntity);
    }

    @Override
    public long countByWorkItemId(Long workItemId, Long tenantId) {
        return repository.countActiveByWorkItemId(tenantId, workItemId);
    }

    @Override
    public WorkItemCommentEntity save(WorkItemCommentEntity comment) {
        return mapper.toEntity(repository.save(mapper.toModel(comment)));
    }
}
