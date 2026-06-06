package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import serp.project.school_bus_service.entity.SchoolBusAppConfigEntity;

import java.util.List;
import java.util.Optional;

public interface SchoolBusAppConfigRepository extends JpaRepository<SchoolBusAppConfigEntity, Long>,
        JpaSpecificationExecutor<SchoolBusAppConfigEntity> {

    Optional<SchoolBusAppConfigEntity> findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(String configCode);

    List<SchoolBusAppConfigEntity> findByIsActiveTrueAndIsDeletedFalse();
}
