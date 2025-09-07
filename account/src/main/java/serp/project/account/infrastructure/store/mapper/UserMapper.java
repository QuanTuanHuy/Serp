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

    public CreateKeycloakUserDto createUserMapper(UserEntity entity, CreateUserDto request) {
        return CreateKeycloakUserDto.builder()
                .username(entity.getEmail())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .password(request.getPassword())
                .uid(entity.getId())
                .build();
    }
}
