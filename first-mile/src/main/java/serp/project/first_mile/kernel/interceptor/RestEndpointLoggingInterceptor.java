/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.kernel.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RestEndpointLoggingInterceptor implements HandlerInterceptor {
    private static final String REQUEST_START_TIME = "REQUEST_START_TIME";

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        log.info("Enter REST endpoint: {} {}", request.getMethod(), buildPathWithQuery(request));
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex
    ) {
        long durationMs = calculateDurationMs(request);
        String path = buildPathWithQuery(request);

        if (ex != null) {
            log.info(
                    "Exit REST endpoint: {} {} - status: {} - durationMs: {} - exception: {}",
                    request.getMethod(),
                    path,
                    response.getStatus(),
                    durationMs,
                    ex.getClass().getSimpleName()
            );
            return;
        }

        log.info(
                "Exit REST endpoint: {} {} - status: {} - durationMs: {}",
                request.getMethod(),
                path,
                response.getStatus(),
                durationMs
        );
    }

    private static long calculateDurationMs(HttpServletRequest request) {
        Object startTime = request.getAttribute(REQUEST_START_TIME);
        if (startTime instanceof Long start) {
            return System.currentTimeMillis() - start;
        }
        return -1L;
    }

    private static String buildPathWithQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + query;
    }
}
