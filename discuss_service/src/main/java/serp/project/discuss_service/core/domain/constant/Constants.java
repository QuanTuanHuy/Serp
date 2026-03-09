/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.discuss_service.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String SERVICE_NAME = "serp-discuss";

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

        public static final String NO_PERMISSION_TO_ACCESS_ORGANIZATION = "You don't have permission to access this organization";
    }

    @UtilityClass
    public static class Security {
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String SERP_SERVICES_ROLE = "SERP_SERVICES";
    }

}
