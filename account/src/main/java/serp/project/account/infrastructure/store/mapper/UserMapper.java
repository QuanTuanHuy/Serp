/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import serp.project.account.core.domain.dto.request.CreateKeycloakUserDto;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.domain.entity.RoleEntity;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.infrastructure.store.model.UserModel;

@Component
public class UserMapper extends BaseMapper {

    public UserEntity toEntity(UserModel model) {
        if (model == null) {
            return null;
        }

        return UserEntity.builder()
                .id(model.getId())
                .email(model.getEmail())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .phoneNumber(model.getPhoneNumber())
                .keycloakId(model.getKeycloakId())
                .isSuperAdmin(model.getIsSuperAdmin())
                .primaryOrganizationId(model.getPrimaryOrganizationId())
                .primaryDepartmentId(model.getPrimaryDepartmentId())
                .userType(model.getUserType())
                .status(model.getStatus())
                .lastLoginAt(model.getLastLoginAt())
                .avatarUrl(model.getAvatarUrl())
                .timezone(model.getTimezone())
                .preferredLanguage(model.getPreferredLanguage())
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public UserModel toModel(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserModel.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phoneNumber(entity.getPhoneNumber())
                .keycloakId(entity.getKeycloakId())
                .isSuperAdmin(entity.getIsSuperAdmin())
                .primaryOrganizationId(entity.getPrimaryOrganizationId())
                .primaryDepartmentId(entity.getPrimaryDepartmentId())
                .userType(entity.getUserType())
                .status(entity.getStatus())
                .lastLoginAt(entity.getLastLoginAt())
                .avatarUrl(entity.getAvatarUrl())
                .timezone(entity.getTimezone())
                .preferredLanguage(entity.getPreferredLanguage())
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }

    public List<UserEntity> toEntityList(List<UserModel> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public List<UserModel> toModelList(List<UserEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public UserEntity createUserMapper(CreateUserDto request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
    }

    public CreateKeycloakUserDto createUserMapper(UserEntity entity, Long organizationId, CreateUserDto request) {
        return CreateKeycloakUserDto.builder()
                .username(entity.getEmail())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .password(request.getPassword())
                .uid(entity.getId())
                .orgId(organizationId)
                .build();
    }

    public UserProfileResponse toProfileResponse(UserEntity user) {
        if (user == null) {
            return null;
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(user.getRoles().stream().map(RoleEntity::getName).toList())
                .build();
    }

    public UserEntity updateUserMapper(UserEntity existing, UserEntity update) {
        if (existing == null || update == null) {
            return existing;
        }

        if (update.getEmail() != null && !update.getEmail().trim().isEmpty()) {
            existing.setEmail(update.getEmail());
        }

        if (update.getFirstName() != null && !update.getFirstName().trim().isEmpty()) {
            existing.setFirstName(update.getFirstName());
        }

        if (update.getLastName() != null && !update.getLastName().trim().isEmpty()) {
            existing.setLastName(update.getLastName());
        }

        if (update.getPhoneNumber() != null && !update.getPhoneNumber().trim().isEmpty()) {
            existing.setPhoneNumber(update.getPhoneNumber());
        }

        if (update.getIsSuperAdmin() != null) {
            existing.setIsSuperAdmin(update.getIsSuperAdmin());
        }

        if (update.getPrimaryOrganizationId() != null) {
            existing.setPrimaryOrganizationId(update.getPrimaryOrganizationId());
        }

        if (update.getPrimaryDepartmentId() != null) {
            existing.setPrimaryDepartmentId(update.getPrimaryDepartmentId());
        }

        if (update.getUserType() != null) {
            existing.setUserType(update.getUserType());
        }

        if (update.getStatus() != null) {
            existing.setStatus(update.getStatus());
        }

        if (update.getLastLoginAt() != null) {
            existing.setLastLoginAt(update.getLastLoginAt());
        }

        if (update.getAvatarUrl() != null && !update.getAvatarUrl().trim().isEmpty()) {
            existing.setAvatarUrl(update.getAvatarUrl());
        }

        if (update.getTimezone() != null && !update.getTimezone().trim().isEmpty()) {
            existing.setTimezone(update.getTimezone());
        }

        if (update.getPreferredLanguage() != null && !update.getPreferredLanguage().trim().isEmpty()) {
            existing.setPreferredLanguage(update.getPreferredLanguage());
        }

        if (update.getUpdatedAt() != null) {
            existing.setUpdatedAt(update.getUpdatedAt());
        }

        return existing;
    }
}
