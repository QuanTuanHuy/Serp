/**
 * Author: QuanTuanHuy
 * Description: Internal Account gRPC Service implementation with mTLS authentication
 */

package serp.project.account.ui.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.util.Pair;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.OrganizationEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.service.IOrganizationService;
import serp.project.account.core.service.IUserService;
import serp.project.account.ui.grpc.interceptor.MtlsAuthInterceptor;
import serp.project.account.ui.grpc.mapper.UserProtoMapper;
import serp.proto.account.*;

import java.util.List;


@GrpcService
@RequiredArgsConstructor
@Slf4j
public class InternalAccountGrpcService extends InternalAccountServiceGrpc.InternalAccountServiceImplBase {

    private final IUserService userService;
    private final IOrganizationService organizationService;
    private final UserProtoMapper userProtoMapper;

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        String callerService = MtlsAuthInterceptor.SERVICE_NAME_KEY.get();
        log.info("gRPC GetUserById called by {} for userId={}", callerService, request.getUserId());

        try {
            UserEntity user = userService.getUserById(request.getUserId());

            if (user == null) {
                log.warn("User not found: id={}", request.getUserId());
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("User not found: " + request.getUserId())
                                .asRuntimeException()
                );
                return;
            }

            UserResponse response = userProtoMapper.toProto(user);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.debug("Successfully returned user: id={}, email={}", user.getId(), user.getEmail());

        } catch (Exception e) {
            log.error("Error fetching user by id={}: {}", request.getUserId(), e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal error: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getUsersByIds(GetUsersByIdsRequest request, StreamObserver<UsersResponse> responseObserver) {
        String callerService = MtlsAuthInterceptor.SERVICE_NAME_KEY.get();
        List<Long> userIds = request.getUserIdsList();
        log.info("gRPC GetUsersByIds called by {} for {} users", callerService, userIds.size());

        try {
            if (userIds.isEmpty()) {
                responseObserver.onNext(UsersResponse.newBuilder().build());
                responseObserver.onCompleted();
                return;
            }

            List<UserEntity> users = userService.getUsersByIds(userIds);
            UsersResponse response = userProtoMapper.toUsersProto(users);

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.debug("Successfully returned {} users", users.size());

        } catch (Exception e) {
            log.error("Error batch fetching users: {}", e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal error: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getUserProfile(GetUserProfileRequest request, StreamObserver<UserProfileResponse> responseObserver) {
        String callerService = MtlsAuthInterceptor.SERVICE_NAME_KEY.get();
        log.info("gRPC GetUserProfile called by {} for userId={}", callerService, request.getUserId());

        try {
            UserEntity user = userService.getUserById(request.getUserId());

            if (user == null) {
                log.warn("User not found for profile: id={}", request.getUserId());
                responseObserver.onError(
                        Status.NOT_FOUND
                                .withDescription("User not found: " + request.getUserId())
                                .asRuntimeException()
                );
                return;
            }

            String organizationName = null;
            if (user.getPrimaryOrganizationId() != null) {
                OrganizationEntity org = organizationService.getOrganizationById(user.getPrimaryOrganizationId());
                if (org != null) {
                    organizationName = org.getName();
                }
            }

            List<String> roleNames = user.getRoleNames();

            UserProfileResponse response = userProtoMapper.toProfileProto(
                    user,
                    organizationName,
                    roleNames,
                    List.of() // permissions - can be expanded later
            );

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.debug("Successfully returned profile for user: id={}", user.getId());

        } catch (Exception e) {
            log.error("Error fetching user profile for id={}: {}", request.getUserId(), e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal error: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void userExists(UserExistsRequest request, StreamObserver<UserExistsResponse> responseObserver) {
        String callerService = MtlsAuthInterceptor.SERVICE_NAME_KEY.get();
        log.debug("gRPC UserExists called by {} for userId={}", callerService, request.getUserId());

        try {
            UserEntity user = userService.getUserById(request.getUserId());
            boolean exists = user != null;

            UserExistsResponse response = UserExistsResponse.newBuilder()
                    .setExists(exists)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error checking user existence for id={}: {}", request.getUserId(), e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal error: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void getUsersByTenant(GetUsersByTenantRequest request, StreamObserver<UsersResponse> responseObserver) {
        String callerService = MtlsAuthInterceptor.SERVICE_NAME_KEY.get();
        log.info("gRPC GetUsersByTenant called by {} for tenantId={}", callerService, request.getTenantId());

        try {
            int page = request.hasPagination() ? request.getPagination().getPage() : 0;
            int pageSize = request.hasPagination() ? request.getPagination().getPageSize() : 100;
            GetUserParams params = GetUserParams.builder()
                    .organizationId(request.getTenantId())
                    .status(UserStatus.ACTIVE.toString())
                    .page(page)
                    .pageSize(pageSize)
                    .build();
            Pair<Long, List<UserEntity>> result = userService.getUsers(params);

            UsersResponse response = userProtoMapper.toUsersProtoWithPagination(
                    result.getSecond(),
                    result.getFirst(),
                    page,
                    pageSize
            );

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.debug("Successfully returned {} users for tenant={}", pageSize, request.getTenantId());

        } catch (Exception e) {
            log.error("Error fetching users by tenant={}: {}", request.getTenantId(), e.getMessage(), e);
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal error: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }
}
