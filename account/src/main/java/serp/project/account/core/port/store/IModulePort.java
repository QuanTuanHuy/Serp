package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.ModuleEntity;

import java.util.List;

public interface IModulePort {
    ModuleEntity save(ModuleEntity module);

    ModuleEntity getModuleById(Long moduleId);

    ModuleEntity getModuleByCode(String code);

    List<ModuleEntity> getAllModules();

    List<ModuleEntity> getModulesByIds(List<Long> moduleIds);

    boolean existsByName(String name);

    boolean existsByCode(String code);
}
