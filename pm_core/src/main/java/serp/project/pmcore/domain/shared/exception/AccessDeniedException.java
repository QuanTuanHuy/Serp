package serp.project.pmcore.domain.shared.exception;

public class AccessDeniedException extends DomainException {

    public AccessDeniedException(DomainErrorCode errorCode) {
        super(errorCode);
    }

    public AccessDeniedException(DomainErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public static AccessDeniedException securityLevel(Long workItemId) {
        return new AccessDeniedException(
                DomainErrorCode.SECURITY_LEVEL_ACCESS_DENIED,
                "Insufficient security level to access work item: id=" + workItemId);
    }

    public static AccessDeniedException transition(String transitionName) {
        return new AccessDeniedException(
                DomainErrorCode.TRANSITION_CONDITION_FAILED,
                "Transition condition not met: " + transitionName);
    }

    public static AccessDeniedException projectPermission(String permissionKey, Long projectId) {
        return new AccessDeniedException(
                DomainErrorCode.PROJECT_PERMISSION_DENIED,
                "Project permission denied: permission=" + permissionKey + ", projectId=" + projectId);
    }
}
