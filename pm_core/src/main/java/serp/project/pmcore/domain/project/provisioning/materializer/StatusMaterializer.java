package serp.project.pmcore.domain.project.provisioning.materializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.shared.constant.TenantConstants;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.workitem.entity.StatusCategoryEntity;
import serp.project.pmcore.domain.workitem.entity.StatusEntity;
import serp.project.pmcore.domain.workitem.port.IStatusCategoryPort;
import serp.project.pmcore.domain.workitem.port.IStatusPort;

import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatusMaterializer {

    private final IStatusPort statusPort;
    private final IStatusCategoryPort statusCategoryPort;

    public Long materialize(Long sourceId, Long tenantId, Long userId) {
        Objects.requireNonNull(sourceId, "sourceId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");

        StatusEntity source = statusPort.getStatusByIdIncludingSystem(sourceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.STATUS_NOT_FOUND,
                        "Status not found: sourceId=" + sourceId
                ));
        if (source.getTenantId().equals(tenantId)) {
            return source.getId();
        }
        if (!TenantConstants.SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.STATUS_NOT_FOUND,
                    "Status source is not tenant/system scoped: " + sourceId
            );
        }

        Optional<StatusEntity> existing = statusPort.getStatusByStatusKey(tenantId, source.getStatusKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        long now = System.currentTimeMillis();
        StatusEntity cloned = StatusEntity.builder()
                .tenantId(tenantId)
                .statusKey(source.getStatusKey())
                .name(source.getName())
                .description(source.getDescription())
                .iconUrl(source.getIconUrl())
                .categoryId(materializeStatusCategory(source.getCategoryId(), tenantId, userId))
                .isSystem(false)
                .build();
        cloned.applyCreate(userId, now);
        StatusEntity saved = statusPort.createStatus(cloned);

        log.info("Materialized shared STATUS source={} -> tenant={} for tenantId={}",
                sourceId, saved.getId(), tenantId);

        return saved.getId();
    }

    public Long materializeStatusCategory(Long sourceId, Long tenantId, Long userId) {
        if (sourceId == null) {
            return null;
        }
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");

        StatusCategoryEntity source = statusCategoryPort
                .getStatusCategoryByIdIncludingSystem(sourceId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.STATUS_CATEGORY_NOT_FOUND,
                        "Status category not found: sourceId=" + sourceId
                ));
        if (source.getTenantId().equals(tenantId)) {
            return source.getId();
        }
        if (!source.getTenantId().equals(TenantConstants.SYSTEM_TENANT_ID)) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.STATUS_CATEGORY_NOT_FOUND,
                    "Status category source is not tenant/system scoped: " + sourceId
            );
        }

        Optional<StatusCategoryEntity> existing = statusCategoryPort.getStatusCategoryByKey(tenantId, source.getKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }
        long now = System.currentTimeMillis();
        StatusCategoryEntity cloned = StatusCategoryEntity.builder()
                .tenantId(tenantId)
                .key(source.getKey())
                .name(source.getName())
                .color(source.getColor())
                .isSystem(false)
                .build();
        cloned.applyCreate(userId, now);
        StatusCategoryEntity saved = statusCategoryPort.createStatusCategory(cloned);
        log.info("Materialized shared STATUS_CATEGORY source={} -> tenant={} for tenantId={}",
                sourceId, saved.getId(), tenantId);
        return saved.getId();
    }

}
