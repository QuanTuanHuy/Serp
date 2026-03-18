package serp.project.pmcore.domain.exception;

public class BusinessRuleViolationException extends DomainException {

    public BusinessRuleViolationException(DomainErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessRuleViolationException(DomainErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}