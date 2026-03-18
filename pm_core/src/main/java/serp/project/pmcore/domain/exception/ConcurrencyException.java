package serp.project.pmcore.domain.exception;

public class ConcurrencyException extends DomainException {

    public ConcurrencyException(DomainErrorCode errorCode) {
        super(errorCode);
    }

    public ConcurrencyException(DomainErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public static ConcurrencyException project(Long id) {
        return new ConcurrencyException(DomainErrorCode.CONCURRENT_MODIFICATION,
                "Project was modified by another request: id=" + id);
    }
}
