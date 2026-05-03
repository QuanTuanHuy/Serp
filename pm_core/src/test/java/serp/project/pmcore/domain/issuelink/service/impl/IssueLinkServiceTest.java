/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.issuelink.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.issuelink.entity.IssueLinkEntity;
import serp.project.pmcore.domain.issuelink.port.IIssueLinkPort;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueLinkServiceTest {

    @Mock
    private IIssueLinkPort issueLinkPort;

    @InjectMocks
    private IssueLinkService issueLinkService;

    @Test
    void createShouldRejectSelfLink() {
        IssueLinkEntity draft = IssueLinkEntity.builder()
                .sourceId(10L)
                .targetId(10L)
                .linkTypeId(1L)
                .build();

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> issueLinkService.create(draft, 1L, 99L)
        );

        assertEquals(DomainErrorCode.SELF_LINK_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    void createShouldRejectDuplicateLink() {
        IssueLinkEntity draft = IssueLinkEntity.builder()
                .sourceId(10L)
                .targetId(20L)
                .linkTypeId(1L)
                .build();
        when(issueLinkPort.getActiveDuplicate(1L, 10L, 20L, 1L))
                .thenReturn(Optional.of(IssueLinkEntity.builder().id(30L).build()));

        BusinessRuleViolationException exception = assertThrows(
                BusinessRuleViolationException.class,
                () -> issueLinkService.create(draft, 1L, 99L)
        );

        assertEquals(DomainErrorCode.DUPLICATE_ISSUE_LINK, exception.getErrorCode());
    }

    @Test
    void createShouldPersistValidLink() {
        IssueLinkEntity draft = IssueLinkEntity.builder()
                .sourceId(10L)
                .targetId(20L)
                .linkTypeId(1L)
                .build();
        IssueLinkEntity saved = IssueLinkEntity.builder()
                .id(55L)
                .tenantId(1L)
                .sourceId(10L)
                .targetId(20L)
                .linkTypeId(1L)
                .createdBy(99L)
                .build();
        when(issueLinkPort.getActiveDuplicate(1L, 10L, 20L, 1L)).thenReturn(Optional.empty());
        when(issueLinkPort.save(any(IssueLinkEntity.class))).thenReturn(saved);

        IssueLinkEntity result = issueLinkService.create(draft, 1L, 99L);

        assertEquals(55L, result.getId());
        verify(issueLinkPort).save(any(IssueLinkEntity.class));
    }
}
