/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.fieldconfig.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeItemEntity;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemeItemPort;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemePort;
import serp.project.pmcore.domain.fieldconfig.service.IFieldConfigService;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainValidationException;
import serp.project.pmcore.domain.shared.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FieldConfigService implements IFieldConfigService {

    private final IFieldConfigSchemePort fieldConfigSchemePort;
    private final IFieldConfigSchemeItemPort fieldConfigSchemeItemPort;

    @Override
    public Long resolveFieldConfigId(Long fieldConfigSchemeId, Long issueTypeId, Long tenantId) {
        if (fieldConfigSchemeId == null) {
            log.error("[FieldConfigService] Field config scheme id is null");
            throw new ResourceNotFoundException(
                    DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND
            );
        }

        FieldConfigSchemeEntity fieldConfigScheme = fieldConfigSchemePort
                .getFieldConfigSchemeById(fieldConfigSchemeId, tenantId)
                .orElseThrow(() -> {
                    log.error("[FieldConfigService] Field config scheme not found: id={}, tenantId={}", fieldConfigSchemeId, tenantId);
                    return new ResourceNotFoundException(
                            DomainErrorCode.FIELD_CONFIG_SCHEME_NOT_FOUND,
                            "Field configuration scheme not found: id=" + fieldConfigSchemeId
                    );
                });

        Long fieldConfigId = fieldConfigSchemeItemPort
                .getItemBySchemeIdAndIssueTypeId(fieldConfigSchemeId, issueTypeId, tenantId)
                .map(FieldConfigSchemeItemEntity::getFieldConfigId)
                .orElse(fieldConfigScheme.getDefaultFieldConfigId());
        if (fieldConfigId == null) {
            log.error("[FieldConfigService] Field config item not found: fieldConfigSchemeId={}, issueTypeId={}, tenantId={}",
                    fieldConfigSchemeId, issueTypeId, tenantId);
            throw new DomainValidationException(
                    DomainErrorCode.FIELD_CONFIG_SCHEME_COVERAGE_MISSING,
                    "Field configuration scheme does not cover issueTypeId=" + issueTypeId + " for fieldConfigSchemeId=" + fieldConfigSchemeId
            );
        }
        return fieldConfigId;
    }
}
