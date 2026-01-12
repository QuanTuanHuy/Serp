/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Attachment scan status enumeration
 */

package serp.project.discuss_service.core.domain.enums;

import lombok.Getter;

/**
 * Represents the virus scan status of an attachment.
 */
@Getter
public enum ScanStatus {
    PENDING("PENDING", "Pending Scan"),
    CLEAN("CLEAN", "Clean"),
    INFECTED("INFECTED", "Infected"),
    ERROR("ERROR", "Scan Error");

    private final String code;
    private final String displayName;

    ScanStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public static ScanStatus fromCode(String code) {
        for (ScanStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown scan status: " + code);
    }

    public boolean canDownload() {
        return this == CLEAN;
    }

    public boolean isComplete() {
        return this == CLEAN || this == INFECTED || this == ERROR;
    }
}
