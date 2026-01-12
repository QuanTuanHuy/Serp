/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for WebSocketSessionRegistry
 */

package serp.project.discuss_service.kernel.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WebSocketSessionRegistry Tests")
class WebSocketSessionRegistryTest {

    private WebSocketSessionRegistry registry;

    private static final Long USER_ID_1 = 100L;
    private static final Long USER_ID_2 = 200L;
    private static final Long USER_ID_3 = 300L;
    private static final Long CHANNEL_ID_1 = 1000L;
    private static final Long CHANNEL_ID_2 = 2000L;
    private static final String SESSION_ID_1 = "session-1";
    private static final String SESSION_ID_2 = "session-2";
    private static final String SESSION_ID_3 = "session-3";

    @BeforeEach
    void setUp() {
        registry = new WebSocketSessionRegistry();
    }

    @Nested
    @DisplayName("Session Registration Tests")
    class SessionRegistrationTests {

        @Test
        @DisplayName("Should register a user session successfully")
        void shouldRegisterUserSession() {
            // When
            registry.registerSession(USER_ID_1, SESSION_ID_1);

            // Then
            assertTrue(registry.isUserConnected(USER_ID_1));
            assertEquals(1, registry.getConnectedUserCount());
            assertEquals(1, registry.getActiveSessionCount());
        }

        @Test
        @DisplayName("Should register multiple sessions for same user")
        void shouldRegisterMultipleSessionsForSameUser() {
            // When
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_1, SESSION_ID_2);

            // Then
            assertTrue(registry.isUserConnected(USER_ID_1));
            assertEquals(1, registry.getConnectedUserCount());
            assertEquals(2, registry.getActiveSessionCount());
            
            Set<String> sessions = registry.getUserSessionIds(USER_ID_1);
            assertEquals(2, sessions.size());
            assertTrue(sessions.contains(SESSION_ID_1));
            assertTrue(sessions.contains(SESSION_ID_2));
        }

        @Test
        @DisplayName("Should register sessions for multiple users")
        void shouldRegisterSessionsForMultipleUsers() {
            // When
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_2, SESSION_ID_2);
            registry.registerSession(USER_ID_3, SESSION_ID_3);

            // Then
            assertEquals(3, registry.getConnectedUserCount());
            assertEquals(3, registry.getActiveSessionCount());
            assertTrue(registry.isUserConnected(USER_ID_1));
            assertTrue(registry.isUserConnected(USER_ID_2));
            assertTrue(registry.isUserConnected(USER_ID_3));
        }

        @Test
        @DisplayName("Should unregister session successfully")
        void shouldUnregisterSession() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            assertTrue(registry.isUserConnected(USER_ID_1));

            // When
            registry.unregisterSession(SESSION_ID_1);

            // Then
            assertFalse(registry.isUserConnected(USER_ID_1));
            assertEquals(0, registry.getConnectedUserCount());
            assertEquals(0, registry.getActiveSessionCount());
        }

        @Test
        @DisplayName("Should keep user connected when unregistering one of multiple sessions")
        void shouldKeepUserConnectedWhenUnregisteringOneSession() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_1, SESSION_ID_2);
            assertEquals(2, registry.getActiveSessionCount());

            // When
            registry.unregisterSession(SESSION_ID_1);

            // Then
            assertTrue(registry.isUserConnected(USER_ID_1));
            assertEquals(1, registry.getConnectedUserCount());
            assertEquals(1, registry.getActiveSessionCount());
            
            Set<String> sessions = registry.getUserSessionIds(USER_ID_1);
            assertEquals(1, sessions.size());
            assertTrue(sessions.contains(SESSION_ID_2));
        }

        @Test
        @DisplayName("Should handle unregistering non-existent session gracefully")
        void shouldHandleUnregisteringNonExistentSession() {
            // When/Then - should not throw
            assertDoesNotThrow(() -> registry.unregisterSession("non-existent-session"));
            assertEquals(0, registry.getConnectedUserCount());
        }

        @Test
        @DisplayName("Should get user ID by session ID")
        void shouldGetUserIdBySessionId() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_2, SESSION_ID_2);

            // Then
            assertEquals(USER_ID_1, registry.getUserIdBySessionId(SESSION_ID_1));
            assertEquals(USER_ID_2, registry.getUserIdBySessionId(SESSION_ID_2));
            assertNull(registry.getUserIdBySessionId("non-existent"));
        }
    }

    @Nested
    @DisplayName("Channel Subscription Tests")
    class ChannelSubscriptionTests {

        @Test
        @DisplayName("Should subscribe user to channel")
        void shouldSubscribeUserToChannel() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);

            // When
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);

            // Then
            Set<Long> subscribers = registry.getChannelSubscribers(CHANNEL_ID_1);
            assertEquals(1, subscribers.size());
            assertTrue(subscribers.contains(USER_ID_1));
        }

        @Test
        @DisplayName("Should subscribe multiple users to same channel")
        void shouldSubscribeMultipleUsersToChannel() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_2, SESSION_ID_2);
            registry.registerSession(USER_ID_3, SESSION_ID_3);

            // When
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);
            registry.subscribeToChannel(USER_ID_2, CHANNEL_ID_1);
            registry.subscribeToChannel(USER_ID_3, CHANNEL_ID_1);

            // Then
            Set<Long> subscribers = registry.getChannelSubscribers(CHANNEL_ID_1);
            assertEquals(3, subscribers.size());
            assertTrue(subscribers.containsAll(Set.of(USER_ID_1, USER_ID_2, USER_ID_3)));
        }

        @Test
        @DisplayName("Should subscribe user to multiple channels")
        void shouldSubscribeUserToMultipleChannels() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);

            // When
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_2);

            // Then
            Set<Long> userChannels = registry.getUserSubscribedChannels(USER_ID_1);
            assertEquals(2, userChannels.size());
            assertTrue(userChannels.containsAll(Set.of(CHANNEL_ID_1, CHANNEL_ID_2)));
        }

        @Test
        @DisplayName("Should unsubscribe user from channel")
        void shouldUnsubscribeUserFromChannel() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);
            assertTrue(registry.getChannelSubscribers(CHANNEL_ID_1).contains(USER_ID_1));

            // When
            registry.unsubscribeFromChannel(USER_ID_1, CHANNEL_ID_1);

            // Then
            assertFalse(registry.getChannelSubscribers(CHANNEL_ID_1).contains(USER_ID_1));
            assertFalse(registry.getUserSubscribedChannels(USER_ID_1).contains(CHANNEL_ID_1));
        }

        @Test
        @DisplayName("Should clean up channel when last subscriber unsubscribes")
        void shouldCleanUpChannelWhenLastSubscriberLeaves() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);

            // When
            registry.unsubscribeFromChannel(USER_ID_1, CHANNEL_ID_1);

            // Then
            assertTrue(registry.getChannelSubscribers(CHANNEL_ID_1).isEmpty());
        }

        @Test
        @DisplayName("Should handle unsubscribing from non-subscribed channel")
        void shouldHandleUnsubscribingFromNonSubscribedChannel() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);

            // When/Then - should not throw
            assertDoesNotThrow(() -> registry.unsubscribeFromChannel(USER_ID_1, CHANNEL_ID_1));
        }

        @Test
        @DisplayName("Should clean up subscriptions when user disconnects completely")
        void shouldCleanUpSubscriptionsWhenUserDisconnects() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_2);

            // When
            registry.unregisterSession(SESSION_ID_1);

            // Then
            assertFalse(registry.getChannelSubscribers(CHANNEL_ID_1).contains(USER_ID_1));
            assertFalse(registry.getChannelSubscribers(CHANNEL_ID_2).contains(USER_ID_1));
            assertTrue(registry.getUserSubscribedChannels(USER_ID_1).isEmpty());
        }

        @Test
        @DisplayName("Should NOT clean up subscriptions when user still has other sessions")
        void shouldNotCleanUpSubscriptionsWhenUserStillConnected() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_1, SESSION_ID_2);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);

            // When
            registry.unregisterSession(SESSION_ID_1);

            // Then - subscriptions should remain because user still has session-2
            assertTrue(registry.getChannelSubscribers(CHANNEL_ID_1).contains(USER_ID_1));
            assertTrue(registry.getUserSubscribedChannels(USER_ID_1).contains(CHANNEL_ID_1));
        }
    }

    @Nested
    @DisplayName("Query Methods Tests")
    class QueryMethodsTests {

        @Test
        @DisplayName("Should get empty set for non-existent channel subscribers")
        void shouldGetEmptySetForNonExistentChannel() {
            Set<Long> subscribers = registry.getChannelSubscribers(999L);
            assertNotNull(subscribers);
            assertTrue(subscribers.isEmpty());
        }

        @Test
        @DisplayName("Should get empty set for user with no subscriptions")
        void shouldGetEmptySetForUserWithNoSubscriptions() {
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            Set<Long> channels = registry.getUserSubscribedChannels(USER_ID_1);
            assertNotNull(channels);
            assertTrue(channels.isEmpty());
        }

        @Test
        @DisplayName("Should get empty set for non-connected user subscriptions")
        void shouldGetEmptySetForNonConnectedUserSubscriptions() {
            Set<Long> channels = registry.getUserSubscribedChannels(USER_ID_1);
            assertNotNull(channels);
            assertTrue(channels.isEmpty());
        }

        @Test
        @DisplayName("Should get all connected users")
        void shouldGetAllConnectedUsers() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_2, SESSION_ID_2);

            // When
            Set<Long> connectedUsers = registry.getConnectedUsers();

            // Then
            assertEquals(2, connectedUsers.size());
            assertTrue(connectedUsers.containsAll(Set.of(USER_ID_1, USER_ID_2)));
        }

        @Test
        @DisplayName("Should return empty set when no users connected")
        void shouldReturnEmptySetWhenNoUsersConnected() {
            Set<Long> connectedUsers = registry.getConnectedUsers();
            assertNotNull(connectedUsers);
            assertTrue(connectedUsers.isEmpty());
        }

        @Test
        @DisplayName("Should get empty set for user with no sessions")
        void shouldGetEmptySetForUserWithNoSessions() {
            Set<String> sessions = registry.getUserSessionIds(USER_ID_1);
            assertNotNull(sessions);
            assertTrue(sessions.isEmpty());
        }
    }

    @Nested
    @DisplayName("Online Channel Subscribers Tests")
    class OnlineChannelSubscribersTests {

        @Test
        @DisplayName("Should get online subscribers for channel")
        void shouldGetOnlineSubscribersForChannel() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_2, SESSION_ID_2);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);
            registry.subscribeToChannel(USER_ID_2, CHANNEL_ID_1);

            // When
            Set<Long> onlineSubscribers = registry.getOnlineChannelSubscribers(CHANNEL_ID_1);

            // Then
            assertEquals(2, onlineSubscribers.size());
            assertTrue(onlineSubscribers.containsAll(Set.of(USER_ID_1, USER_ID_2)));
        }

        @Test
        @DisplayName("Should only return connected subscribers")
        void shouldOnlyReturnConnectedSubscribers() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.registerSession(USER_ID_2, SESSION_ID_2);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);
            registry.subscribeToChannel(USER_ID_2, CHANNEL_ID_1);
            
            // Disconnect user 2
            registry.unregisterSession(SESSION_ID_2);

            // When
            Set<Long> onlineSubscribers = registry.getOnlineChannelSubscribers(CHANNEL_ID_1);

            // Then
            assertEquals(1, onlineSubscribers.size());
            assertTrue(onlineSubscribers.contains(USER_ID_1));
            assertFalse(onlineSubscribers.contains(USER_ID_2));
        }

        @Test
        @DisplayName("Should return empty set for channel with no subscribers")
        void shouldReturnEmptySetForChannelWithNoSubscribers() {
            Set<Long> onlineSubscribers = registry.getOnlineChannelSubscribers(CHANNEL_ID_1);
            assertNotNull(onlineSubscribers);
            assertTrue(onlineSubscribers.isEmpty());
        }

        @Test
        @DisplayName("Should return empty set when all subscribers disconnected")
        void shouldReturnEmptySetWhenAllSubscribersDisconnected() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);
            
            // Disconnect user
            registry.unregisterSession(SESSION_ID_1);

            // When
            Set<Long> onlineSubscribers = registry.getOnlineChannelSubscribers(CHANNEL_ID_1);

            // Then
            assertTrue(onlineSubscribers.isEmpty());
        }
    }

    @Nested
    @DisplayName("Connection Status Tests")
    class ConnectionStatusTests {

        @Test
        @DisplayName("Should return false for non-connected user")
        void shouldReturnFalseForNonConnectedUser() {
            assertFalse(registry.isUserConnected(USER_ID_1));
        }

        @Test
        @DisplayName("Should return true for connected user")
        void shouldReturnTrueForConnectedUser() {
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            assertTrue(registry.isUserConnected(USER_ID_1));
        }

        @Test
        @DisplayName("Should return false after user disconnects")
        void shouldReturnFalseAfterUserDisconnects() {
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.unregisterSession(SESSION_ID_1);
            assertFalse(registry.isUserConnected(USER_ID_1));
        }

        @Test
        @DisplayName("Should correctly count connected users")
        void shouldCorrectlyCountConnectedUsers() {
            assertEquals(0, registry.getConnectedUserCount());
            
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            assertEquals(1, registry.getConnectedUserCount());
            
            registry.registerSession(USER_ID_2, SESSION_ID_2);
            assertEquals(2, registry.getConnectedUserCount());
            
            registry.unregisterSession(SESSION_ID_1);
            assertEquals(1, registry.getConnectedUserCount());
        }

        @Test
        @DisplayName("Should correctly count active sessions")
        void shouldCorrectlyCountActiveSessions() {
            assertEquals(0, registry.getActiveSessionCount());
            
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            assertEquals(1, registry.getActiveSessionCount());
            
            registry.registerSession(USER_ID_1, SESSION_ID_2); // Same user, second session
            assertEquals(2, registry.getActiveSessionCount());
            
            registry.registerSession(USER_ID_2, SESSION_ID_3);
            assertEquals(3, registry.getActiveSessionCount());
            
            registry.unregisterSession(SESSION_ID_1);
            assertEquals(2, registry.getActiveSessionCount());
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should return immutable set for channel subscribers")
        void shouldReturnImmutableSetForChannelSubscribers() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);

            // When
            Set<Long> subscribers = registry.getChannelSubscribers(CHANNEL_ID_1);

            // Then
            assertThrows(UnsupportedOperationException.class, () -> subscribers.add(USER_ID_2));
        }

        @Test
        @DisplayName("Should return immutable set for user subscriptions")
        void shouldReturnImmutableSetForUserSubscriptions() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);
            registry.subscribeToChannel(USER_ID_1, CHANNEL_ID_1);

            // When
            Set<Long> channels = registry.getUserSubscribedChannels(USER_ID_1);

            // Then
            assertThrows(UnsupportedOperationException.class, () -> channels.add(CHANNEL_ID_2));
        }

        @Test
        @DisplayName("Should return immutable set for connected users")
        void shouldReturnImmutableSetForConnectedUsers() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);

            // When
            Set<Long> users = registry.getConnectedUsers();

            // Then
            assertThrows(UnsupportedOperationException.class, () -> users.add(USER_ID_2));
        }

        @Test
        @DisplayName("Should return immutable set for user session IDs")
        void shouldReturnImmutableSetForUserSessionIds() {
            // Given
            registry.registerSession(USER_ID_1, SESSION_ID_1);

            // When
            Set<String> sessions = registry.getUserSessionIds(USER_ID_1);

            // Then
            assertThrows(UnsupportedOperationException.class, () -> sessions.add(SESSION_ID_2));
        }
    }
}
