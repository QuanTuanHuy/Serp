/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.request.CreateOrganizationDto;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
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
    public OrganizationEntity updateOrganizationSettings(Long organizationId, UpdateOrganizationSettingsRequest request) {
        var organization = getOrganizationById(organizationId);

        applySettingsUpdate(organization, request);
        return organizationPort.save(organization);
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

    @Override
    public Pair<List<OrganizationEntity>, Long> getOrganizations(GetOrganizationParams params) {
        return organizationPort.getOrganizations(params);
    }

    private void applySettingsUpdate(OrganizationEntity organization, UpdateOrganizationSettingsRequest request) {
        if (request.getName() != null) {
            organization.setName(normalizeRequired(request.getName()));
        }
        organization.setEmail(normalizeOptional(request.getEmail()));
        organization.setPhoneNumber(normalizeOptional(request.getPhoneNumber()));
        organization.setWebsite(normalizeOptional(request.getWebsite()));
        organization.setAddress(normalizeOptional(request.getAddress()));
        organization.setCity(normalizeOptional(request.getCity()));
        organization.setState(normalizeOptional(request.getState()));
        organization.setCountry(normalizeOptional(request.getCountry()));
        organization.setZipCode(normalizeOptional(request.getZipCode()));
        organization.setTaxId(normalizeOptional(request.getTaxId()));
        organization.setIndustry(normalizeOptional(request.getIndustry()));
        organization.setEmployeeCount(request.getEmployeeCount());
        organization.setDescription(normalizeOptional(request.getDescription()));
        organization.setLogoUrl(normalizeOptional(request.getLogoUrl()));
        organization.setFaviconUrl(normalizeOptional(request.getFaviconUrl()));
        organization.setPrimaryColor(normalizeOptional(request.getPrimaryColor()));
        organization.setSecondaryColor(normalizeOptional(request.getSecondaryColor()));
        organization.setTimezone(normalizeOptional(request.getTimezone()));
        organization.setDateFormat(normalizeOptional(request.getDateFormat()));
        organization.setTimeFormat(normalizeOptional(request.getTimeFormat()));
        organization.setWeekStartsOn(normalizeOptional(request.getWeekStartsOn()));
        organization.setCurrency(normalizeOptional(request.getCurrency()));
        organization.setLanguage(normalizeOptional(request.getLanguage()));
    }

    private String normalizeRequired(String value) {
        var normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new AppException(Constants.ErrorMessage.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
