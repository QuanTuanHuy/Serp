/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.entity.OrganizationModuleAccessSettingEntity;
import serp.project.account.core.port.store.IOrganizationModuleAccessSettingPort;

@ExtendWith(MockitoExtension.class)
class OrganizationModuleAccessSettingServiceTest {

    @Mock
    private IOrganizationModuleAccessSettingPort port;

    @InjectMocks
    private OrganizationModuleAccessSettingService service;

    @Test
    void upsertAutoGrantShouldCreateSettingWhenMissing() {
        when(port.findByOrganizationIdAndModuleId(10L, 20L)).thenReturn(Optional.empty());
        when(port.save(any(OrganizationModuleAccessSettingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.upsertAutoGrantToNewUsers(10L, 20L, true, 30L);

        assertTrue(result.isAutoGrantEnabled());
        assertEquals(10L, result.getOrganizationId());
        assertEquals(20L, result.getModuleId());
        assertEquals(30L, result.getCreatedBy());
        assertEquals(30L, result.getUpdatedBy());
        ArgumentCaptor<OrganizationModuleAccessSettingEntity> captor =
                ArgumentCaptor.forClass(OrganizationModuleAccessSettingEntity.class);
        verify(port).save(captor.capture());
        assertTrue(captor.getValue().isAutoGrantEnabled());
    }

    @Test
    void upsertAutoGrantShouldUpdateExistingSetting() {
        var existing = OrganizationModuleAccessSettingEntity.builder()
                .id(99L)
                .organizationId(10L)
                .moduleId(20L)
                .autoGrantToNewUsers(true)
                .createdBy(1L)
                .updatedBy(1L)
                .build();
        when(port.findByOrganizationIdAndModuleId(10L, 20L)).thenReturn(Optional.of(existing));
        when(port.save(any(OrganizationModuleAccessSettingEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.upsertAutoGrantToNewUsers(10L, 20L, false, 30L);

        assertFalse(result.isAutoGrantEnabled());
        assertEquals(1L, result.getCreatedBy());
        assertEquals(30L, result.getUpdatedBy());
    }

    @Test
    void isAutoGrantEnabledShouldReturnFalseWhenSettingIsMissing() {
        when(port.findByOrganizationIdAndModuleId(10L, 20L)).thenReturn(Optional.empty());

        assertFalse(service.isAutoGrantEnabled(10L, 20L));
    }

    @Test
    void getEnabledByOrganizationIdShouldDelegateToPort() {
        var enabled = OrganizationModuleAccessSettingEntity.builder()
                .organizationId(10L)
                .moduleId(20L)
                .autoGrantToNewUsers(true)
                .build();
        when(port.findEnabledByOrganizationId(10L)).thenReturn(List.of(enabled));

        var result = service.getEnabledByOrganizationId(10L);

        assertEquals(1, result.size());
        assertEquals(20L, result.getFirst().getModuleId());
    }
}
