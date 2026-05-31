package serp.project.school_bus_service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import serp.project.school_bus_service.entity.CodeSequenceEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.Optional;

public interface CodeSequenceRepository extends BaseRepository<CodeSequenceEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CodeSequenceEntity> findByTenantIdAndSequenceKeyAndIsDeletedFalse(Long tenantId, String sequenceKey);
}
