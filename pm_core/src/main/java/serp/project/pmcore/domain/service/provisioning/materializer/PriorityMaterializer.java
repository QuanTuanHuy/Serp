package serp.project.pmcore.domain.service.provisioning.materializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.constant.TenantConstants;
import serp.project.pmcore.domain.entity.PriorityEntity;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.IPriorityPort;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriorityMaterializer {

    private final IPriorityPort priorityPort;

    public Long materialize(Long sourceId, Long tenantId, Long userId) {
        validateArguments(sourceId, tenantId, userId);

        PriorityEntity source = priorityPort
                .getPriorityByIdIncludingSystem(sourceId, tenantId)
                .orElseThrow(() -> ResourceNotFoundException.priority(sourceId));
        if (source.getTenantId().equals(tenantId)) {
            return source.getId();
        }
        if (!TenantConstants.SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.PRIORITY_NOT_FOUND,
                    "Priority source is not tenant/system scoped: " + sourceId
            );
        }

        Optional<PriorityEntity> existing = priorityPort.getPriorityByPriorityKey(tenantId, source.getPriorityKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        long now = System.currentTimeMillis();
        PriorityEntity cloned = PriorityEntity.builder()
                .tenantId(tenantId)
                .priorityKey(source.getPriorityKey())
                .name(source.getName())
                .description(source.getDescription())
                .iconUrl(source.getIconUrl())
                .color(source.getColor())
                .sequence(source.getSequence())
                .isSystem(false)
                .build();
        cloned.applyCreate(userId, now);
        PriorityEntity saved = priorityPort.createPriority(cloned);
        log.info("Materialized shared PRIORITY source={} -> tenant={} for tenantId={}",
                sourceId, saved.getId(), tenantId);
        return saved.getId();
    }

    private void validateArguments(Long sourceId, Long tenantId, Long userId) {
        Objects.requireNonNull(sourceId, "Source priority ID must not be null");
        Objects.requireNonNull(tenantId, "Tenant ID must not be null");
        Objects.requireNonNull(userId, "User ID must not be null");
    }
}
