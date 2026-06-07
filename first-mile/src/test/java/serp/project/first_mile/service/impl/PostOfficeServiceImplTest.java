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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import serp.project.first_mile.caller.GeocodeCaller;
import serp.project.first_mile.kafka.HubPostOfficeSyncEventPublisher;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.PostOfficeFilterRequest;
import serp.project.first_mile.dto.response.PostOfficeResponse;
import serp.project.first_mile.enums.PostOfficeStatus;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.ProvinceRepository;
import serp.project.first_mile.repository.WardRepository;
import serp.project.first_mile.service.FileStorageService;
import serp.project.first_mile.service.PostOfficeImportExcelService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostOfficeServiceImplTest {

    @Mock
    private PostOfficeRepository postOfficeRepository;

    @Mock
    private ProvinceRepository provinceRepository;

    @Mock
    private WardRepository wardRepository;

    @Mock
    private FirstMileAccessUtils firstMileAccessUtils;

    @Mock
    private GeocodeCaller geocodeCaller;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PostOfficeImportExcelService postOfficeImportExcelService;

    @Mock
    private HubPostOfficeSyncEventPublisher hubPostOfficeSyncEventPublisher;

    @InjectMocks
    private PostOfficeServiceImpl postOfficeService;

    @Test
    void getPostOfficesShouldThrowWhenFilterRangeIsInvalid() {
        when(firstMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(1L);

        PostOfficeFilterRequest filterRequest = PostOfficeFilterRequest.builder()
                .minDailyCapacity(10)
                .maxDailyCapacity(5)
                .build();

        AppException exception = assertThrows(
                AppException.class,
                () -> postOfficeService.getPostOffices(0, 20, filterRequest)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
        verify(postOfficeRepository, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getPostOfficesShouldReturnPagedResultWhenFilterIsValid() {
        when(firstMileAccessUtils.getCurrentTenantIdOrThrow()).thenReturn(1L);

        PostOffice postOffice = new PostOffice();
        postOffice.setId(1L);
        postOffice.setCode("PO-HCM-01");
        postOffice.setName("Post Office 01");
        postOffice.setStatus(PostOfficeStatus.ACTIVE);
        postOffice.setTenantId(1L);

        Page<PostOffice> postOfficePage = new PageImpl<>(
                List.of(postOffice),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id")),
                1
        );

        when(postOfficeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(postOfficePage);

        PostOfficeFilterRequest filterRequest = PostOfficeFilterRequest.builder()
                .keyword("PO")
                .status(PostOfficeStatus.ACTIVE)
                .build();

        PageResponse<PostOfficeResponse> result = postOfficeService.getPostOffices(0, 20, filterRequest);

        assertEquals(1, result.items().size());
        assertEquals("PO-HCM-01", result.items().get(0).code());
        assertEquals(1L, result.totalElements());
    }
}
