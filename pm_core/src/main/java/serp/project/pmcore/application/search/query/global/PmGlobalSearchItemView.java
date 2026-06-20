/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.search.query.global;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PmGlobalSearchItemView(
        String id,
        String title,
        String subtitle,
        String url,
        Map<String, Object> meta
) {
}
