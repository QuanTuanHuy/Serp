/**
 * Author: QuanTuanHuy
 * Description: Maps UserEntity to gRPC protobuf messages
 */

package serp.project.account.ui.grpc.mapper;

import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.UserEntity;
import serp.proto.account.UserProfileResponse;
import serp.proto.account.UserResponse;
import serp.proto.account.UsersResponse;
import serp.proto.common.PaginationResponse;

import java.util.List;

@Component
public class UserProtoMapper {

    public UserResponse toProto(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserResponse.Builder builder = UserResponse.newBuilder()
                .setId(entity.getId())
                .setIsActive(entity.isActive());

        if (entity.getEmail() != null) {
            builder.setEmail(entity.getEmail());
        }
        if (entity.getFirstName() != null) {
            builder.setFirstName(entity.getFirstName());
        }
        if (entity.getLastName() != null) {
            builder.setLastName(entity.getLastName());
        }
        builder.setFullName(entity.getFullName());

        if (entity.getAvatarUrl() != null) {
            builder.setAvatarUrl(entity.getAvatarUrl());
        }
        if (entity.getPhoneNumber() != null) {
            builder.setPhone(entity.getPhoneNumber());
        }
        if (entity.getPrimaryOrganizationId() != null) {
            builder.setTenantId(entity.getPrimaryOrganizationId());
        }
        if (entity.getCreatedAt() != null) {
            builder.setCreatedAt(entity.getCreatedAt());
        }
        if (entity.getUpdatedAt() != null) {
            builder.setUpdatedAt(entity.getUpdatedAt());
        }

        return builder.build();
    }

    public UsersResponse toUsersProto(List<UserEntity> entities) {
        UsersResponse.Builder builder = UsersResponse.newBuilder();

        if (entities != null) {
            entities.forEach(entity -> builder.addUsers(toProto(entity)));
        }

        return builder.build();
    }

    public UsersResponse toUsersProtoWithPagination(
            List<UserEntity> entities,
            long totalRecords,
            int currentPage,
            int pageSize) {

        UsersResponse.Builder builder = UsersResponse.newBuilder();

        if (entities != null) {
            entities.forEach(entity -> builder.addUsers(toProto(entity)));
        }

        int totalPages = pageSize > 0 ? (int) Math.ceil((double) totalRecords / pageSize) : 0;

        builder.setPagination(PaginationResponse.newBuilder()
                .setTotalRecords(totalRecords)
                .setCurrentPage(currentPage)
                .setTotalPages(totalPages)
                .setPageSize(pageSize)
                .build());

        return builder.build();
    }

    public UserProfileResponse toProfileProto(
            UserEntity entity,
            String organizationName,
            List<String> roles,
            List<String> permissions) {

        if (entity == null) {
            return null;
        }

        UserProfileResponse.Builder builder = UserProfileResponse.newBuilder()
                .setUser(toProto(entity));

        if (entity.getPrimaryOrganizationId() != null) {
            builder.setOrganizationId(entity.getPrimaryOrganizationId());
        }
        if (organizationName != null) {
            builder.setOrganizationName(organizationName);
        }
        if (roles != null) {
            builder.addAllRoles(roles);
        }
        if (permissions != null) {
            builder.addAllPermissions(permissions);
        }

        return builder.build();
    }
}
