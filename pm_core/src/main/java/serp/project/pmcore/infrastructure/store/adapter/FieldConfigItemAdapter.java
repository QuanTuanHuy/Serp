/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.FieldConfigItemEntity;
import serp.project.pmcore.domain.port.store.IFieldConfigItemPort;
import serp.project.pmcore.infrastructure.store.mapper.FieldConfigItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IFieldConfigItemRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FieldConfigItemAdapter implements IFieldConfigItemPort {

    private final IFieldConfigItemRepository fieldConfigItemRepository;
    private final FieldConfigItemMapper fieldConfigItemMapper;

    @Override
    public List<FieldConfigItemEntity> createFieldConfigItems(List<FieldConfigItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return fieldConfigItemMapper.toEntities(
                fieldConfigItemRepository.saveAll(fieldConfigItemMapper.toModels(items))
        );
    }

    @Override
    public List<FieldConfigItemEntity> getFieldConfigItemsByFieldConfigIdIncludingSystem(Long fieldConfigId, Long tenantId) {
        return fieldConfigItemMapper.toEntities(
                fieldConfigItemRepository.findAllByFieldConfigIdAndTenantIdOrSystemTenant(fieldConfigId, tenantId)
        );
    }

    @Override
    public List<FieldConfigItemEntity> getFieldConfigItemsByFieldConfigId(Long fieldConfigId, Long tenantId) {
        return fieldConfigItemMapper.toEntities(
                fieldConfigItemRepository.findAllByFieldConfigIdAndTenantId(fieldConfigId, tenantId)
        );
    }
}
