/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateModuleDto;
import serp.project.account.core.domain.dto.request.UpdateModuleDto;
import serp.project.account.core.domain.entity.ModuleEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IModulePort;
import serp.project.account.core.service.IModuleService;
import serp.project.account.infrastructure.store.mapper.ModuleMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModuleService implements IModuleService {

    private final IModulePort modulePort;
    private final ModuleMapper moduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModuleEntity createModule(CreateModuleDto request) {
        try {
            boolean existedModule = modulePort.existsByName(request.getName());
            if (existedModule) {
                throw new AppException(Constants.ErrorMessage.MODULE_ALREADY_EXISTS);
            }

            var module = moduleMapper.createModuleMapper(request);
            return modulePort.save(module);

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
            return modulePort.save(module);
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
    public List<ModuleEntity> getAllModules() {
        return modulePort.getAllModules();
    }
}
