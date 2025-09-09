/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.port.store.IModulePort;
import serp.project.account.infrastructure.store.mapper.ModuleMapper;
import serp.project.account.infrastructure.store.model.ModuleModel;
import serp.project.account.infrastructure.store.repository.IModuleRepository;

@Component
@RequiredArgsConstructor
public class ModuleAdapter implements IModulePort {
    private final IModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;
    
    @Override
    public ModuleEntity save(ModuleEntity module) {
        ModuleModel moduleModel = moduleMapper.toModel(module);
        return moduleMapper.toEntity(moduleRepository.save(moduleModel));
    }

    @Override
    public ModuleEntity getModuleById(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .map(moduleMapper::toEntity)
                .orElse(null);
    }

    @Override
    public List<ModuleEntity> getAllModules() {
        return moduleMapper.toEntityList(moduleRepository.findAll());
    }

    @Override
    public boolean existsByName(String name) {
        return moduleRepository.existsByModuleName(name);
    }
}
