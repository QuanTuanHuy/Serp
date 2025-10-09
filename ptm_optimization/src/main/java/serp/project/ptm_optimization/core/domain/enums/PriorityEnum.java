package serp.project.ptm_optimization.core.domain.enums;

import lombok.Getter;

@Getter
public enum PriorityEnum {
    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH");

    private final String value;

    PriorityEnum(String value) {
        this.value = value;
    }
}
