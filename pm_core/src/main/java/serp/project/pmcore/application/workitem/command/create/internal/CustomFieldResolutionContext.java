/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.internal;

import java.util.List;

import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;

public record CustomFieldResolutionContext(Long customFieldId,
                                           Long customFieldContextId,
                                           String fieldKey,
                                           String normalizedTypeKey,
                                           List<CustomFieldOptionEntity> options) {

    public CustomFieldResolutionContext {
        options = List.copyOf(options);
    }
}
