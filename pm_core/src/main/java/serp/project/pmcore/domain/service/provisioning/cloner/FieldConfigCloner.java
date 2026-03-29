package serp.project.pmcore.domain.service.provisioning.cloner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigEntity;
import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigItemPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigPort;
import serp.project.pmcore.domain.shared.enums.CloneMode;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FieldConfigCloner {

    private final IFieldConfigPort fieldConfigPort;
    private final IFieldConfigItemPort fieldConfigItemPort;

    public Long cloneFieldConfigBySourceId(Long sourceFieldConfigId,
                                           Long tenantId,
                                           Long userId,
                                           CloneMode cloneMode) {
        validateRequired(sourceFieldConfigId, "sourceFieldConfigId");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        FieldConfigEntity source = fieldConfigPort
                .getFieldConfigByIdIncludingSystem(sourceFieldConfigId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Field configuration not found for source id=" + sourceFieldConfigId
                ));

        return cloneFieldConfig(source, tenantId, userId, cloneMode);
    }

    public Long cloneFieldConfig(FieldConfigEntity source,
                                 Long tenantId,
                                 Long userId,
                                 CloneMode cloneMode) {
        validateRequired(source, "source");
        validateRequired(tenantId, "tenantId");
        validateRequired(userId, "userId");

        List<FieldConfigItemEntity> sourceItems = fieldConfigItemPort
                .getFieldConfigItemsByFieldConfigIdIncludingSystem(source.getId(), tenantId);

        long now = System.currentTimeMillis();

        FieldConfigEntity cloned = FieldConfigEntity.builder()
                .tenantId(tenantId)
                .name(source.getName())
                .description(source.getDescription())
                .isSystem(false)
                .build();
        cloned.applyCreate(userId, now);
        FieldConfigEntity saved = fieldConfigPort.createFieldConfig(cloned);

        if (!sourceItems.isEmpty()) {
            List<FieldConfigItemEntity> clonedItems = new ArrayList<>();
            for (FieldConfigItemEntity item : sourceItems) {
                clonedItems.add(FieldConfigItemEntity.builder()
                        .tenantId(tenantId)
                        .fieldConfigId(saved.getId())
                        .fieldRefType(item.getFieldRefType())
                        .fieldRef(item.getFieldRef())
                        .isRequired(item.getIsRequired())
                        .isHidden(item.getIsHidden())
                        .rendererKey(item.getRendererKey())
                        .sequence(item.getSequence())
                        .createdAt(now)
                        .createdBy(userId)
                        .build());
            }

            fieldConfigItemPort.createFieldConfigItems(clonedItems);
        }

        log.info("Created {} FIELD_CONFIG clone: source={} -> cloned={} (tenantId={})",
                cloneMode, source.getId(), saved.getId(), tenantId);

        return saved.getId();
    }

    private void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(
                    DomainErrorCode.CLONE_FIELD_CONFIG_FAILED,
                    fieldName + " is required"
            );
        }
    }
}