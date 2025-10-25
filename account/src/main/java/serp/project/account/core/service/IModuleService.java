/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import java.util.List;

import serp.project.account.core.domain.dto.request.CreateModuleDto;
import serp.project.account.core.domain.dto.request.UpdateModuleDto;
import serp.project.account.core.domain.entity.ModuleEntity;

public interface IModuleService {
    ModuleEntity createModule(CreateModuleDto request);

    ModuleEntity updateModule(Long moduleId, UpdateModuleDto request);

    ModuleEntity getModuleById(Long moduleId);

    ModuleEntity getModuleByIdFromCache(Long moduleId);

    ModuleEntity getModuleByCode(String code);

    List<ModuleEntity> getAllModules();

    List<ModuleEntity> getModulesByIds(List<Long> moduleIds);

    void seedPredefinedModules();
}
