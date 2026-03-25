/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.FieldConfigEntity;
import serp.project.pmcore.domain.port.store.IFieldConfigPort;
import serp.project.pmcore.infrastructure.store.mapper.FieldConfigMapper;
import serp.project.pmcore.infrastructure.store.repository.IFieldConfigRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FieldConfigAdapter implements IFieldConfigPort {

    private final IFieldConfigRepository fieldConfigRepository;
    private final FieldConfigMapper fieldConfigMapper;

    @Override
    public FieldConfigEntity createFieldConfig(FieldConfigEntity fieldConfig) {
        return fieldConfigMapper.toEntity(fieldConfigRepository.save(fieldConfigMapper.toModel(fieldConfig)));
    }

    @Override
    public Optional<FieldConfigEntity> getFieldConfigById(Long fieldConfigId, Long tenantId) {
        return fieldConfigRepository.findByIdAndTenantId(fieldConfigId, tenantId)
                .map(fieldConfigMapper::toEntity);
    }

    @Override
    public Optional<FieldConfigEntity> getFieldConfigByIdIncludingSystem(Long fieldConfigId, Long tenantId) {
        return fieldConfigRepository.findByIdAndTenantIdOrSystemTenant(fieldConfigId, tenantId)
                .map(fieldConfigMapper::toEntity);
    }
}
