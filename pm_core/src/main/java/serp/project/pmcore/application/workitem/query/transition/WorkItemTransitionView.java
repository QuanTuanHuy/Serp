/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.query.transition;

import com.fasterxml.jackson.annotation.JsonInclude;
import serp.project.pmcore.application.workitem.command.transition.support.AvailableTransitionConfiguration;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkItemTransitionView(
        Long id,
        String name,
        Long fromStepId,
        Long toStepId,
        Long screenId,
        Integer sequence,
        StatusView targetStatus,
        StatusCategoryView targetStatusCategory
) {
    public static WorkItemTransitionView from(AvailableTransitionConfiguration configuration) {
        return new WorkItemTransitionView(
                configuration.transition().getId(),
                configuration.transition().getName(),
                configuration.currentStep().getId(),
                configuration.targetStep().getId(),
                configuration.transition().getScreenId(),
                configuration.transition().getSequence(),
                new StatusView(
                        configuration.targetStatus().getId(),
                        configuration.targetStatus().getStatusKey(),
                        configuration.targetStatus().getName(),
                        configuration.targetStatus().getIconUrl()
                ),
                new StatusCategoryView(
                        configuration.targetStatusCategory().getId(),
                        configuration.targetStatusCategory().getKey(),
                        configuration.targetStatusCategory().getName()
                )
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StatusView(
            Long id,
            String key,
            String name,
            String iconUrl
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record StatusCategoryView(
            Long id,
            String key,
            String name
    ) {
    }
}
