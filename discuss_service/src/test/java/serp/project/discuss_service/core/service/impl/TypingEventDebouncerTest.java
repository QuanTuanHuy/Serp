/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for typing event debouncer
 */

package serp.project.discuss_service.core.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.discuss_service.core.port.client.ICachePort;
import serp.project.discuss_service.kernel.property.RealtimeProperties;
import serp.project.discuss_service.testutil.TestDataFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypingEventDebouncerTest {

    @Mock
    private ICachePort cachePort;

    @Test
    @DisplayName("should publish when debounce key does not exist")
    void testShouldPublish_NoExistingKey_ReturnsTrueAndStoresKey() {
        TypingEventDebouncer debouncer = new TypingEventDebouncer(cachePort, properties(2000));
        String key = "discuss:typing:debounce:1000:100:true";
        when(cachePort.exists(key)).thenReturn(false);

        boolean result = debouncer.shouldPublish(
                TestDataFactory.CHANNEL_ID,
                TestDataFactory.USER_ID_1,
                true
        );

        assertTrue(result);
        verify(cachePort).setToCache(key, "1", 2);
    }

    @Test
    @DisplayName("should suppress when debounce key exists")
    void testShouldPublish_ExistingKey_ReturnsFalse() {
        TypingEventDebouncer debouncer = new TypingEventDebouncer(cachePort, properties(2000));
        when(cachePort.exists("discuss:typing:debounce:1000:100:true")).thenReturn(true);

        boolean result = debouncer.shouldPublish(
                TestDataFactory.CHANNEL_ID,
                TestDataFactory.USER_ID_1,
                true
        );

        assertFalse(result);
    }

    private RealtimeProperties properties(long debounceMs) {
        RealtimeProperties properties = new RealtimeProperties();
        properties.getTyping().setDebounceMs(debounceMs);
        return properties;
    }
}
