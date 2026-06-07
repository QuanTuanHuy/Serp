/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.constant.Constants;
import serp.project.account.core.domain.dto.GeneralResponse;
import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
import serp.project.account.core.domain.dto.request.UpdateOrganizationStatusRequest;
import serp.project.account.core.domain.dto.response.OrganizationSettingsResponse;
import serp.project.account.core.domain.dto.response.OrganizationSettingsSummaryResponse;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.usecase.organization.command.OrganizationStatusCommandService;
import serp.project.account.kernel.utils.PaginationUtils;
import serp.project.account.kernel.utils.ResponseUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationUseCase {
    private final IOrganizationService organizationService;
    private final OrganizationStatusCommandService organizationStatusCommandService;
    private final ResponseUtils responseUtils;
    private final PaginationUtils paginationUtils;

    public GeneralResponse<?> getOrganizations(GetOrganizationParams params) {
        try {
            var pairOrganizations = organizationService.getOrganizations(params);
            return responseUtils.success(paginationUtils.getResponse(
                    pairOrganizations.getSecond(),
                    params.getPage(),
                    params.getPageSize(),
                    pairOrganizations.getFirst()));
        } catch (Exception e) {
            log.error("Error getting organizations: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getOrganizationById(Long organizationId) {
        try {
            var organization = organizationService.getOrganizationById(organizationId);
            if (organization == null) {
                return responseUtils.notFound(Constants.ErrorMessage.ORGANIZATION_NOT_FOUND);
            }
            return responseUtils.success(organization);
        } catch (Exception e) {
            log.error("Error getting organization by id: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> getOrganizationSettings(Long organizationId) {
        try {
            var organization = organizationService.getOrganizationById(organizationId);
            return responseUtils.success(toSettingsResponse(organization));
        } catch (Exception e) {
            log.error("Error getting organization settings: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> updateOrganizationSettings(Long organizationId, UpdateOrganizationSettingsRequest request) {
        try {
            var organization = organizationService.updateOrganizationSettings(organizationId, request);
            return responseUtils.success(toSettingsResponse(organization));
        } catch (Exception e) {
            log.error("Error updating organization settings: {}", e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    public GeneralResponse<?> updateOrganizationStatus(
            Long organizationId,
            Long updatedBy,
            UpdateOrganizationStatusRequest request) {
        try {
            var result = organizationStatusCommandService.updateOrganizationStatus(organizationId, updatedBy, request);
            return responseUtils.success(result);
        } catch (Exception e) {
            log.error("Error updating organization status {}: {}", organizationId, e.getMessage());
            return responseUtils.internalServerError(e.getMessage());
        }
    }

    private OrganizationSettingsResponse toSettingsResponse(OrganizationEntity organization) {
        return OrganizationSettingsResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .code(organization.getCode())
                .email(organization.getEmail())
                .phoneNumber(organization.getPhoneNumber())
                .website(organization.getWebsite())
                .address(organization.getAddress())
                .city(organization.getCity())
                .state(organization.getState())
                .country(organization.getCountry())
                .zipCode(organization.getZipCode())
                .taxId(organization.getTaxId())
                .industry(organization.getIndustry())
                .employeeCount(organization.getEmployeeCount())
                .description(organization.getDescription())
                .logoUrl(organization.getLogoUrl())
                .faviconUrl(organization.getFaviconUrl())
                .primaryColor(organization.getPrimaryColor())
                .secondaryColor(organization.getSecondaryColor())
                .timezone(organization.getTimezone())
                .dateFormat(organization.getDateFormat())
                .timeFormat(organization.getTimeFormat())
                .weekStartsOn(organization.getWeekStartsOn())
                .currency(organization.getCurrency())
                .language(organization.getLanguage())
                .createdAt(organization.getCreatedAt())
                .updatedAt(organization.getUpdatedAt())
                .summary(OrganizationSettingsSummaryResponse.builder().build())
                .build();
    }

}
