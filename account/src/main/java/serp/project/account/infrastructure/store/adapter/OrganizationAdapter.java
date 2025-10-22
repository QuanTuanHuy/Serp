/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.port.store.IOrganizationPort;
import serp.project.account.infrastructure.store.mapper.OrganizationMapper;
import serp.project.account.infrastructure.store.model.OrganizationModel;
import serp.project.account.infrastructure.store.repository.IOrganizationRepository;
import serp.project.account.infrastructure.store.specification.OrganizationSpecification;
import serp.project.account.kernel.utils.CollectionUtils;
import serp.project.account.kernel.utils.PaginationUtils;

@Component
@RequiredArgsConstructor
public class OrganizationAdapter implements IOrganizationPort {
    private final IOrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    private final PaginationUtils paginationUtils;

    @Override
    public OrganizationEntity save(OrganizationEntity organization) {
        OrganizationModel organizationModel = organizationMapper.toModel(organization);
        return organizationMapper.toEntity(organizationRepository.save(organizationModel));
    }

    @Override
    public OrganizationEntity getById(Long id) {
        return organizationRepository.findById(id)
                .map(organizationMapper::toEntity)
                .orElse(null);
    }

    @Override
    public boolean existsByCode(String code) {
        return organizationRepository.findByCode(code).isPresent();
    }

    @Override
    public List<OrganizationEntity> getByIds(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids) && ids.size() == 1) {
            return List.of(getById(ids.getFirst()));
        }
        return organizationMapper.toEntityList(organizationRepository.findByIdIn(ids));
    }

    @Override
    public OrganizationEntity getOrganizationByName(String name) {
        return organizationRepository.findByName(name)
                .map(organizationMapper::toEntity)
                .orElse(null);
    }

    @Override
    public Pair<List<OrganizationEntity>, Long> getOrganizations(GetOrganizationParams params) {
        var pageable = paginationUtils.getPageable(params);
        var specification = OrganizationSpecification.getOrganizations(params.getSearch(), params.getStatus(),
                params.getType());
        var page = organizationRepository.findAll(specification, pageable);

        var organizations = organizationMapper.toEntityList(page.getContent());
        return Pair.of(organizations, page.getTotalElements());
    }
}
