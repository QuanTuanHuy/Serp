package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.ModuleType;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

public interface IModulePort {
    ModuleEntity save(ModuleEntity module);

    ModuleEntity getModuleById(Long moduleId);

    ModuleEntity getModuleByCode(String code);

    List<ModuleEntity> getAllModules();

    List<ModuleEntity> getModulesByIds(List<Long> moduleIds);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    Long countModules();

    Long countModulesByStatus(ModuleStatus status);

    Pair<List<ModuleEntity>, Long> searchModules(String search, int limit);

    Pair<List<ModuleEntity>, Long> getModulesPaginated(
            String search,
            ModuleStatus status,
            ModuleType moduleType,
            Pageable pageable);
}
