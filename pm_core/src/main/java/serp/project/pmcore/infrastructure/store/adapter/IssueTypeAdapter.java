/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuetype.entity.IssueTypeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypePort;
import serp.project.pmcore.domain.issuetype.query.IssueTypeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeMapper;
import serp.project.pmcore.infrastructure.store.model.IssueTypeModel;
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
    public Optional<IssueTypeEntity> getIssueTypeByIdIncludingSystem(Long issueTypeId, Long tenantId) {
        return issueTypeRepository.findByIdAndTenantIdOrSystemTenant(issueTypeId, tenantId)
                .map(issueTypeMapper::toEntity);
    }

    @Override
    public Optional<IssueTypeEntity> getIssueTypeByTypeKey(Long tenantId, String typeKey) {
        return issueTypeRepository.findFirstByTenantIdAndTypeKeyOrderByIdAsc(tenantId, typeKey)
                .map(issueTypeMapper::toEntity);
    }

    @Override
    public Optional<IssueTypeEntity> getIssueTypeByTypeKeyIncludingSystem(Long tenantId, String typeKey) {
        return issueTypeRepository.findByTypeKeyAndTenantIdOrSystemTenant(typeKey, tenantId)
                .stream()
                .findFirst()
                .map(issueTypeMapper::toEntity);
    }

    @Override
    public List<IssueTypeEntity> listIssueTypes(Long tenantId) {
        return issueTypeMapper.toEntities(issueTypeRepository.findAllByTenantIdOrderByHierarchyLevelAsc(tenantId));
    }

    @Override
    public List<IssueTypeEntity> listIssueTypesIncludingSystem(Long tenantId) {
        return issueTypeMapper.toEntities(issueTypeRepository.findAllByTenantIdOrSystemTenant(tenantId));
    }

    @Override
    public List<IssueTypeEntity> getIssueTypesByIdsIncludingSystem(List<Long> issueTypeIds, Long tenantId) {
        if (issueTypeIds == null || issueTypeIds.isEmpty()) {
            return List.of();
        }
        return issueTypeMapper.toEntities(
                issueTypeRepository.findAllByIdInAndTenantIdOrSystemTenant(issueTypeIds, tenantId)
        );
    }

    @Override
    public PageResult<IssueTypeEntity> listIssueTypesIncludingSystem(Long tenantId, IssueTypeListCriteria criteria) {
        Pageable pageable = PageRequest.of(resolvePage(criteria), resolvePageSize(criteria), resolveSort(criteria));
        Page<IssueTypeModel> result = issueTypeRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getHierarchyLevel(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(issueTypeMapper.toEntities(result.getContent()), result.getTotalElements());
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

    private int resolvePage(IssueTypeListCriteria criteria) {
        int page = criteria.getPage();
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        return page;
    }

    private int resolvePageSize(IssueTypeListCriteria criteria) {
        int pageSize = criteria.getPageSize();
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize must be between 1 and 100");
        }
        return pageSize;
    }

    private Sort resolveSort(IssueTypeListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        String sortDirection = criteria.getSortDirection().toUpperCase();

        Sort.Direction direction = switch (sortDirection) {
            case "ASC" -> Sort.Direction.ASC;
            case "DESC" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException("sortDirection must be ASC or DESC");
        };

        return switch (sortBy) {
            case "hierarchy_level" -> Sort.by(
                    new Sort.Order(direction, "hierarchyLevel"),
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "name" -> Sort.by(
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "type_key" -> Sort.by(
                    new Sort.Order(direction, "typeKey"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException("sortBy must be one of hierarchy_level, name, type_key, created_at");
        };
    }
}
