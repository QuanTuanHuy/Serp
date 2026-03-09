/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for RedisCacheAdapter
 */

package serp.project.discuss_service.infrastructure.client;

import io.github.serp.platform.redis.cache.SerpCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.ParameterizedTypeReference;
import serp.project.discuss_service.kernel.utils.JsonUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    private static final String NAMESPACE = "legacy";

    @Mock
    private JsonUtils jsonUtils;

    @Mock
    private SerpCacheService cacheService;

    @InjectMocks
    private RedisCacheAdapter redisCacheAdapter;

    @Test
    void setToCache_WithTtl_ShouldSerializeAndStoreAsJson() {
        Object payload = Map.of("foo", "bar");
        String json = "{\"foo\":\"bar\"}";
        when(jsonUtils.toJson(payload)).thenReturn(json);

        redisCacheAdapter.setToCache("discuss:msg:1", payload, 30);

        verify(cacheService).put(NAMESPACE, "discuss:msg:1", json, Duration.ofSeconds(30));
    }

    @Test
    void getFromCache_Typed_ShouldDeserializeFromJson() {
        String json = "{\"foo\":\"bar\"}";
        TestPayload parsed = new TestPayload("bar");
        when(cacheService.get(NAMESPACE, "discuss:msg:2")).thenReturn(Optional.of(json));
        when(jsonUtils.fromJson(json, TestPayload.class)).thenReturn(parsed);

        TestPayload result = redisCacheAdapter.getFromCache("discuss:msg:2", TestPayload.class);

        assertEquals(parsed, result);
    }

    @Test
    void getFromCache_Parameterized_ShouldDeserializeFromJson() {
        String json = "[\"a\",\"b\"]";
        List<String> parsed = List.of("a", "b");
        ParameterizedTypeReference<List<String>> typeReference = new ParameterizedTypeReference<>() {
        };

        when(cacheService.get(NAMESPACE, "discuss:list")).thenReturn(Optional.of(json));
        when(jsonUtils.fromJson(eq(json), any(ParameterizedTypeReference.class))).thenReturn(parsed);

        List<String> result = redisCacheAdapter.getFromCache("discuss:list", typeReference);

        assertEquals(parsed, result);
    }

    @Test
    void deleteAllByPattern_ShouldDelegateToStarterCacheService() {
        redisCacheAdapter.deleteAllByPattern("discuss:channel:*");

        verify(cacheService).evictByPattern(NAMESPACE, "discuss:channel:*", 500);
    }

    @Test
    void scanAndDelete_ShouldDelegateToStarterCacheService() {
        redisCacheAdapter.scanAndDelete("discuss:typing:*", 100);

        verify(cacheService).evictByPattern(NAMESPACE, "discuss:typing:*", 100);
    }

    @Test
    void batchHashIncrement_ShouldDelegateToStarterCacheService() {
        Map<String, Map<String, Long>> operations = Map.of(
                "discuss:unread:1", Map.of("10", 1L),
                "discuss:unread:2", Map.of("10", 1L));

        redisCacheAdapter.batchHashIncrement(operations);

        verify(cacheService).batchHashIncrement(NAMESPACE, operations);
    }

    @Test
    void batchHashGetAll_ShouldReturnStarterCacheServiceResult() {
        List<String> keys = List.of("discuss:presence:user:1", "discuss:presence:user:2");
        Map<String, Map<String, String>> expected = Map.of(
                "discuss:presence:user:1", Map.of("status", "ONLINE"),
                "discuss:presence:user:2", Map.of("status", "OFFLINE"));
        when(cacheService.batchHashGetAll(NAMESPACE, keys)).thenReturn(expected);

        Map<String, Map<String, String>> result = redisCacheAdapter.batchHashGetAll(keys);

        assertEquals(expected, result);
    }

    @Test
    void setToCache_ShouldThrowWhenSerializationFails() {
        when(jsonUtils.toJson(any())).thenThrow(new RuntimeException("serialize-failed"));

        assertThrows(RuntimeException.class, () -> redisCacheAdapter.setToCache("discuss:key", new Object()));
    }

    @Test
    void getFromCache_ShouldReturnNullWhenCacheServiceFails() {
        when(cacheService.get(NAMESPACE, "discuss:key")).thenThrow(new RuntimeException("cache-down"));

        assertNull(redisCacheAdapter.getFromCache("discuss:key"));
    }

    private record TestPayload(String foo) {
    }
}
