/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.enums.ModuleType;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.enums.ModuleStatus;
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
    public ModuleEntity getModuleByCode(String code) {
        return moduleRepository.findByCode(code)
                .map(moduleMapper::toEntity)
                .orElse(null);
    }

    @Override
    public List<ModuleEntity> getAllModules() {
        return moduleMapper.toEntityList(moduleRepository.findAll());
    }

    @Override
    public List<ModuleEntity> getModulesByIds(List<Long> moduleIds) {
        return moduleMapper.toEntityList(moduleRepository.findAllById(moduleIds));
    }

    @Override
    public boolean existsByName(String name) {
        return moduleRepository.existsByModuleName(name);
    }

    @Override
    public boolean existsByCode(String code) {
        return moduleRepository.existsByCode(code);
    }

    @Override
    public Long countModules() {
        return moduleRepository.count();
    }

    @Override
    public Long countModulesByStatus(ModuleStatus status) {
        return moduleRepository.countByStatus(status);
    }

    @Override
    public Pair<List<ModuleEntity>, Long> searchModules(String search, int limit) {
        var pageable = PageRequest.of(0, limit);
        var modules = moduleMapper.toEntityList(moduleRepository.searchModules(search, pageable));
        Long total = moduleRepository.countSearchModules(search);
        return Pair.of(modules, total != null ? total : 0L);
    }

    @Override
    public Pair<List<ModuleEntity>, Long> getModulesPaginated(
            String search,
            ModuleStatus status,
            ModuleType moduleType,
            Pageable pageable) {
        var page = moduleRepository.findAllPaginated(search, status, moduleType, pageable);
        return Pair.of(moduleMapper.toEntityList(page.getContent()), page.getTotalElements());
    }
}
