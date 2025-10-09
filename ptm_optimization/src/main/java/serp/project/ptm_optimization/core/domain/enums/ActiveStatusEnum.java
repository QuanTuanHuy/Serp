package serp.project.ptm_optimization.core.domain.enums;

import lombok.Getter;

@Getter
public enum ActiveStatusEnum {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");
    
    private final String value;

    ActiveStatusEnum(String value) {
        this.value = value;
    }
}
