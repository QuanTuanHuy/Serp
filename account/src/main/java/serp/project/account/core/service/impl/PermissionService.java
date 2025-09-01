/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreatePermissionDto;
import serp.project.account.core.domain.entity.PermissionEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IPermissionPort;
import serp.project.account.core.service.IPermissionService;
import serp.project.account.infrastructure.store.mapper.PermissionMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService implements IPermissionService {
    private final PermissionMapper permissionMapper;
    private final IPermissionPort permissionPort;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionEntity createPermission(CreatePermissionDto request) {
        var existedPer = permissionPort.getPermissionByName(request.getName());
        if (existedPer != null) {
            throw new AppException(Constants.ErrorMessage.PERMISSION_ALREADY_EXISTS);
        }

        var permission = permissionMapper.createPermissionMapper(request);
        return permissionPort.save(permission);
    }


    @Override
    public PermissionEntity getPermissionByName(String name) {
        return permissionPort.getPermissionByName(name);
    }

    @Override
    public List<PermissionEntity> getPermissionsByIds(List<Long> ids) {
        return permissionPort.getPermissionsByIds(ids);
    }

    @Override
    public List<PermissionEntity> getAllPermissions() {
        return permissionPort.getAllPermissions();
    }
}
