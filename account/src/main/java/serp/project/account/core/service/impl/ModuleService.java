/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.CacheConstants;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateModuleDto;
import serp.project.account.core.domain.dto.request.UpdateModuleDto;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.domain.enums.ModuleEnum;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.client.ICachePort;
import serp.project.account.core.port.store.IModulePort;
import serp.project.account.core.service.IModuleService;
import serp.project.account.infrastructure.store.mapper.ModuleMapper;
import serp.project.account.kernel.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleService implements IModuleService {

    private final IModulePort modulePort;
    private final ModuleMapper moduleMapper;

    private final ICachePort cachePort;
    private final AsyncTaskExecutor asyncTaskExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModuleEntity createModule(CreateModuleDto request) {
        try {
            boolean existedModule = modulePort.existsByName(request.getName());
            if (existedModule) {
                throw new AppException(Constants.ErrorMessage.MODULE_ALREADY_EXISTS);
            }

            var module = moduleMapper.createModuleMapper(request);
            module = modulePort.save(module);

            clearCacheAllModules();

            return module;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModuleEntity updateModule(Long moduleId, UpdateModuleDto request) {
        try {
            var module = modulePort.getModuleById(moduleId);
            if (module == null) {
                throw new AppException(Constants.ErrorMessage.MODULE_NOT_FOUND);
            }
            module = moduleMapper.updateModuleMapper(module, request);
            module = modulePort.save(module);

            clearCacheAllModules();

            return module;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ModuleEntity getModuleById(Long moduleId) {
        return modulePort.getModuleById(moduleId);
    }

    @Override
    public ModuleEntity getModuleByIdFromCache(Long moduleId) {
        return getAllModules().stream()
                .filter(m -> m.getId().equals(moduleId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ModuleEntity getModuleByCode(String code) {
        return getAllModules().stream()
                .filter(m -> m.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ModuleEntity> getAllModules() {
        List<ModuleEntity> cachedModules = cachePort.getFromCache(
                CacheConstants.ALL_MODULES,
                new ParameterizedTypeReference<>() {
                });
        if (!CollectionUtils.isEmpty(cachedModules)) {
            return cachedModules;
        }

        var modules = modulePort.getAllModules();
        cacheAllModules(modules);

        return modules;
    }

    @Override
    public List<ModuleEntity> getModulesByIds(List<Long> moduleIds) {
        if (moduleIds == null || moduleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return getAllModules().stream()
                .filter(m -> moduleIds.contains(m.getId()))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void seedPredefinedModules() {
        try {
            log.info("Starting to seed predefined modules from ModuleEnum");

            for (ModuleEnum moduleEnum : ModuleEnum.values()) {

                if (modulePort.existsByCode(moduleEnum.getCode())) {
                    log.debug("Module {} already exists, skipping", moduleEnum.getCode());
                    continue;
                }

                List<Long> dependsOnModuleIds = resolveDependencies(moduleEnum);

                ModuleEntity module = moduleMapper.buildFromModuleEnum(moduleEnum, dependsOnModuleIds);

                modulePort.save(module);
                log.info("Successfully seeded module: {} - {}", moduleEnum.getCode(), moduleEnum.getModuleName());
            }

            clearCacheAllModules();
            log.info("Completed seeding predefined modules");
        } catch (Exception e) {
            log.error("Error seeding predefined modules: {}", e.getMessage(), e);
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Resolve module dependencies - convert codes to IDs
     */
    private List<Long> resolveDependencies(ModuleEnum moduleEnum) {
        List<Long> dependsOnModuleIds = new ArrayList<>();

        for (String dependencyCode : moduleEnum.getDependsOnModuleCodes()) {
            ModuleEntity dependencyModule = modulePort.getModuleByCode(dependencyCode);
            if (dependencyModule != null) {
                dependsOnModuleIds.add(dependencyModule.getId());
            } else {
                log.warn("Dependency module {} not found for module {}",
                        dependencyCode, moduleEnum.getCode());
            }
        }

        return dependsOnModuleIds;
    }

    private void cacheAllModules(List<ModuleEntity> modules) {
        asyncTaskExecutor.execute(
                () -> cachePort.setToCache(CacheConstants.ALL_MODULES, modules, CacheConstants.LONG_EXPIRATION));
    }

    private void clearCacheAllModules() {
        asyncTaskExecutor.execute(() -> cachePort.deleteFromCache(CacheConstants.ALL_MODULES));
    }
}
