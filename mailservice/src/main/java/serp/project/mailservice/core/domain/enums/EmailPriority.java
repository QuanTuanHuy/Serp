/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.enums;

public enum EmailPriority {
    HIGH(3),
    NORMAL(2),
    LOW(1);

    private final int level;

    EmailPriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isHigherThan(EmailPriority other) {
        return this.level > other.level;
    }

    public boolean isHighPriority() {
        return this == HIGH;
    }
}
