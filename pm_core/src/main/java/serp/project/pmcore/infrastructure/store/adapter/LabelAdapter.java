/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.core.domain.entity.LabelEntity;
import serp.project.pmcore.core.port.store.ILabelPort;
import serp.project.pmcore.infrastructure.store.mapper.LabelMapper;
import serp.project.pmcore.infrastructure.store.repository.ILabelRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LabelAdapter implements ILabelPort {

    private final ILabelRepository labelRepository;
    private final LabelMapper labelMapper;

    @Override
    public LabelEntity createLabel(LabelEntity label) {
        return labelMapper.toEntity(labelRepository.save(labelMapper.toModel(label)));
    }

    @Override
    public Optional<LabelEntity> getLabelById(Long id, Long tenantId) {
        return labelRepository.findByIdAndTenantId(id, tenantId)
                .map(labelMapper::toEntity);
    }

    @Override
    public List<LabelEntity> getLabelsByProjectId(Long projectId, Long tenantId) {
        return labelMapper.toEntities(labelRepository.findAllByTenantIdAndProjectId(tenantId, projectId));
    }

    @Override
    public void updateLabel(LabelEntity label) {
        labelRepository.save(labelMapper.toModel(label));
    }

    @Override
    public void deleteLabel(Long labelId, Long tenantId) {
        labelRepository.deleteByIdAndTenantId(labelId, tenantId);
    }
}
