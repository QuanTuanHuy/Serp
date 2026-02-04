/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - gRPC Client to call Account Service
 */

package serp.project.discuss_service.infrastructure.grpc;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import serp.project.discuss_service.core.domain.dto.response.ChannelMemberResponse.UserInfo;
import serp.project.discuss_service.core.port.client.IAccountServiceClient;
import serp.proto.account.GetUserByIdRequest;
import serp.proto.account.GetUsersByTenantRequest;
import serp.proto.account.InternalAccountServiceGrpc;
import serp.proto.account.UserResponse;
import serp.proto.account.UsersResponse;
import serp.proto.common.PaginationRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(name = "app.grpc.enabled", havingValue = "true")
@Slf4j
public class AccountGrpcClientAdapter implements IAccountServiceClient {

    @GrpcClient("account-service")
    private InternalAccountServiceGrpc.InternalAccountServiceBlockingStub accountStub;

    @Override
    public Optional<UserInfo> getUserById(Long userId) {
        if (userId == null) {
            log.warn("[gRPC] getUserById called with null userId");
            return Optional.empty();
        }

        try {
            log.debug("[gRPC] Fetching user by ID: {}", userId);

            GetUserByIdRequest request = GetUserByIdRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            UserResponse response = accountStub.getUserById(request);

            if (response.getId() == 0) {
                log.warn("[gRPC] User not found for ID: {}", userId);
                return Optional.empty();
            }

            return Optional.of(mapToUserInfo(response));

        } catch (StatusRuntimeException e) {
            log.error("[gRPC] Error fetching user {}: {} - {}", 
                    userId, e.getStatus().getCode(), e.getStatus().getDescription());
            return Optional.empty();
        } catch (Exception e) {
            log.error("[gRPC] Unexpected error fetching user {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<UserInfo> getUsersForTenant(Long tenantId, String query) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID must not be null");
        }

        try {
            log.debug("[gRPC] Fetching users for tenant: {}, query: {}", tenantId, query);

            PaginationRequest pagination = PaginationRequest.newBuilder()
                    .setPage(0)
                    .setPageSize(50)
                    .build();

            GetUsersByTenantRequest request = GetUsersByTenantRequest.newBuilder()
                    .setTenantId(tenantId)
                    .setQuery(query)
                    .setPagination(pagination)
                    .build();

            UsersResponse response = accountStub.getUsersByTenant(request);
            List<UserInfo> users = response.getUsersList().stream()
                    .map(this::mapToUserInfo)
                    .toList();

            if (users.isEmpty()) {
                log.info("[gRPC] No users found for tenant: {}", tenantId);
                return Collections.emptyList();
            }

            log.debug("[gRPC] Found {} users for tenant: {}", users.size(), tenantId);
            return users;

        } catch (StatusRuntimeException e) {
            log.error("[gRPC] Error fetching users for tenant {}: {} - {}",
                    tenantId, e.getStatus().getCode(), e.getStatus().getDescription());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[gRPC] Unexpected error fetching users for tenant {}: {}", tenantId, e.getMessage());
            return Collections.emptyList();
        }
    }


    private UserInfo mapToUserInfo(UserResponse proto) {
        return UserInfo.builder()
                .id(proto.getId())
                .name(proto.getFullName().isEmpty() ? 
                      buildFullName(proto.getFirstName(), proto.getLastName()) : 
                      proto.getFullName())
                .email(proto.getEmail())
                .avatarUrl(proto.getAvatarUrl().isEmpty() ? null : proto.getAvatarUrl())
                .build();
    }

    private String buildFullName(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty()) {
            return lastName != null ? lastName : "";
        }
        if (lastName == null || lastName.isEmpty()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
