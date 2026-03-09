package serp.project.pmcore.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EventConstants {
    public static final String SOURCE = "pm_core";
    public static final String VERSION = "1.0";

    @UtilityClass
    public static class Project {
        public static final String TOPIC = "serp.pm.project.events";
        public static final String AGGREGATE = "PROJECT";
        
        public static class EventType {
            public static final String PROJECT_CREATED = "PROJECT_CREATED";
            public static final String PROJECT_UPDATED = "PROJECT_UPDATED";
            public static final String PROJECT_DELETED = "PROJECT_DELETED";
            public static final String PROJECT_ARCHIVED = "PROJECT_ARCHIVED";
            public static final String PROJECT_UNARCHIVED = "PROJECT_UNARCHIVED";
        }
    }

    @UtilityClass
    public static class WorkItem {
        public static final String TOPIC = "serp.pm.workitem.events";
        public static final String AGGREGATE = "WORK_ITEM";

        public static class EventType {
            public static final String WORK_ITEM_CREATED = "WORK_ITEM_CREATED";
            public static final String WORK_ITEM_UPDATED = "WORK_ITEM_UPDATED";
            public static final String WORK_ITEM_DELETED = "WORK_ITEM_DELETED";
            public static final String WORK_ITEM_MOVED = "WORK_ITEM_MOVED";
        }
    }
}
