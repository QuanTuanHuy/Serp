package serp.project.pmcore.domain.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class DomainValidationException extends DomainException {

    private final List<String> violations;

    public DomainValidationException(DomainErrorCode errorCode) {
        super(errorCode);
        this.violations = List.of();
    }

    public DomainValidationException(DomainErrorCode errorCode, String detail) {
        super(errorCode, detail);
        this.violations = List.of(detail);
    }

    public DomainValidationException(DomainErrorCode errorCode, List<String> violations) {
        super(errorCode, String.join("; ", violations));
        this.violations = List.copyOf(violations);
    }

    public static DomainValidationException schemeIncompatible(List<Long> uncoveredIssueTypeIds) {
        List<String> violations = uncoveredIssueTypeIds.stream()
                .map(id -> "Issue type " + id + " has no workflow mapping")
                .toList();
        return new DomainValidationException(
                DomainErrorCode.SCHEME_INCOMPATIBLE, violations);
    }

    public static DomainValidationException workflowValidation(List<String> errors) {
        return new DomainValidationException(
                DomainErrorCode.WORKFLOW_VALIDATION_FAILED, errors);
    }
}
