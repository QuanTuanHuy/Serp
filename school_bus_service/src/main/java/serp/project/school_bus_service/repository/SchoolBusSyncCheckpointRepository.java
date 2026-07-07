package serp.project.school_bus_service.repository;

import serp.project.school_bus_service.entity.SchoolBusSyncCheckpointEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SchoolBusSyncCheckpointRepository extends BaseRepository<SchoolBusSyncCheckpointEntity, Long> {

    Optional<SchoolBusSyncCheckpointEntity> findFirstBySyncCodeAndIsDeletedFalseOrderByUpdatedAtDescIdDesc(
            String syncCode);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO public.school_bus_sync_checkpoint (
                sync_code,
                last_synced_count,
                is_active,
                is_deleted,
                tenant_id,
                created_at,
                created_by,
                updated_at,
                updated_by
            )
            VALUES (
                :syncCode,
                0,
                true,
                false,
                0,
                CURRENT_TIMESTAMP,
                'SYSTEM',
                CURRENT_TIMESTAMP,
                'SYSTEM'
            )
            ON CONFLICT (sync_code) WHERE is_deleted = false DO NOTHING
            """, nativeQuery = true)
    int insertActiveCheckpointIfAbsent(@Param("syncCode") String syncCode);

}
