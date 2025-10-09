package serp.project.ptm_optimization.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TopicConstants {
    @UtilityClass
    public class PTMTask {
        public static final String TOPIC = "ptm.task.topic";

        public static final String CREATE_TASK = "taskManagerCreateTask";
        public static final String UPDATE_TASK = "taskManagerUpdateTask";
        public static final String DELETE_TASK = "taskManagerDeleteTask";
    }
}