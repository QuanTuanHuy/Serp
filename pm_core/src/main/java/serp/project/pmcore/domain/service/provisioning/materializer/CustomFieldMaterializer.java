/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service.provisioning.materializer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.constant.TenantConstants;
import serp.project.pmcore.domain.entity.CustomFieldEntity;
import serp.project.pmcore.domain.exception.DomainErrorCode;
import serp.project.pmcore.domain.exception.ResourceNotFoundException;
import serp.project.pmcore.domain.port.store.ICustomFieldPort;

import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomFieldMaterializer {

    private final ICustomFieldPort customFieldPort;

    public Long materialize(Long sourceCustomFieldId, Long tenantId, Long userId) {
        validateArguments(sourceCustomFieldId, tenantId, userId);

        CustomFieldEntity source = customFieldPort.getCustomFieldByIdIncludingSystem(sourceCustomFieldId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.CUSTOM_FIELD_NOT_FOUND,
                        "Custom field not found: id=" + sourceCustomFieldId
                ));

        if (tenantId.equals(source.getTenantId())) {
            return source.getId();
        }

        if (!TenantConstants.SYSTEM_TENANT_ID.equals(source.getTenantId())) {
            throw new ResourceNotFoundException(
                    DomainErrorCode.CUSTOM_FIELD_NOT_FOUND,
                    "Custom field source is not tenant/system scoped: id=" + sourceCustomFieldId
            );
        }

        Optional<CustomFieldEntity> existing = customFieldPort.getCustomFieldByFieldKey(tenantId, source.getFieldKey());
        if (existing.isPresent()) {
            return existing.get().getId();
        }

        long now = System.currentTimeMillis();
        CustomFieldEntity clone = CustomFieldEntity.builder()
                .tenantId(tenantId)
                .fieldKey(source.getFieldKey())
                .name(source.getName())
                .description(source.getDescription())
                .typeKey(source.getTypeKey())
                .searchTemplate(source.getSearchTemplate())
                .isSystem(false)
                .schemaJson(source.getSchemaJson())
                .build();
        clone.applyCreate(userId, now);

        CustomFieldEntity saved = customFieldPort.createCustomField(clone);
        log.info("Materialized shared CUSTOM_FIELD source={} -> tenant={} for tenantId={}",
                sourceCustomFieldId, saved.getId(), tenantId);
        return saved.getId();
    }

    private void validateArguments(Long sourceCustomFieldId, Long tenantId, Long userId) {
        Objects.requireNonNull(sourceCustomFieldId, "Source custom field ID must not be null");
        Objects.requireNonNull(tenantId, "Tenant ID must not be null");
        Objects.requireNonNull(userId, "User ID must not be null");
    }
}
