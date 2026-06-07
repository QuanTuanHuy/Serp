package serp.project.pmcore.domain.shared.constant;

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
            public static final String PROJECT_SCHEMES_UPDATED = "PROJECT_SCHEMES_UPDATED";
            public static final String ROLE_ACTOR_ADDED = "ROLE_ACTOR_ADDED";
            public static final String ROLE_ACTOR_REMOVED = "ROLE_ACTOR_REMOVED";
        }
    }

    @UtilityClass
    public static class ProjectCategory {
        public static final String TOPIC = "serp.pm.project-category.events";
        public static final String AGGREGATE = "PROJECT_CATEGORY";

        public static class EventType {
            public static final String PROJECT_CATEGORY_CREATED = "PROJECT_CATEGORY_CREATED";
            public static final String PROJECT_CATEGORY_UPDATED = "PROJECT_CATEGORY_UPDATED";
            public static final String PROJECT_CATEGORY_DELETED = "PROJECT_CATEGORY_DELETED";
        }
    }

    @UtilityClass
    public static class ProjectRole {
        public static final String TOPIC = "serp.pm.project-role.events";
        public static final String AGGREGATE = "PROJECT_ROLE";

        public static class EventType {
            public static final String PROJECT_ROLE_CREATED = "PROJECT_ROLE_CREATED";
            public static final String PROJECT_ROLE_UPDATED = "PROJECT_ROLE_UPDATED";
            public static final String PROJECT_ROLE_DELETED = "PROJECT_ROLE_DELETED";
        }
    }

    @UtilityClass
    public static class ProjectComponent {
        public static final String TOPIC = "serp.pm.component.events";
        public static final String AGGREGATE = "PROJECT_COMPONENT";

        public static class EventType {
            public static final String PROJECT_COMPONENT_CREATED = "COMPONENT_CREATED";
            public static final String PROJECT_COMPONENT_UPDATED = "PROJECT_COMPONENT_UPDATED";
            public static final String PROJECT_COMPONENT_DELETED = "PROJECT_COMPONENT_DELETED";
        }
    }

    @UtilityClass
    public static class WorkItem {
        public static final String TOPIC = "serp.pm.workitem.events";
        public static final String AGGREGATE = "WORK_ITEM";

        public static class EventType {
            public static final String WORK_ITEM_ASSIGNED = "WORK_ITEM_ASSIGNED";
            public static final String WORK_ITEM_CREATED = "WORK_ITEM_CREATED";
            public static final String WORK_ITEM_UPDATED = "WORK_ITEM_UPDATED";
            public static final String WORK_ITEM_DELETED = "WORK_ITEM_DELETED";
            public static final String WORK_ITEM_MOVED = "WORK_ITEM_MOVED";
            public static final String WORK_ITEM_STATUS_CHANGED = "WORK_ITEM_STATUS_CHANGED";
        }
    }

    @UtilityClass
    public static class Worklog {
        public static final String TOPIC = "serp.pm.worklog.events";
        public static final String AGGREGATE = "WORKLOG";

        public static class EventType {
            public static final String WORKLOG_CREATED = "WORKLOG_CREATED";
            public static final String WORKLOG_UPDATED = "WORKLOG_UPDATED";
            public static final String WORKLOG_DELETED = "WORKLOG_DELETED";
        }
    }

    @UtilityClass
    public static class IssueLink {
        public static final String TOPIC = "serp.pm.issuelink.events";
        public static final String AGGREGATE = "ISSUE_LINK";

        public static class EventType {
            public static final String ISSUE_LINK_CREATED = "ISSUE_LINK_CREATED";
            public static final String ISSUE_LINK_DELETED = "ISSUE_LINK_DELETED";
        }
    }

    @UtilityClass
    public static class IssueType {
        public static final String TOPIC = "serp.pm.issuetype.events";
        public static final String AGGREGATE = "ISSUE_TYPE";

        public static class EventType {
            public static final String ISSUE_TYPE_CREATED = "ISSUE_TYPE_CREATED";
            public static final String ISSUE_TYPE_UPDATED = "ISSUE_TYPE_UPDATED";
            public static final String ISSUE_TYPE_DELETED = "ISSUE_TYPE_DELETED";
        }
    }

    @UtilityClass
    public static class Priority {
        public static final String TOPIC = "serp.pm.priority.events";
        public static final String AGGREGATE = "PRIORITY";

        public static class EventType {
            public static final String PRIORITY_CREATED = "PRIORITY_CREATED";
            public static final String PRIORITY_UPDATED = "PRIORITY_UPDATED";
            public static final String PRIORITY_DELETED = "PRIORITY_DELETED";
        }
    }
}
