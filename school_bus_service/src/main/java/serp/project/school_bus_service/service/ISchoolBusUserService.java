package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.request.SchoolBusUserUpsertCommand;
import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import serp.project.school_bus_service.shared.base.IBaseService;
import java.util.Optional;

public interface ISchoolBusUserService extends IBaseService<SchoolBusUserEntity, Long> {

    SchoolBusUserEntity upsertFromAccountUser(SchoolBusUserUpsertCommand command);

    Optional<SchoolBusUserEntity> findByAccountUserId(Long accountUserId);

    Optional<SchoolBusUserEntity> findByKeycloakId(String keycloakId);

    Optional<SchoolBusUserEntity> findByTenantIdAndEmail(Long tenantId, String email);

    SchoolBusUserEntity getRequiredByAccountUserId(Long accountUserId);

    SchoolBusUserEntity getRequiredByKeycloakId(String keycloakId);

}
