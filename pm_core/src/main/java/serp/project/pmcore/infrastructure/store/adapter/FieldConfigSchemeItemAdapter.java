/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeItemEntity;
import serp.project.pmcore.domain.fieldconfig.port.IFieldConfigSchemeItemPort;
import serp.project.pmcore.infrastructure.store.mapper.FieldConfigSchemeItemMapper;
import serp.project.pmcore.infrastructure.store.repository.IFieldConfigSchemeItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FieldConfigSchemeItemAdapter implements IFieldConfigSchemeItemPort {

    private final IFieldConfigSchemeItemRepository fieldConfigSchemeItemRepository;
    private final FieldConfigSchemeItemMapper fieldConfigSchemeItemMapper;

    @Override
    public List<FieldConfigSchemeItemEntity> createFieldConfigSchemeItems(List<FieldConfigSchemeItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return fieldConfigSchemeItemMapper.toEntities(
                fieldConfigSchemeItemRepository.saveAll(fieldConfigSchemeItemMapper.toModels(items))
        );
    }

    @Override
    public List<FieldConfigSchemeItemEntity> getFieldConfigSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId) {
        return fieldConfigSchemeItemMapper.toEntities(
                fieldConfigSchemeItemRepository.findAllBySchemeIdAndTenantIdOrSystemTenant(schemeId, tenantId)
        );
    }

    @Override
    public List<FieldConfigSchemeItemEntity> getFieldConfigSchemeItemsBySchemeId(Long schemeId, Long tenantId) {
        return fieldConfigSchemeItemMapper.toEntities(
                fieldConfigSchemeItemRepository.findAllBySchemeIdAndTenantId(schemeId, tenantId)
        );
    }

    @Override
    public Optional<FieldConfigSchemeItemEntity> getItemBySchemeIdAndIssueTypeId(Long schemeId, Long issueTypeId, Long tenantId) {
        return fieldConfigSchemeItemRepository.findFirstBySchemeIdAndIssueTypeIdAndTenantId(
                schemeId, issueTypeId, tenantId
        ).map(fieldConfigSchemeItemMapper::toEntity);
    }
}
