package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.SchoolBusSyncCheckpointEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;
import java.util.Optional;

public interface SchoolBusSyncCheckpointRepository extends BaseRepository<SchoolBusSyncCheckpointEntity, Long> {

    Optional<SchoolBusSyncCheckpointEntity> findBySyncCodeAndIsDeletedFalse(String syncCode);

}
