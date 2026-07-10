/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.PostOfficeStaff;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;
import serp.project.first_mile.enums.PostOfficeStaffRole;
import serp.project.first_mile.enums.PostOfficeStaffStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.PostOfficeStaffAssignmentRepository;
import serp.project.first_mile.repository.PostOfficeStaffRepository;
import serp.project.first_mile.service.FileStorageService;

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
class PostOfficeStaffServiceImplTest {
    private static final Long TENANT_ID = 1L;

    @Mock
    private PostOfficeStaffRepository postOfficeStaffRepository;

    @Mock
    private PostOfficeRepository postOfficeRepository;

    @Mock
    private PostOfficeStaffAssignmentRepository postOfficeStaffAssignmentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FirstMileAccessUtils firstMileAccessUtils;

    @InjectMocks
    private PostOfficeStaffServiceImpl postOfficeStaffService;

    @Test
    void assignManagerToPostOfficeShouldRejectWhenAnotherManagerIsActive() {
        PostOffice postOffice = PostOffice.builder()
                .id(10L)
                .code("PO-01")
                .name("Bưu cục 01")
                .build();
        PostOfficeStaff newManager = PostOfficeStaff.builder()
                .id(100L)
                .code("MGR-NEW")
                .fullName("Trưởng mới")
                .role(PostOfficeStaffRole.MANAGER)
                .status(PostOfficeStaffStatus.ACTIVE)
                .build();
        PostOfficeStaff currentManager = PostOfficeStaff.builder()
                .id(101L)
                .code("MGR-OLD")
                .fullName("Trưởng hiện tại")
                .role(PostOfficeStaffRole.MANAGER)
                .status(PostOfficeStaffStatus.ACTIVE)
                .build();
        PostOfficeStaffAssignment currentAssignment = PostOfficeStaffAssignment.builder()
                .id(1L)
                .postOffice(postOffice)
                .staff(currentManager)
                .assignedFrom(LocalDate.now().minusDays(1))
                .tenantId(TENANT_ID)
                .build();

        when(firstMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(TENANT_ID);
        when(postOfficeStaffRepository.findByIdAndTenantId(newManager.getId(), TENANT_ID))
                .thenReturn(Optional.of(newManager));
        when(postOfficeRepository.findByIdAndTenantId(postOffice.getId(), TENANT_ID))
                .thenReturn(Optional.of(postOffice));
        when(postOfficeStaffAssignmentRepository.findActiveAssignmentsByPostOfficeIdAndTenantIdAndStaffRole(
                postOffice.getId(),
                TENANT_ID,
                LocalDate.now(),
                PostOfficeStaffRole.MANAGER
        )).thenReturn(List.of(currentAssignment));

        AppException exception = assertThrows(
                AppException.class,
                () -> postOfficeStaffService.assignManagerToPostOffice(newManager.getId(), postOffice.getId())
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(postOfficeStaffAssignmentRepository, never()).save(any(PostOfficeStaffAssignment.class));
    }
}
