/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.TeamEntity;
import serp.project.crm.core.port.store.ITeamPort;
import serp.project.crm.infrastructure.store.mapper.TeamMapper;
import serp.project.crm.infrastructure.store.repository.TeamRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeamAdapter implements ITeamPort {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    @Override
    public TeamEntity save(TeamEntity teamEntity) {
        var model = teamMapper.toModel(teamEntity);
        var savedModel = teamRepository.save(model);
        return teamMapper.toEntity(savedModel);
    }

    @Override
    public Optional<TeamEntity> findById(Long id, Long tenantId) {
        return teamRepository.findByIdAndTenantId(id, tenantId)
                .map(teamMapper::toEntity);
    }

    @Override
    public Pair<List<TeamEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var pageable = teamMapper.toPageable(pageRequest);
        var page = teamRepository.findByTenantId(tenantId, pageable)
                .map(teamMapper::toEntity);
        return teamMapper.pageToPair(page);
    }

    @Override
    public Pair<List<TeamEntity>, Long> findByLeaderId(Long leaderId, Long tenantId, PageRequest pageRequest) {
        var pageable = teamMapper.toPageable(pageRequest);
        var page = teamRepository.findByTenantIdAndLeaderId(tenantId, leaderId, pageable)
                .map(teamMapper::toEntity);
        return teamMapper.pageToPair(page);
    }

    @Override
    public Boolean existsByName(String name, Long tenantId) {
        return teamRepository.existsByNameAndTenantId(name, tenantId);
    }

    @Override
    public void deleteById(Long id, Long tenantId) {
        teamRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(teamRepository::delete);
    }

    @Override
    public List<TeamEntity> findAllByTenantId(Long tenantId) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 1000);
        return teamRepository.findByTenantId(tenantId, pageable)
                .stream()
                .map(teamMapper::toEntity)
                .collect(Collectors.toList());
    }
}
