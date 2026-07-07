/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import serp.project.account.core.domain.entity.OrganizationSubscriptionEntity;
import serp.project.account.core.domain.entity.SubscriptionPlanEntity;
import serp.project.account.core.domain.enums.SubscriptionStatus;
import serp.project.account.core.exception.AppException;
import serp.project.account.core.port.store.IOrganizationSubscriptionPort;
import serp.project.account.core.port.store.ISubscriptionPlanModulePort;
import serp.project.account.core.port.store.ISubscriptionPlanPort;
import serp.project.account.infrastructure.store.mapper.SubscriptionPlanMapper;
import serp.project.account.infrastructure.store.mapper.SubscriptionPlanModuleMapper;

@ExtendWith(MockitoExtension.class)
class SubscriptionPlanServiceTest {

    @Mock
    private ISubscriptionPlanPort subscriptionPlanPort;

    @Mock
    private ISubscriptionPlanModulePort subscriptionPlanModulePort;

    @Mock
    private IOrganizationSubscriptionPort organizationSubscriptionPort;

    @Mock
    private SubscriptionPlanMapper subscriptionPlanMapper;

    @Mock
    private SubscriptionPlanModuleMapper subscriptionPlanModuleMapper;

    @InjectMocks
    private SubscriptionPlanService service;

    @Test
    void deletePlanShouldFailWhenPlanHasPendingSubscriptions() {
        when(subscriptionPlanPort.getById(100L)).thenReturn(Optional.of(plan()));
        when(organizationSubscriptionPort.getByPlanId(100L)).thenReturn(List.of(
                subscription(SubscriptionStatus.ACTIVE),
                subscription(SubscriptionStatus.PENDING)));

        AppException exception = assertThrows(AppException.class, () -> service.deletePlan(100L));

        assertEquals("Subscription plan has pending subscriptions", exception.getMessage());
        verify(subscriptionPlanPort, never()).update(any());
    }

    private SubscriptionPlanEntity plan() {
        return SubscriptionPlanEntity.builder()
                .id(100L)
                .isActive(true)
                .build();
    }

    private OrganizationSubscriptionEntity subscription(SubscriptionStatus status) {
        return OrganizationSubscriptionEntity.builder()
                .subscriptionPlanId(100L)
                .status(status)
                .build();
    }
}
