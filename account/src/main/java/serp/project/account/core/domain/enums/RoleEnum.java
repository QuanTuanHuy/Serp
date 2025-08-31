package serp.project.account.core.domain.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    USER("USER"),
    ADMIN("ADMIN");

    private final String role;

    RoleEnum(String role) {
        this.role = role;
    }
}
