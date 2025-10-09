package serp.project.ptm_optimization.core.domain.enums;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {
    TODO("TODO"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE"),
    PENDING("PENDING"),
    ARCHIVED("ARCHIVED");
    
    private final String value;

    TaskStatusEnum(String value) {
        this.value = value;
    }
}
