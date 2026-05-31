/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.dto.request.UpdateOrganizationSettingsRequest;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.port.store.IOrganizationPort;
import serp.project.account.core.port.store.IUserOrganizationPort;
import serp.project.account.infrastructure.store.mapper.OrganizationMapper;
import serp.project.account.infrastructure.store.mapper.UserOrganizationMapper;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @Mock
    private IOrganizationPort organizationPort;

    @Mock
    private IUserOrganizationPort userOrganizationPort;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private UserOrganizationMapper userOrganizationMapper;

    @InjectMocks
    private OrganizationService organizationService;

    private OrganizationEntity organization;

    @BeforeEach
    void setUp() {
        organization = OrganizationEntity.builder()
                .id(1L)
                .name("Old Name")
                .email("old@example.com")
                .city("Old City")
                .primaryColor("#111111")
                .build();
    }

    @Test
    void updateOrganizationSettingsShouldTrimValuesAndClearBlankOptionalFields() {
        var request = UpdateOrganizationSettingsRequest.builder()
                .name(" New Name ")
                .email(" ")
                .city(" Hanoi ")
                .primaryColor("#7c3aed")
                .weekStartsOn("monday")
                .build();

        when(organizationPort.getById(1L)).thenReturn(organization);
        when(organizationPort.save(any(OrganizationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = organizationService.updateOrganizationSettings(1L, request);

        assertEquals("New Name", updated.getName());
        assertNull(updated.getEmail());
        assertEquals("Hanoi", updated.getCity());
        assertEquals("#7c3aed", updated.getPrimaryColor());
        assertEquals("monday", updated.getWeekStartsOn());
        verify(organizationPort).save(organization);
    }
}
