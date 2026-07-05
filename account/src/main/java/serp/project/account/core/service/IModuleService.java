/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import serp.project.account.core.domain.dto.request.CreateModuleDto;
import serp.project.account.core.domain.dto.request.UpdateModuleDto;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.ModuleType;

public interface IModuleService {
    ModuleEntity createModule(CreateModuleDto request);

    ModuleEntity updateModule(Long moduleId, UpdateModuleDto request);

    ModuleEntity getModuleById(Long moduleId);

    ModuleEntity getModuleByIdFromCache(Long moduleId);

    ModuleEntity getModuleByCode(String code);

    List<ModuleEntity> getAllModules();

    List<ModuleEntity> getModulesByIds(List<Long> moduleIds);

    void seedPredefinedModules();

    Long countModules();

    Long countAvailableModules();

    Pair<List<ModuleEntity>, Long> getModulesPaginated(
            String search,
            ModuleStatus status,
            ModuleType moduleType,
            Pageable pageable);
}
