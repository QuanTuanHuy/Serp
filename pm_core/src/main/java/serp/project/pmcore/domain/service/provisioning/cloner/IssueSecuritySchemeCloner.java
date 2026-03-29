package serp.project.pmcore.domain.service.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecurityLevelMemberEntity;
import serp.project.pmcore.domain.issuesecurity.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelMemberPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecurityLevelPort;
import serp.project.pmcore.domain.issuesecurity.port.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.service.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.service.provisioning.support.CloneNamingHelper;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class IssueSecuritySchemeCloner {

    private final IIssueSecuritySchemePort issueSecuritySchemePort;
    private final IIssueSecurityLevelPort issueSecurityLevelPort;
    private final IIssueSecurityLevelMemberPort issueSecurityLevelMemberPort;
    private final CloneNamingHelper cloneNamingHelper;

    public Long cloneIssueSecurityScheme(IssueSecuritySchemeEntity source,
                                         Long tenantId,
                                         Long userId,
                                         CloneMode cloneMode,
                                         ProvisioningExecutionContext context) {
        validateRequired(source, "source");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        List<IssueSecurityLevelEntity> sourceLevels = issueSecurityLevelPort
                .getIssueSecurityLevelsBySchemeIdIncludingSystem(source.getId(), tenantId);

        long now = System.currentTimeMillis();
        IssueSecuritySchemeEntity cloned = IssueSecuritySchemeEntity.builder()
                .tenantId(tenantId)
                .name(cloneNamingHelper.buildSchemeCloneName(context.getProjectKey(), source.getName(), SchemeType.ISSUE_SECURITY, cloneMode))
                .description(source.getDescription())
                .defaultLevelId(null)
                .build();
        cloned.applyCreate(userId, now);
        IssueSecuritySchemeEntity savedScheme = issueSecuritySchemePort.createIssueSecurityScheme(cloned);

        Map<Long, Long> levelIdMap = cloneLevels(sourceLevels, savedScheme.getId(), tenantId, userId);
        cloneLevelMembers(sourceLevels, tenantId, userId, levelIdMap);

        savedScheme.setDefaultLevelId(
                requireMappedId(levelIdMap, source.getDefaultLevelId())
        );
        issueSecuritySchemePort.updateIssueSecurityScheme(savedScheme);

        log.info("Created {} ISSUE_SECURITY scheme clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), savedScheme.getId(), tenantId);

        return savedScheme.getId();
    }

    private Map<Long, Long> cloneLevels(List<IssueSecurityLevelEntity> sourceLevels,
                                        Long targetSchemeId,
                                        Long tenantId,
                                        Long userId) {
        Map<Long, Long> levelIdMap = new HashMap<>();
        if (sourceLevels.isEmpty()) {
            return levelIdMap;
        }

        long now = System.currentTimeMillis();
        List<IssueSecurityLevelEntity> clonedLevels = new ArrayList<>();
        for (IssueSecurityLevelEntity level : sourceLevels) {
            clonedLevels.add(IssueSecurityLevelEntity.builder()
                    .tenantId(tenantId)
                    .schemeId(targetSchemeId)
                    .name(level.getName())
                    .description(level.getDescription())
                    .createdAt(now)
                    .createdBy(userId)
                    .build());
        }
        List<IssueSecurityLevelEntity> savedLevels = issueSecurityLevelPort.createIssueSecurityLevels(clonedLevels);

        for (int i = 0; i < sourceLevels.size(); i++) {
            levelIdMap.put(sourceLevels.get(i).getId(), savedLevels.get(i).getId());
        }

        return levelIdMap;
    }

    private void cloneLevelMembers(List<IssueSecurityLevelEntity> sourceLevels,
                                   Long tenantId,
                                   Long userId,
                                   Map<Long, Long> levelIdMap) {
        List<IssueSecurityLevelMemberEntity> clonedMembers = new ArrayList<>();

        long now = System.currentTimeMillis();
        for (IssueSecurityLevelEntity sourceLevel : sourceLevels) {
            List<IssueSecurityLevelMemberEntity> sourceMembers = issueSecurityLevelMemberPort
                    .getIssueSecurityLevelMembersByLevelIdIncludingSystem(sourceLevel.getId(), tenantId);

            Long targetLevelId = requireMappedId(levelIdMap, sourceLevel.getId());

            for (IssueSecurityLevelMemberEntity member : sourceMembers) {
                clonedMembers.add(IssueSecurityLevelMemberEntity.builder()
                        .tenantId(tenantId)
                        .levelId(targetLevelId)
                        .subjectType(member.getSubjectType())
                        .subjectRef(member.getSubjectRef())
                        .customFieldId(member.getCustomFieldId())
                        .createdAt(now)
                        .createdBy(userId)
                        .build());
            }
        }

        if (!clonedMembers.isEmpty()) {
            issueSecurityLevelMemberPort.createIssueSecurityLevelMembers(clonedMembers);
        }
    }

    private Long requireMappedId(Map<Long, Long> mapping, Long sourceId) {
        if (sourceId == null) {
            return null;
        }

        Long mappedId = mapping.get(sourceId);
        if (mappedId == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Missing issue security level mapping for source id=" + sourceId
            );
        }

        return mappedId;
    }

    private void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(
                    DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    fieldName + " is required"
            );
        }
    }
}