package serp.project.pmcore.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RestControllerConstants {
    public static final String API_BASE_PATH = "/api/v1";
    
    public static final String PROJECTS = API_BASE_PATH + "/projects";
    public static final String WORKITEMS = API_BASE_PATH + "/workitems";
    public static final String ONBOARDING = API_BASE_PATH + "/onboarding";
}
