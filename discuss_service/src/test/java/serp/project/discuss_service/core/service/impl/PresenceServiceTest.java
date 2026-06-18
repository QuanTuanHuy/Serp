/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for PresenceService
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.domain.entity.UserPresenceEntity;
import serp.project.discuss_service.core.domain.enums.UserStatus;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.testutil.TestDataFactory;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @Mock
    private IDiscussCacheService cacheService;

    @Mock
    private IDiscussEventPublisher eventPublisher;

    @InjectMocks
    private PresenceService presenceService;

    @Nested
    @DisplayName("updateUserStatus")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("should publish online event after status becomes available")
        void testUpdateUserStatus_OnlineStatus_PublishesOnlineEvent() {
            // Given
            UserPresenceEntity presence = UserPresenceEntity.offline(
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID
            );
            when(cacheService.getUserPresence(TestDataFactory.USER_ID_1))
                    .thenReturn(Optional.of(presence));

            // When
            presenceService.updateUserStatus(
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    UserStatus.ONLINE,
                    "Available"
            );

            // Then
            verify(cacheService).setUserPresence(presence);
            verify(eventPublisher).publishUserOnline(TestDataFactory.USER_ID_1);
        }

        @Test
        @DisplayName("should publish offline event after status becomes unavailable")
        void testUpdateUserStatus_OfflineStatus_PublishesOfflineEvent() {
            // Given
            UserPresenceEntity presence = UserPresenceEntity.online(
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID
            );
            when(cacheService.getUserPresence(TestDataFactory.USER_ID_1))
                    .thenReturn(Optional.of(presence));

            // When
            presenceService.updateUserStatus(
                    TestDataFactory.USER_ID_1,
                    TestDataFactory.TENANT_ID,
                    UserStatus.OFFLINE,
                    "Away"
            );

            // Then
            verify(cacheService).setUserPresence(presence);
            verify(eventPublisher).publishUserOffline(TestDataFactory.USER_ID_1);
        }
    }
}
