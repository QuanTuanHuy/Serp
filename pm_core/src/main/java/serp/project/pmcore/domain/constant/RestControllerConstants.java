package serp.project.pmcore.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RestControllerConstants {
    public static final String API_BASE_PATH = "/api/v1";
    
    public static final String PROJECTS = API_BASE_PATH + "/projects";
    public static final String WORKITEMS = API_BASE_PATH + "/projects/{projectId}/workitems";
    public static final String PROJECT_ROLE_ACTORS = PROJECTS + "/{projectId}/roles/{roleId}/actors";
    public static final String ONBOARDING = API_BASE_PATH + "/onboarding";
}
