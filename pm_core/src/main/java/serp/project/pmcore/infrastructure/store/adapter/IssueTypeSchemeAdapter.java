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

import serp.project.pmcore.domain.issuetype.entity.IssueTypeSchemeEntity;
import serp.project.pmcore.domain.issuetype.port.IIssueTypeSchemePort;
import serp.project.pmcore.domain.issuetype.query.IssueTypeSchemeListCriteria;
import serp.project.pmcore.domain.shared.pagination.PageResult;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.mapper.IssueTypeSchemeMapper;
import serp.project.pmcore.infrastructure.store.model.IssueTypeSchemeModel;
import serp.project.pmcore.infrastructure.store.repository.IIssueTypeSchemeItemRepository;
import serp.project.pmcore.infrastructure.store.repository.IIssueTypeSchemeRepository;
import serp.project.pmcore.infrastructure.store.support.PageableUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class IssueTypeSchemeAdapter implements IIssueTypeSchemePort {

    private final IIssueTypeSchemeRepository issueTypeSchemeRepository;
    private final IIssueTypeSchemeItemRepository issueTypeSchemeItemRepository;
    private final IssueTypeSchemeMapper issueTypeSchemeMapper;
    private final IssueTypeSchemeItemMapper issueTypeSchemeItemMapper;

    @Override
    public IssueTypeSchemeEntity createIssueTypeScheme(IssueTypeSchemeEntity scheme) {
        return issueTypeSchemeMapper.toEntity(issueTypeSchemeRepository.save(issueTypeSchemeMapper.toModel(scheme)));
    }

    @Override
    public Optional<IssueTypeSchemeEntity> getIssueTypeSchemeById(Long schemeId, Long tenantId) {
        return issueTypeSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(issueTypeSchemeMapper::toEntity);
    }

    @Override
    public Optional<IssueTypeSchemeEntity> getIssueTypeSchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return issueTypeSchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(issueTypeSchemeMapper::toEntity);
    }

    @Override
    public Optional<IssueTypeSchemeEntity> getIssueTypeSchemeWithItems(Long schemeId, Long tenantId) {
        return issueTypeSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(model -> {
                    IssueTypeSchemeEntity scheme = issueTypeSchemeMapper.toEntity(model);
                    scheme.setItems(issueTypeSchemeItemMapper.toEntities(
                            issueTypeSchemeItemRepository.findAllByTenantIdAndSchemeIdOrderBySequenceAsc(tenantId, schemeId)
                    ));
                    return scheme;
                });
    }

    @Override
    public List<IssueTypeSchemeEntity> listIssueTypeSchemes(Long tenantId) {
        return issueTypeSchemeMapper.toEntities(issueTypeSchemeRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId));
    }

    @Override
    public PageResult<IssueTypeSchemeEntity> listIssueTypeSchemesIncludingSystem(Long tenantId,
                                                                                 IssueTypeSchemeListCriteria criteria) {
        Pageable pageable = PageableUtils.of(criteria, resolveSort(criteria));
        Page<IssueTypeSchemeModel> result = issueTypeSchemeRepository.findAllVisibleWithFilters(
                tenantId,
                criteria.getSearch(),
                criteria.getIsSystem(),
                pageable
        );
        return new PageResult<>(issueTypeSchemeMapper.toEntities(result.getContent()), result.getTotalElements());
    }

    @Override
    public void updateIssueTypeScheme(IssueTypeSchemeEntity scheme) {
        issueTypeSchemeRepository.save(issueTypeSchemeMapper.toModel(scheme));
    }

    @Override
    public void deleteIssueTypeScheme(Long schemeId, Long tenantId) {
        issueTypeSchemeRepository.deleteByIdAndTenantId(schemeId, tenantId);
    }

    @Override
    public boolean existsByName(Long tenantId, String name) {
        return issueTypeSchemeRepository.existsByTenantIdAndName(tenantId, name);
    }

    @Override
    public boolean existsByDefaultIssueTypeId(Long issueTypeId, Long tenantId) {
        return issueTypeSchemeRepository.existsByDefaultIssueTypeIdAndTenantId(issueTypeId, tenantId);
    }

    private Sort resolveSort(IssueTypeSchemeListCriteria criteria) {
        String sortBy = criteria.getSortBy().toLowerCase();
        Sort.Direction direction = PageableUtils.resolveDirection(criteria.getSortDirection());

        return switch (sortBy) {
            case "name" -> Sort.by(
                    new Sort.Order(direction, "name"),
                    new Sort.Order(direction, "id")
            );
            case "created_at" -> Sort.by(
                    new Sort.Order(direction, "createdAt"),
                    new Sort.Order(direction, "id")
            );
            default -> throw new IllegalArgumentException("sortBy must be one of name, created_at");
        };
    }
}
