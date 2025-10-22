/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateOrganizationDto;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IOrganizationPort;
import serp.project.account.core.port.store.IUserOrganizationPort;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.infrastructure.store.mapper.OrganizationMapper;
import serp.project.account.infrastructure.store.mapper.UserOrganizationMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService implements IOrganizationService {
    private final IOrganizationPort organizationPort;
    private final IUserOrganizationPort userOrganizationPort;

    private final OrganizationMapper organizationMapper;
    private final UserOrganizationMapper userOrganizationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationEntity createOrganization(CreateOrganizationDto request) {
        var existed = organizationPort.getOrganizationByName(request.getName());
        if (existed != null) {
            log.error("Organization with name {} already exists", request.getName());
            throw new AppException(Constants.ErrorMessage.ORGANIZATION_ALREADY_EXISTS);
        }
        var organization = organizationMapper.createOrganizationMapper(request);
        return organizationPort.save(organization);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignOrganizationToUser(Long organizationId, Long userId, Long roleId, Boolean isDefault) {
        var existed = userOrganizationPort.getByUserIdAndOrganizationIdAndRoleId(userId, organizationId, roleId);
        if (existed != null) {
            log.warn("User with id {} already assigned to organization with id {} and role id {}", userId,
                    organizationId, roleId);
            return;
        }
        var userOrganization = userOrganizationMapper.assignUserOrganizationMapper(userId, organizationId, roleId,
                isDefault);
        userOrganizationPort.save(userOrganization);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationEntity createOrganization(Long ownerId, CreateOrganizationDto request) {
        try {
            var existed = organizationPort.getOrganizationByName(request.getName());
            if (existed != null) {
                log.error("Organization with name {} already exists", request.getName());
                throw new AppException(Constants.ErrorMessage.ORGANIZATION_ALREADY_EXISTS);
            }
            var organization = organizationMapper.createOrganizationMapper(request);
            organization.setOwnerId(ownerId);
            return organizationPort.save(organization);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating organization: {}", e.getMessage());
            throw new AppException(Constants.ErrorMessage.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public OrganizationEntity getOrganizationById(Long organizationId) {
        var organization = organizationPort.getById(organizationId);
        if (organization == null) {
            throw new AppException(Constants.ErrorMessage.ORGANIZATION_NOT_FOUND);
        }
        return organization;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrganizationEntity updateSubscription(Long organizationId,
            OrganizationSubscriptionEntity subscription) {
        var organization = getOrganizationById(organizationId);
        organization.setSubscriptionId(subscription.getId());
        return organizationPort.save(organization);
    }

    @Override
    public List<OrganizationEntity> getOrganizationsByIds(List<Long> organizationIds) {
        if (CollectionUtils.isEmpty(organizationIds)) {
            return Collections.emptyList();
        }
        return organizationPort.getByIds(organizationIds);
    }

}
