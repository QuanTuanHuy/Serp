/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for user info cache
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse;
import serp.project.discuss_service.core.port.client.IAccountServiceClient;
import serp.project.discuss_service.core.port.client.ICachePort;
import serp.project.discuss_service.core.service.IUserInfoService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

    @Mock
    private IAccountServiceClient accountServiceClient;

    @Mock
    private ICachePort cachePort;

    @InjectMocks
    private UserInfoService userInfoService;

    @Test
    @DisplayName("getUserById should return cached user without calling account service")
    void testGetUserById_CacheHit_ReturnsCachedUser() {
        ChannelMemberResponse.UserInfo cached = user(100L, "Cached User");
        when(cachePort.getFromCache("discuss:user_info:100", ChannelMemberResponse.UserInfo.class))
                .thenReturn(cached);

        Optional<ChannelMemberResponse.UserInfo> result = userInfoService.getUserById(100L);

        assertTrue(result.isPresent());
        assertEquals("Cached User", result.get().getName());
        verify(accountServiceClient, never()).getUserById(100L);
    }

    @Test
    @DisplayName("getUserById should fetch and cache user when cache misses")
    void testGetUserById_CacheMiss_FetchesAndCachesUser() {
        ChannelMemberResponse.UserInfo fetched = user(100L, "Fetched User");
        when(cachePort.getFromCache("discuss:user_info:100", ChannelMemberResponse.UserInfo.class))
                .thenReturn(null);
        when(accountServiceClient.getUserById(100L)).thenReturn(Optional.of(fetched));

        Optional<ChannelMemberResponse.UserInfo> result = userInfoService.getUserById(100L);

        assertTrue(result.isPresent());
        assertEquals("Fetched User", result.get().getName());
        verify(cachePort).setToCache(
                "discuss:user_info:100",
                fetched,
                IUserInfoService.USER_INFO_CACHE_TTL
        );
    }

    @Test
    @DisplayName("getUsersByIds should fetch only cache misses and keep cached users")
    void testGetUsersByIds_PartialCacheHit_FetchesOnlyMissingUsers() {
        ChannelMemberResponse.UserInfo cached = user(100L, "Cached User");
        ChannelMemberResponse.UserInfo fetched = user(200L, "Fetched User");
        when(cachePort.getFromCache("discuss:user_info:100", ChannelMemberResponse.UserInfo.class))
                .thenReturn(cached);
        when(cachePort.getFromCache("discuss:user_info:200", ChannelMemberResponse.UserInfo.class))
                .thenReturn(null);
        when(accountServiceClient.getUsersByIds(List.of(200L))).thenReturn(List.of(fetched));

        List<ChannelMemberResponse.UserInfo> result = userInfoService.getUsersByIds(List.of(100L, 200L));

        assertEquals(List.of(cached, fetched), result);
        verify(accountServiceClient).getUsersByIds(List.of(200L));
        verify(cachePort).setToCache(
                "discuss:user_info:200",
                fetched,
                IUserInfoService.USER_INFO_CACHE_TTL
        );
    }

    private ChannelMemberResponse.UserInfo user(Long id, String name) {
        return ChannelMemberResponse.UserInfo.builder()
                .id(id)
                .name(name)
                .email(name.toLowerCase().replace(" ", ".") + "@example.com")
                .avatarUrl("https://example.com/avatar/" + id + ".png")
                .build();
    }
}
