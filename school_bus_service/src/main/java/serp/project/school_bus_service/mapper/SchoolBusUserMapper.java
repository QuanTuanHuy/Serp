package serp.project.school_bus_service.mapper;

import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.request.SchoolBusUserUpsertCommand;
import serp.project.school_bus_service.dto.response.SchoolBusUserResponse;
import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import serp.project.school_bus_service.shared.base.BaseMapper;

@Component
public class SchoolBusUserMapper extends BaseMapper {

    public SchoolBusUserResponse toResponse(SchoolBusUserEntity entity) {
        if (entity == null) {
            return null;
        }
        SchoolBusUserResponse response = enrich(new SchoolBusUserResponse(), entity);
        response.setAccountUserId(entity.getAccountUserId());
        response.setKeycloakId(entity.getKeycloakId());
        response.setEmail(entity.getEmail());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setFullName(entity.getFullName());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setAvatarUrl(entity.getAvatarUrl());
        response.setStatus(entity.getStatus());
        response.setUserType(entity.getUserType());
        response.setLastSyncedAt(entity.getLastSyncedAt());
        response.setSyncSource(entity.getSyncSource());
        return response;
    }

    public void applyUpsertCommand(SchoolBusUserEntity entity, SchoolBusUserUpsertCommand command) {
        if (command == null || entity == null) {
            return;
        }
        entity.setAccountUserId(command.getAccountUserId());
        entity.setKeycloakId(command.getKeycloakId());
        entity.setEmail(command.getEmail());
        entity.setFirstName(command.getFirstName());
        entity.setLastName(command.getLastName());
        
        // Build fullName
        String firstName = command.getFirstName() == null ? "" : command.getFirstName().trim();
        String lastName = command.getLastName() == null ? "" : command.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        entity.setFullName(fullName.isEmpty() ? null : fullName);

        entity.setPhoneNumber(command.getPhoneNumber());
        entity.setAvatarUrl(command.getAvatarUrl());
        entity.setPrimaryOrganizationId(command.getPrimaryOrganizationId());
        entity.setPreferredLanguage(command.getPreferredLanguage());
        entity.setTimezone(command.getTimezone());
        entity.setUserType(command.getUserType());
        entity.setStatus(command.getStatus());
        entity.setSyncSource(command.getSyncSource());
        entity.setRawPayloadJson(command.getRawPayloadJson());
    }

}
