/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeEntity;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemePort;
import serp.project.pmcore.infrastructure.store.mapper.FieldConfigSchemeMapper;
import serp.project.pmcore.infrastructure.store.repository.IFieldConfigSchemeRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FieldConfigSchemeAdapter implements IFieldConfigSchemePort {

    private final IFieldConfigSchemeRepository fieldConfigSchemeRepository;
    private final FieldConfigSchemeMapper fieldConfigSchemeMapper;

    @Override
    public FieldConfigSchemeEntity createFieldConfigScheme(FieldConfigSchemeEntity scheme) {
        return fieldConfigSchemeMapper.toEntity(fieldConfigSchemeRepository.save(fieldConfigSchemeMapper.toModel(scheme)));
    }

    @Override
    public Optional<FieldConfigSchemeEntity> getFieldConfigSchemeById(Long schemeId, Long tenantId) {
        return fieldConfigSchemeRepository.findByIdAndTenantId(schemeId, tenantId)
                .map(fieldConfigSchemeMapper::toEntity);
    }

    @Override
    public Optional<FieldConfigSchemeEntity> getFieldConfigSchemeByIdIncludingSystem(Long schemeId, Long tenantId) {
        return fieldConfigSchemeRepository.findByIdAndTenantIdOrSystemTenant(schemeId, tenantId)
                .map(fieldConfigSchemeMapper::toEntity);
    }
}
