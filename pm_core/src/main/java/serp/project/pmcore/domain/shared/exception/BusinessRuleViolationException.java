package serp.project.pmcore.domain.shared.exception;

public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(DomainErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessRuleViolationException(DomainErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}