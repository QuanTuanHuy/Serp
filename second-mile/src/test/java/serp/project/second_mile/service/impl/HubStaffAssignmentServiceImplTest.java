/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.second_mile.domain.Hub;
import serp.project.second_mile.domain.HubStaff;
import serp.project.second_mile.domain.HubStaffAssignment;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;
import serp.project.second_mile.exception.AppException;
import serp.project.second_mile.exception.ErrorCode;
import serp.project.second_mile.kernel.utils.SecondMileAccessUtils;
import serp.project.second_mile.repository.HubRepository;
import serp.project.second_mile.repository.HubStaffAssignmentRepository;
import serp.project.second_mile.repository.HubStaffRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HubStaffAssignmentServiceImplTest {
    private static final Long TENANT_ID = 1L;

    @Mock
    private HubRepository hubRepository;

    @Mock
    private HubStaffRepository hubStaffRepository;

    @Mock
    private HubStaffAssignmentRepository hubStaffAssignmentRepository;

    @Mock
    private SecondMileAccessUtils secondMileAccessUtils;

    @InjectMocks
    private HubStaffAssignmentServiceImpl hubStaffAssignmentService;

    @Test
    void assignStaffToHubShouldRejectManagerWhenAnotherManagerIsActive() {
        Hub hub = Hub.builder()
                .id(20L)
                .code("HUB-01")
                .name("Hub 01")
                .tenantId(TENANT_ID)
                .build();
        HubStaff newManager = HubStaff.builder()
                .id(200L)
                .code("HM-NEW")
                .fullName("Trưởng hub mới")
                .role(HubStaffRole.MANAGER)
                .status(HubStaffStatus.ACTIVE)
                .build();
        HubStaff currentManager = HubStaff.builder()
                .id(201L)
                .code("HM-OLD")
                .fullName("Trưởng hub hiện tại")
                .role(HubStaffRole.MANAGER)
                .status(HubStaffStatus.ACTIVE)
                .build();
        HubStaffAssignment currentAssignment = HubStaffAssignment.builder()
                .id(1L)
                .hub(hub)
                .staff(currentManager)
                .assignedFrom(LocalDate.now().minusDays(1))
                .tenantId(TENANT_ID)
                .build();

        when(secondMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(hubRepository.findById(hub.getId())).thenReturn(Optional.of(hub));
        when(hubStaffRepository.findByIdAndTenantId(newManager.getId(), TENANT_ID))
                .thenReturn(Optional.of(newManager));
        when(hubStaffAssignmentRepository.findFirstActiveAssignmentByStaffIdAndHubIdAndTenantId(
                newManager.getId(),
                hub.getId(),
                TENANT_ID,
                LocalDate.now()
        )).thenReturn(Optional.empty());
        when(hubStaffAssignmentRepository.findActiveAssignmentsByHubIdAndTenantIdAndStaffRole(
                hub.getId(),
                TENANT_ID,
                LocalDate.now(),
                HubStaffRole.MANAGER
        )).thenReturn(List.of(currentAssignment));

        AppException exception = assertThrows(
                AppException.class,
                () -> hubStaffAssignmentService.assignStaffToHub(newManager.getId(), hub.getId())
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(hubStaffAssignmentRepository, never()).save(any(HubStaffAssignment.class));
    }
}
