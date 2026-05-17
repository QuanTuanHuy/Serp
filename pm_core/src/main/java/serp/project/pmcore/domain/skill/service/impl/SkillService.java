/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.skill.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.skill.dto.UserSkillDraftData;
import serp.project.pmcore.domain.skill.dto.WorkItemSkillDraftData;
import serp.project.pmcore.domain.skill.entity.SkillEntity;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;
import serp.project.pmcore.domain.skill.enums.SkillSource;
import serp.project.pmcore.domain.skill.port.read.ISkillReadPort;
import serp.project.pmcore.domain.skill.port.read.IUserSkillReadPort;
import serp.project.pmcore.domain.skill.port.read.IWorkItemSkillReadPort;
import serp.project.pmcore.domain.skill.port.write.ISkillWritePort;
import serp.project.pmcore.domain.skill.port.write.IUserSkillWritePort;
import serp.project.pmcore.domain.skill.port.write.IWorkItemSkillWritePort;
import serp.project.pmcore.domain.skill.service.ISkillService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService implements ISkillService {
    private final ISkillReadPort skillReadPort;
    private final ISkillWritePort skillWritePort;
    private final IWorkItemSkillReadPort workItemSkillReadPort;
    private final IWorkItemSkillWritePort workItemSkillWritePort;
    private final IUserSkillReadPort userSkillReadPort;
    private final IUserSkillWritePort userSkillWritePort;
    private final IWorkItemReadPort workItemReadPort;

    @Override
    public List<SkillEntity> listSkills(Long tenantId) {
        return skillReadPort.listActive(tenantId);
    }

    @Override
    public SkillEntity createSkill(Long tenantId, Long userId, String code, String name, String description) {
        String normalizedCode = normalizeCode(code);
        requireUniqueCode(tenantId, normalizedCode, null);
        long now = System.currentTimeMillis();
        SkillEntity draft = SkillEntity.builder()
                .tenantId(tenantId)
                .code(normalizedCode)
                .name(name)
                .description(description)
                .active(true)
                .deletedAt(null)
                .build();
        draft.applyCreate(userId, now);
        return skillWritePort.save(draft);
    }

    @Override
    public SkillEntity updateSkill(Long tenantId, Long userId, Long skillId, String code, String name, String description) {
        SkillEntity skill = getActiveSkill(tenantId, skillId);
        String normalizedCode = normalizeCode(code);
        requireUniqueCode(tenantId, normalizedCode, skillId);
        skill.setCode(normalizedCode);
        skill.setName(name);
        skill.setDescription(description);
        skill.applyUpdate(userId, System.currentTimeMillis());
        return skillWritePort.save(skill);
    }

    @Override
    public SkillEntity archiveSkill(Long tenantId, Long userId, Long skillId) {
        SkillEntity skill = getActiveSkill(tenantId, skillId);
        long now = System.currentTimeMillis();
        skill.setActive(false);
        skill.setDeletedAt(now);
        skill.applyUpdate(userId, now);
        return skillWritePort.save(skill);
    }

    @Override
    public List<WorkItemSkillEntity> listWorkItemSkills(Long tenantId, Long projectId, Long workItemId) {
        requireWorkItemInProject(tenantId, projectId, workItemId);
        return workItemSkillReadPort.listActive(tenantId, projectId, workItemId);
    }

    @Override
    public List<WorkItemSkillEntity> replaceWorkItemSkills(
            Long tenantId,
            Long userId,
            Long projectId,
            Long workItemId,
            List<WorkItemSkillDraftData> drafts) {
        requireWorkItemInProject(tenantId, projectId, workItemId);
        List<Long> skillIds = drafts.stream().map(WorkItemSkillDraftData::skillId).toList();
        validateUniqueSkillIds(skillIds);
        validateActiveSkillIds(tenantId, skillIds);
        long now = System.currentTimeMillis();
        workItemSkillWritePort.softDeleteActive(tenantId, projectId, workItemId, userId, now);
        List<WorkItemSkillEntity> newSkills = drafts.stream()
                .map(draft -> {
                    WorkItemSkillEntity entity = WorkItemSkillEntity.builder()
                            .tenantId(tenantId)
                            .projectId(projectId)
                            .workItemId(workItemId)
                            .skillId(draft.skillId())
                            .requirementType(draft.requirementType())
                            .minProficiency(draft.minProficiency())
                            .weight(draft.weight())
                            .source(draft.source() == null ? SkillSource.MANUAL : draft.source())
                            .deletedAt(null)
                            .build();
                    entity.applyCreate(userId, now);
                    return entity;
                })
                .toList();
        return workItemSkillWritePort.saveAll(newSkills);
    }

    @Override
    public List<UserSkillEntity> listUserSkills(Long tenantId, Long userId) {
        return userSkillReadPort.listActive(tenantId, userId);
    }

    @Override
    public List<UserSkillEntity> replaceUserSkills(
            Long tenantId,
            Long targetUserId,
            Long actorUserId,
            List<UserSkillDraftData> drafts) {
        List<Long> skillIds = drafts.stream().map(UserSkillDraftData::skillId).toList();
        validateUniqueSkillIds(skillIds);
        validateActiveSkillIds(tenantId, skillIds);
        long now = System.currentTimeMillis();
        userSkillWritePort.softDeleteActive(tenantId, targetUserId, actorUserId, now);
        List<UserSkillEntity> newSkills = drafts.stream()
                .map(draft -> {
                    UserSkillEntity entity = UserSkillEntity.builder()
                            .tenantId(tenantId)
                            .userId(targetUserId)
                            .skillId(draft.skillId())
                            .proficiency(draft.proficiency())
                            .confidence(draft.confidence())
                            .source(draft.source() == null ? SkillSource.MANUAL : draft.source())
                            .verifiedAt(draft.verifiedAt())
                            .deletedAt(null)
                            .build();
                    entity.applyCreate(actorUserId, now);
                    return entity;
                })
                .toList();
        return userSkillWritePort.saveAll(newSkills);
    }

    private SkillEntity getActiveSkill(Long tenantId, Long skillId) {
        return skillReadPort.findActiveById(tenantId, skillId)
                .orElseThrow(() -> {
                    log.error("Skill not found: tenantId={}, skillId={}", tenantId, skillId);
                    return new ResourceNotFoundException(DomainErrorCode.NOT_FOUND, "Skill not found: id=" + skillId);
                });
    }

    private void requireUniqueCode(Long tenantId, String code, Long currentId) {
        skillReadPort.findActiveByCode(tenantId, code)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    log.error("Active skill code already exists: tenantId={}, code={}, existingId={}", tenantId, code, existing.getId());
                    throw new BusinessRuleViolationException(DomainErrorCode.CONFLICT, "Active skill code already exists: " + code);
                });
    }

    private String normalizeCode(String code) {
        return code.trim().toLowerCase(Locale.ROOT);
    }

    private void validateActiveSkillIds(Long tenantId, List<Long> skillIds) {
        List<SkillEntity> activeSkills = skillReadPort.listActiveByIds(tenantId, skillIds);
        Set<Long> activeSkillIds = activeSkills.stream().map(SkillEntity::getId).collect(Collectors.toSet());
        for (Long skillId : skillIds) {
            if (!activeSkillIds.contains(skillId)) {
                log.error("Skill id does not exist or is not active: tenantId={}, skillId={}", tenantId, skillId);
                throw new ResourceNotFoundException(DomainErrorCode.NOT_FOUND, "Skill not found: id=" + skillId);
            }
        }
    }

    private void validateUniqueSkillIds(List<Long> skillIds) {
        Set<Long> seen = new HashSet<>();
        for (Long skillId : skillIds) {
            if (!seen.add(skillId)) {
                log.error("Duplicate skill id in request: skillId={}", skillId);
                throw new BusinessRuleViolationException(DomainErrorCode.CONFLICT, "Duplicate skill id: " + skillId);
            }
        }
    }

    private void requireWorkItemInProject(Long tenantId, Long projectId, Long workItemId) {
        WorkItemEntity workItem = workItemReadPort.getWorkItemById(workItemId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.workItem(workItemId));
        if (!projectId.equals(workItem.getProjectId())) {
            log.error("Work item does not belong to the specified project: tenantId={}, projectId={}, workItemId={}", tenantId, projectId, workItemId);
            throw ResourceNotFoundException.workItem(workItemId);
        }
    }
}
