package serp.project.pmcore.ui.rest.shared.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathConstants {
    public static final String API_BASE_PATH = "/api/v1";

    public static final String PROJECTS = API_BASE_PATH + "/projects";
    public static final String PROJECT_OPTIMIZATION_RUNS = PROJECTS + "/{projectId}/optimization-runs";
    public static final String SKILLS = API_BASE_PATH + "/skills";
    public static final String USERS = API_BASE_PATH + "/users";
    public static final String USER_SKILLS = USERS + "/{userId}/skills";
    public static final String USER_SKILLS_BATCH = USERS + "/skills";
    public static final String PROJECT_COMPONENTS = PROJECTS + "/{projectId}/components";
    public static final String PROJECT_PEOPLE = PROJECTS + "/{projectId}/people";
    public static final String PROJECT_CALENDAR = PROJECTS + "/{projectId}/calendar";
    public static final String PROJECT_CATEGORIES = API_BASE_PATH + "/project-categories";
    public static final String PROJECT_BLUEPRINTS = API_BASE_PATH + "/project-blueprints";
    public static final String WORKITEMS = API_BASE_PATH + "/projects/{projectId}/work-items";
    public static final String WORKITEM_SKILLS = WORKITEMS + "/{workItemId}/skills";
    public static final String TIMELINE_WORK_ITEMS = PROJECTS + "/{projectId}/timeline/work-items";
    public static final String ISSUE_LINKS = WORKITEMS + "/{workItemId}/links";
    public static final String WORKLOGS = WORKITEMS + "/{workItemId}/worklogs";
    public static final String PROJECT_ROLE_ACTORS = PROJECTS + "/{projectId}/roles/{roleId}/actors";
    public static final String ISSUE_LINK_TYPES = API_BASE_PATH + "/issue-link-types";
    public static final String ISSUE_TYPES = API_BASE_PATH + "/issue-types";
    public static final String ISSUE_TYPE_SETTINGS = API_BASE_PATH + "/issue-type-settings";
    public static final String RESOLUTIONS = API_BASE_PATH + "/resolutions";
    public static final String ISSUE_TYPE_SCHEMES = API_BASE_PATH + "/issue-type-schemes";
    public static final String PRIORITIES = API_BASE_PATH + "/priorities";
    public static final String PRIORITY_SCHEMES = API_BASE_PATH + "/priority-schemes";
    public static final String PRIORITY_SETTINGS = API_BASE_PATH + "/priority-settings";
    public static final String STATUS_CATEGORIES = API_BASE_PATH + "/status-categories";
    public static final String STATUSES = API_BASE_PATH + "/statuses";
    public static final String WORKFLOWS = API_BASE_PATH + "/workflows";
    public static final String WORKFLOW_SCHEMES = API_BASE_PATH + "/workflow-schemes";
    public static final String WORKFLOW_SETTINGS = API_BASE_PATH + "/workflow-settings";

    public static final String ROLES = API_BASE_PATH + "/roles";
}
