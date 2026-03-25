package serp.project.pmcore.domain.service.provisioning.materializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.constant.TenantConstants;
import serp.project.pmcore.domain.entity.workitem.IssueTypeEntity;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IIssueTypePort;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class IssueTypeMaterializer {

    private final IIssueTypePort issueTypePort;

    public Long materialize(Long sourceIssueTypeId, Long tenantId, Long userId) {
        validateArguments(sourceIssueTypeId, tenantId, userId);

        IssueTypeEntity source = issueTypePort
                .getIssueTypeByIdIncludingSystem(sourceIssueTypeId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.issueType(sourceIssueTypeId));
        if (source.getTenantId().equals(tenantId)) {
            return source.getId();
        }
        if (!TenantConstants.SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.ISSUE_TYPE_NOT_FOUND,
                    "Issue type source is not tenant/system scoped: " + sourceIssueTypeId
            );
        }

        Optional<IssueTypeEntity> existing = issueTypePort.getIssueTypeByTypeKey(tenantId, source.getTypeKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        long now = System.currentTimeMillis();
        IssueTypeEntity clone = IssueTypeEntity.builder()
                .tenantId(tenantId)
                .typeKey(source.getTypeKey())
                .name(source.getName())
                .description(source.getDescription())
                .iconUrl(source.getIconUrl())
                .hierarchyLevel(source.getHierarchyLevel())
                .isSystem(false)
                .build();
        clone.applyCreate(userId, now);
        IssueTypeEntity saved = issueTypePort.createIssueType(clone);
        log.info("Materialized shared ISSUE_TYPE source={} -> tenant={} for tenantId={}",
                sourceIssueTypeId, saved.getId(), tenantId);
        return saved.getId();
    }

    private void validateArguments(Long sourceIssueTypeId, Long tenantId, Long userId) {
        Objects.requireNonNull(sourceIssueTypeId, "Source issue type ID must not be null");
        Objects.requireNonNull(tenantId, "Tenant ID must not be null");
        Objects.requireNonNull(userId, "User ID must not be null");
    }
}
