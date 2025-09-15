package serp.project.account.core.domain.enums;

import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum ExternalServices {
    PTM("Personal Task Management", "serp-ptm"),
    CRM("Customer Relationship Management", "serp-crm"),
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
