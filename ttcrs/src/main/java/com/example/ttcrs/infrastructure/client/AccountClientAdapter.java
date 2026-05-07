package com.example.ttcrs.infrastructure.client;

import com.example.ttcrs.infrastructure.client.dto.AccountUserDTO;
import com.example.ttcrs.infrastructure.client.dto.GeneralResponse;
import com.example.ttcrs.kernel.property.AppProperties;
import com.example.ttcrs.kernel.property.ExternalServiceProperties;
import com.example.ttcrs.kernel.utils.HttpClientHelper;
import com.example.ttcrs.kernel.utils.TokenUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountClientAdapter {

    private static final String ACCOUNT_SERVICE = "account-service";
    private static final String USERS_PATH = "/internal/api/v1/users";

    private final TokenUtils tokenUtils;
    private final HttpClientHelper httpClientHelper;
    private final ExternalServiceProperties serviceProperties;
    private final AppProperties appProperties;
    private final Gson gson;

    /**
     * Returns all users with the TTCRS_DRIVER role belonging to the given tenant.
     */
    public List<AccountUserDTO> getDriverUsers(Long tenantId) {
        try {
            String token = tokenUtils.getServiceToken();
            String baseUrl = serviceProperties.getServiceUrlByName(ACCOUNT_SERVICE);

            var params = new LinkedMultiValueMap<String, String>();
            params.add("roleId", String.valueOf(appProperties.getDriverRoleId()));
            params.add("organizationId", String.valueOf(tenantId));
            params.add("pageSize", "1000");
            params.add("status", "ACTIVE");

            GeneralResponse response = httpClientHelper
                    .get(baseUrl + USERS_PATH, params,
                            Map.of("Authorization", "Bearer " + token),
                            GeneralResponse.class)
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                return parseUserList(response.getData());
            }
            return List.of();
        } catch (Exception e) {
            log.error("[AccountClientAdapter] Failed to fetch driver users for tenant={}: {}", tenantId, e.getMessage());
            return List.of();
        }
    }

    /**
     * Returns a single user by ID.
     */
    public AccountUserDTO getUserById(Long userId) {
        try {
            String token = tokenUtils.getServiceToken();
            String url = serviceProperties.getServiceUrlByName(ACCOUNT_SERVICE) + USERS_PATH + "/" + userId;

            GeneralResponse response = httpClientHelper
                    .get(url, null,
                            Map.of("Authorization", "Bearer " + token),
                            GeneralResponse.class)
                    .block();

            if (response != null && response.isSuccess() && response.getData() != null) {
                String json = gson.toJson(response.getData());
                return gson.fromJson(json, AccountUserDTO.class);
            }
            return null;
        } catch (Exception e) {
            log.error("[AccountClientAdapter] Failed to fetch user id={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private List<AccountUserDTO> parseUserList(Object data) {
        JsonElement element = gson.toJsonTree(data);
        try {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                JsonElement itemsEl = obj.get("items");
                if (itemsEl != null && itemsEl.isJsonArray()) {
                    return gson.fromJson(itemsEl, new TypeToken<List<AccountUserDTO>>() {}.getType());
                }
            } else if (element.isJsonArray()) {
                return gson.fromJson(element, new TypeToken<List<AccountUserDTO>>() {}.getType());
            }
        } catch (Exception e) {
            log.warn("[AccountClientAdapter] Could not parse user list response: {}", e.getMessage());
        }
        return List.of();
    }
}
