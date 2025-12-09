/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.entity.TeamMemberEntity;
import serp.project.crm.core.domain.enums.TeamMemberStatus;
import serp.project.crm.core.port.store.ITeamMemberPort;
import serp.project.crm.infrastructure.store.mapper.TeamMemberMapper;
import serp.project.crm.infrastructure.store.repository.TeamMemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeamMemberAdapter implements ITeamMemberPort {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamMemberMapper teamMemberMapper;

    @Override
    public TeamMemberEntity save(TeamMemberEntity teamMemberEntity) {
        var model = teamMemberMapper.toModel(teamMemberEntity);
        var savedModel = teamMemberRepository.save(model);
        return teamMemberMapper.toEntity(savedModel);
    }

    @Override
    public Optional<TeamMemberEntity> findById(Long id, Long tenantId) {
        return teamMemberRepository.findByIdAndTenantId(id, tenantId)
                .map(teamMemberMapper::toEntity);
    }

    @Override
    public Pair<List<TeamMemberEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var pageable = teamMemberMapper.toPageable(pageRequest);
        var page = teamMemberRepository.findByTenantId(tenantId, pageable)
                .map(teamMemberMapper::toEntity);
        return teamMemberMapper.pageToPair(page);
    }

    @Override
    public Optional<TeamMemberEntity> findByUserId(Long userId, Long tenantId) {
        return teamMemberRepository.findByTenantIdAndUserId(tenantId, userId)
                .map(teamMemberMapper::toEntity);
    }

    @Override
    public Pair<List<TeamMemberEntity>, Long> findByTeamId(Long teamId, Long tenantId, PageRequest pageRequest) {
        var pageable = teamMemberMapper.toPageable(pageRequest);
        var page = teamMemberRepository.findByTenantIdAndTeamId(tenantId, teamId, pageable)
                .map(teamMemberMapper::toEntity);
        return teamMemberMapper.pageToPair(page);
    }

    @Override
    public Optional<TeamMemberEntity> findByTeamIdAndUserId(Long teamId, Long userId, Long tenantId) {
        return teamMemberRepository.findByTenantIdAndTeamIdAndUserId(tenantId, teamId, userId)
                .map(teamMemberMapper::toEntity);
    }

    @Override
    public Pair<List<TeamMemberEntity>, Long> findByStatus(TeamMemberStatus status, Long tenantId,
            PageRequest pageRequest) {
        return Pair.of(List.of(), 0L);
    }

    @Override
    public Long countByTeamIdAndStatus(Long teamId, TeamMemberStatus status, Long tenantId) {
        return teamMemberRepository.countByTenantIdAndTeamIdAndStatus(tenantId, teamId, status.name());
    }

    @Override
    public Boolean existsByTeamIdAndUserId(Long teamId, Long userId, Long tenantId) {
        return teamMemberRepository.existsByTenantIdAndTeamIdAndUserId(tenantId, teamId, userId);
    }

    @Override
    public void deleteById(Long id, Long tenantId) {
        teamMemberRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(teamMemberRepository::delete);
    }

    @Override
    public List<TeamMemberEntity> findAllByTeamId(Long teamId, Long tenantId) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, 1000);
        return teamMemberRepository.findByTenantIdAndTeamId(tenantId, teamId, pageable)
                .stream()
                .map(teamMemberMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllByTeamId(Long teamId, Long tenantId) {
        teamMemberRepository.deleteByTeamIdAndTenantId(teamId, tenantId);
    }
}
