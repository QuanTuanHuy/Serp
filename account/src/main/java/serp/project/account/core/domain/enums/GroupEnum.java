package serp.project.account.core.domain.enums;

import lombok.Getter;

@Getter
public enum GroupEnum {
    DEFAULT_SERP_GROUP("DEFAULT_SERP_GROUP", "Default serp group"),
    ;

    private final String name;
    private final String description;

    GroupEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
