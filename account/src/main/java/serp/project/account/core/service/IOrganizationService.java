/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service;

import serp.project.account.core.domain.dto.request.CreateOrganizationDto;
import serp.project.account.core.domain.entity.OrganizationEntity;

public interface IOrganizationService {
    OrganizationEntity createOrganization(CreateOrganizationDto request);

    OrganizationEntity createOrganization(Long ownerId, CreateOrganizationDto request);

    void assignOrganizationToUser(Long organizationId, Long userId, Long roleId);
}
