/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import java.util.List;

import org.springframework.data.util.Pair;

import serp.project.account.core.domain.dto.request.CreateOrganizationDto;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;

public interface IOrganizationService {
    OrganizationEntity createOrganization(CreateOrganizationDto request);

    OrganizationEntity createOrganization(Long ownerId, CreateOrganizationDto request);

    OrganizationEntity updateSubscription(Long organizationId, OrganizationSubscriptionEntity subscription);

    OrganizationEntity getOrganizationById(Long organizationId);

    void assignOrganizationToUser(Long organizationId, Long userId, Long roleId, Boolean isDefault);

    List<OrganizationEntity> getOrganizationsByIds(List<Long> organizationIds);

    Pair<List<OrganizationEntity>, Long> getOrganizations(GetOrganizationParams params);
}
