/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.domain.enums;

import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

@Getter
public enum ExternalServices {
    ACCOUNT("Serp Account Service", "serp-account"),
    ;

    private final String name;
    private final String clientId;

    ExternalServices(String name, String clientId) {
        this.name = name;
        this.clientId = clientId;
    }

    public static List<String> getAllClientIds() {
        return Stream.of(ExternalServices.values()).map(ExternalServices::getClientId).toList();
    }

    public static boolean isValidClientId(String clientId) {
        return getAllClientIds().contains(clientId);

    }
}
