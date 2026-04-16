package serp.project.pmcore.ui.rest.shared.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathConstants {
    public static final String API_BASE_PATH = "/api/v1";

    public static final String PROJECTS = API_BASE_PATH + "/projects";
    public static final String PROJECT_CATEGORIES = API_BASE_PATH + "/project-categories";
    public static final String WORKITEMS = API_BASE_PATH + "/projects/{projectId}/work-items";
    public static final String WORKLOGS = WORKITEMS + "/{workItemId}/worklogs";
    public static final String PROJECT_ROLE_ACTORS = PROJECTS + "/{projectId}/roles/{roleId}/actors";
    public static final String ISSUE_TYPES = API_BASE_PATH + "/issue-types";
    public static final String PRIORITIES = API_BASE_PATH + "/priorities";

    public static final String ROLES = API_BASE_PATH + "/roles";
}
