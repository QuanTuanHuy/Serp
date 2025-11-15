/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String SERVICE_NAME = "serp-account";

    @UtilityClass
    public static class HttpStatus {
        public static final String ERROR = "error";
        public static final String SUCCESS = "success";
    }

    @UtilityClass
    public static class HttpStatusCode {
        public static final int SUCCESS = 200;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    @UtilityClass
    public static class ErrorMessage {
        public static final String OK = "OK";
        public static final String NOT_FOUND = "Not Found";
        public static final String UNAUTHORIZED = "Unauthorized";
        public static final String FORBIDDEN = "Forbidden";
        public static final String BAD_REQUEST = "Bad Request";
        public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
        public static final String CONFLICT = "Conflict";
        public static final String TOO_MANY_REQUESTS = "Too Many Requests";
        public static final String UNKNOWN_ERROR = "Unknown Error";

        public static final String MODULE_ALREADY_EXISTS = "Module already exists";
        public static final String MODULE_NOT_FOUND = "Module not found";

        public static final String USER_MODULE_ACCESS_NOT_FOUND = "User module access not found";

        public static final String CLIENT_NOT_FOUND = "Client not found";

        public static final String ORGANIZATION_ALREADY_EXISTS = "Organization already exists";

        public static final String PERMISSION_ALREADY_EXISTS = "Permission already exists";
        public static final String ONE_OR_MORE_PERMISSIONS_NOT_FOUND = "One or more permissions not found";

        public static final String ROLE_ALREADY_EXISTS = "Role already exists";
        public static final String ROLE_NOT_FOUND = "Role not found";
        public static final String INVALID_ROLE_SCOPE = "Invalid role scope";
        public static final String INVALID_ROLE_TYPE = "Invalid role type";
        public static final String NO_ROLES_FOUND_FOR_MODULE = "No roles found for module";

        public static final String GROUP_ALREADY_EXISTS = "Group already exists";
        public static final String GROUP_NOT_FOUND = "Group not found";
        public static final String CREATE_GROUP_KEYCLOAK_FAILED = "Create group in Keycloak failed";

        public static final String USER_ALREADY_EXISTS = "User already exists";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String CREATE_USER_FAILED = "Create user failed";
        public static final String USER_INACTIVE = "User is inactive";
        public static final String USER_NOT_IN_ORGANIZATION = "User does not belong to the organization";

        public static final String WRONG_EMAIL_OR_PASSWORD = "Wrong email or password";
        public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
        public static final String TOKEN_EXPIRED = "Token expired";

        public static final String INVALID_MENU_TYPE = "Invalid menu type";
        public static final String MENU_DISPLAY_ALREADY_EXISTS = "Menu display already exists";
        public static final String MENU_DISPLAY_NOT_FOUND = "Menu display not found";
        public static final String PARENT_MENU_DISPLAY_NOT_FOUND = "Parent menu display not found";
        public static final String CREATE_MENU_DISPLAY_FAILED = "Create menu display failed";
        public static final String UPDATE_MENU_DISPLAY_FAILED = "Update menu display failed";
        public static final String DELETE_MENU_DISPLAY_FAILED = "Delete menu display failed";
        public static final String GET_MENU_DISPLAY_FAILED = "Get menu display failed";
        public static final String ASSIGN_MENU_DISPLAY_FAILED = "Assign menu display failed";
        public static final String UNASSIGN_MENU_DISPLAY_FAILED = "Unassign menu display failed";
        public static final String ROLE_CANNOT_ASSIGN_MENU_DISPLAYS = "This role cannot be assigned menu displays";
        public static final String NO_VALID_MENU_DISPLAYS_TO_ASSIGN = "No valid menu displays to assign to the role";

        // Organization errors
        public static final String ORGANIZATION_NOT_FOUND = "Organization not found";

        // Subscription Plan errors
        public static final String SUBSCRIPTION_PLAN_NOT_FOUND = "Subscription plan not found";
        public static final String SUBSCRIPTION_PLAN_CODE_ALREADY_EXISTS = "Subscription plan code already exists";
        public static final String MODULE_ALREADY_IN_PLAN = "Module already in plan";
        public static final String MODULE_NOT_IN_PLAN = "Module not in plan";

        // Organization Subscription errors
        public static final String ORGANIZATION_ALREADY_HAS_ACTIVE_SUBSCRIPTION = "Organization already has active subscription";
        public static final String PLAN_DOES_NOT_SUPPORT_TRIAL = "Plan does not support trial";
        public static final String NEW_PLAN_MUST_BE_HIGHER_THAN_CURRENT = "New plan must be higher than current";
        public static final String NEW_PLAN_MUST_BE_LOWER_THAN_CURRENT = "New plan must be lower than current";
        public static final String SUBSCRIPTION_NOT_EXPIRED = "Subscription not expired";
        public static final String SUBSCRIPTION_NOT_PENDING_APPROVAL = "Subscription not pending approval";
        public static final String SUBSCRIPTION_NOT_IN_TRIAL = "Subscription not in trial";
        public static final String SUBSCRIPTION_CANNOT_BE_UPGRADED = "Subscription cannot be upgraded";
        public static final String SUBSCRIPTION_ALREADY_ACTIVE = "Subscription already active";
        public static final String ACTIVE_SUBSCRIPTION_NOT_FOUND = "Active subscription not found";
        public static final String SUBSCRIPTION_NOT_FOUND = "Subscription not found";
        public static final String NO_ACTIVE_SUBSCRIPTION = "No active subscription";
        public static final String PLAN_NOT_ACTIVE = "Plan not active";
        public static final String NO_NEW_MODULES_TO_ADD = "No new modules to add to the subscription plan";

        // Module Access errors
        public static final String ORGANIZATION_CANNOT_ACCESS_MODULE = "Organization does not have access to this module";
        public static final String ORGANIZATION_CANNOT_ACCESS_MODULE_ID = "Organization does not have access to the module %d";
        public static final String MODULE_NOT_AVAILABLE = "Module is not available";
        public static final String USER_ALREADY_HAS_MODULE_ACCESS = "User already has access to this module";
        public static final String MODULE_NOT_IN_SUBSCRIPTION_PLAN = "Module not found in subscription plan";
        public static final String MAX_USERS_LIMIT_REACHED = "Maximum users limit reached for this module";
        public static final String NO_PERMISSION_TO_ACCESS_ORGANIZATION = "You don't have permission to access this organization";

        // Department errors
        public static final String DEPARTMENT_NOT_FOUND = "Department not found";
        public static final String DEPARTMENT_INACTIVE = "Department is inactive";
        public static final String DEPARTMENT_ALREADY_EXISTS = "Department already exists";
        public static final String CIRCULAR_PARENT_RELATIONSHIP = "Circular parent relationship detected";
        public static final String MANAGER_NOT_IN_ORGANIZATION = "Manager must belong to the same organization";
        public static final String PARENT_DEPARTMENT_NOT_IN_ORGANIZATION = "Parent department must belong to the same organization";
        public static final String DEPARTMENT_CANNOT_BE_OWN_PARENT = "Department cannot be its own parent";
        public static final String USER_ALREADY_IN_DEPARTMENT = "User already assigned to this department";
        public static final String USER_NOT_IN_DEPARTMENT = "User not found in department";
        public static final String PRIMARY_DEPARTMENT_CONFLICT = "Primary department conflict";
        public static final String MAX_DEPARTMENT_DEPTH_EXCEEDED = "Maximum department depth exceeded";
    }

    @UtilityClass
    public static class Security {
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String SERP_SERVICES_ROLE = "SERP_SERVICES";
    }

}
