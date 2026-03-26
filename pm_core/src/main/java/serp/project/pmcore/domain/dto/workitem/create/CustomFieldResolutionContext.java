/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.workitem.create;

import serp.project.pmcore.domain.entity.CustomFieldOptionEntity;

import java.util.List;

public record CustomFieldResolutionContext(Long customFieldId,
                                           Long customFieldContextId,
                                           String fieldKey,
                                           String normalizedTypeKey,
                                           List<CustomFieldOptionEntity> options) {

    public CustomFieldResolutionContext {
        options = List.copyOf(options);
    }
}
