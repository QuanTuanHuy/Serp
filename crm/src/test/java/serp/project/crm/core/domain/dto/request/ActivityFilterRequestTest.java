/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActivityFilterRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeDateFiltersFromEpochMillis() throws Exception {
        String json = """
                {
                  "activityDateFrom": 1782900000000,
                  "activityDateTo": 1782986400000,
                  "dueDateFrom": 1783072800000,
                  "dueDateTo": 1783159200000
                }
                """;

        ActivityFilterRequest request = objectMapper.readValue(json, ActivityFilterRequest.class);

        assertEquals(1782900000000L, request.getActivityDateFrom());
        assertEquals(1782986400000L, request.getActivityDateTo());
        assertEquals(1783072800000L, request.getDueDateFrom());
        assertEquals(1783159200000L, request.getDueDateTo());
    }
}
