/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.mapper;

import org.springframework.stereotype.Component;
import serp.project.account.core.domain.entity.PasswordResetRequestEntity;
import serp.project.account.infrastructure.store.model.PasswordResetRequestModel;

@Component
public class PasswordResetRequestMapper extends BaseMapper {
    public PasswordResetRequestEntity toEntity(PasswordResetRequestModel model) {
        if (model == null) {
            return null;
        }

        return PasswordResetRequestEntity.builder()
                .id(model.getId())
                .userId(model.getUserId())
                .organizationId(model.getOrganizationId())
                .email(model.getEmail())
                .tokenHash(model.getTokenHash())
                .status(model.getStatus())
                .requestedBy(model.getRequestedBy())
                .expiresAt(localDateTimeToLong(model.getExpiresAt()))
                .usedAt(localDateTimeToLong(model.getUsedAt()))
                .createdAt(localDateTimeToLong(model.getCreatedAt()))
                .updatedAt(localDateTimeToLong(model.getUpdatedAt()))
                .build();
    }

    public PasswordResetRequestModel toModel(PasswordResetRequestEntity entity) {
        if (entity == null) {
            return null;
        }

        return PasswordResetRequestModel.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .organizationId(entity.getOrganizationId())
                .email(entity.getEmail())
                .tokenHash(entity.getTokenHash())
                .status(entity.getStatus())
                .requestedBy(entity.getRequestedBy())
                .expiresAt(longToLocalDateTime(entity.getExpiresAt()))
                .usedAt(longToLocalDateTime(entity.getUsedAt()))
                .createdAt(longToLocalDateTime(entity.getCreatedAt()))
                .updatedAt(longToLocalDateTime(entity.getUpdatedAt()))
                .build();
    }
}
