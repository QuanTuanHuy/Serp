/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import serp.project.pmcore.domain.workitem.query.ResolutionListCriteria;
import serp.project.pmcore.infrastructure.store.mapper.ResolutionMapper;
import serp.project.pmcore.infrastructure.store.repository.IResolutionRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolutionAdapterTest {

    private static final Long TENANT_ID = 1L;

    @Mock
    private IResolutionRepository resolutionRepository;

    private ResolutionAdapter resolutionAdapter;

    @BeforeEach
    void setUp() {
        resolutionAdapter = new ResolutionAdapter(resolutionRepository, new ResolutionMapper());
    }

    @Test
    void listResolutionsIncludingSystemShouldPassNullSearchPatternWhenSearchIsBlank() {
        ResolutionListCriteria criteria = ResolutionListCriteria.builder()
                .search("   ")
                .isSystem(false)
                .sortBy("name")
                .sortDirection("ASC")
                .build();

        when(resolutionRepository.findAllVisibleWithFilters(
                eq(TENANT_ID),
                eq(null),
                eq(false),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        resolutionAdapter.listResolutionsIncludingSystem(TENANT_ID, criteria);

        verify(resolutionRepository).findAllVisibleWithFilters(
                eq(TENANT_ID),
                eq(null),
                eq(false),
                any(Pageable.class)
        );
    }

    @Test
    void listResolutionsIncludingSystemShouldPassLowercaseSearchPattern() {
        ResolutionListCriteria criteria = ResolutionListCriteria.builder()
                .search(" Fixed ")
                .sortBy("name")
                .sortDirection("ASC")
                .build();

        when(resolutionRepository.findAllVisibleWithFilters(
                eq(TENANT_ID),
                eq("%fixed%"),
                eq(null),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        resolutionAdapter.listResolutionsIncludingSystem(TENANT_ID, criteria);

        verify(resolutionRepository).findAllVisibleWithFilters(
                eq(TENANT_ID),
                eq("%fixed%"),
                eq(null),
                any(Pageable.class)
        );
    }
}
