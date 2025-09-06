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

        public static final String CLIENT_NOT_FOUND = "Client not found";

        public static final String PERMISSION_ALREADY_EXISTS = "Permission already exists";
        public static final String ONE_OR_MORE_PERMISSIONS_NOT_FOUND = "One or more permissions not found";

        public static final String ROLE_ALREADY_EXISTS = "Role already exists";

        public static final String USER_ALREADY_EXISTS = "User already exists";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String CREATE_USER_FAILED = "Create user failed";

        public static final String WRONG_EMAIL_OR_PASSWORD = "Wrong email or password";
    }

    @UtilityClass
    public static class Security {
        public static final String ROLE_PREFIX = "ROLE_";
    }

}
