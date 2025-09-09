package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.ModuleEntity;

import java.util.List;

public interface IModulePort {
    ModuleEntity save(ModuleEntity module);
    ModuleEntity getModuleById(Long moduleId);
    List<ModuleEntity> getAllModules();
    boolean existsByName(String name);
}
