package serp.project.school_bus_service.kernel.shared.base;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;

@RequiredArgsConstructor
public abstract class AbstractBaseController {

    private final AuthUtils authUtils;

    protected Long getCurrentTenantId() {
        return authUtils.getCurrentTenantIdOrThrow();
    }

    protected Long getCurrentUserId() {
        return authUtils.getCurrentUserIdOrThrow();
    }

    protected <T> ResponseEntity<GeneralResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(GeneralResponse.success(message, data));
    }

    protected ResponseEntity<GeneralResponse<Void>> ok(String message) {
        return ResponseEntity.ok(GeneralResponse.success(message));
    }

    protected <T> ResponseEntity<GeneralResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GeneralResponse.success(HttpStatus.CREATED, message, data));
    }
}
