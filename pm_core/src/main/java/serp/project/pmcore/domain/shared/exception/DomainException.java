package serp.project.pmcore.domain.shared.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

    private final DomainErrorCode errorCode;

    public DomainException(DomainErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public DomainException(DomainErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
    }

}
