/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.IssueTypeEntity;
import serp.project.pmcore.core.port.store.IIssueTypePort;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeMapper;
import serp.project.pmcore.infrastructure.store.repository.IIssueTypeRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueTypeAdapter implements IIssueTypePort {

    private final IIssueTypeRepository issueTypeRepository;
    private final IssueTypeMapper issueTypeMapper;

    @Override
    public IssueTypeEntity createIssueType(IssueTypeEntity issueType) {
        return issueTypeMapper.toEntity(issueTypeRepository.save(issueTypeMapper.toModel(issueType)));
    }

    @Override
    public Optional<IssueTypeEntity> getIssueTypeById(Long issueTypeId, Long tenantId) {
        return issueTypeRepository.findByIdAndTenantId(issueTypeId, tenantId)
                .map(issueTypeMapper::toEntity);
    }

    @Override
    public List<IssueTypeEntity> listIssueTypes(Long tenantId) {
        return issueTypeMapper.toEntities(issueTypeRepository.findAllByTenantIdOrderByHierarchyLevelAsc(tenantId));
    }

    @Override
    public void updateIssueType(IssueTypeEntity issueType) {
        issueTypeRepository.save(issueTypeMapper.toModel(issueType));
    }

    @Override
    public void deleteIssueType(Long issueTypeId, Long tenantId) {
        issueTypeRepository.deleteByIdAndTenantId(issueTypeId, tenantId);
    }

    @Override
    public boolean existsByTypeKey(Long tenantId, String typeKey) {
        return issueTypeRepository.existsByTenantIdAndTypeKey(tenantId, typeKey);
    }
}
