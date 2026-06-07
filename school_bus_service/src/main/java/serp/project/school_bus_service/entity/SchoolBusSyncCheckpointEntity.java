package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Entity storing sync checkpoint state for incremental user synchronization.
 */
@Entity
@Table(name = "school_bus_sync_checkpoint")
@Getter
@Setter
public class SchoolBusSyncCheckpointEntity extends BaseModel {

    @Column(name = "sync_code", nullable = false)
    private String syncCode;

    @Column(name = "last_success_sync_at")
    private LocalDateTime lastSuccessSyncAt;

    @Column(name = "last_attempt_sync_at")
    private LocalDateTime lastAttemptSyncAt;

    @Column(name = "last_status")
    private String lastStatus;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "last_synced_count", nullable = false)
    private Integer lastSyncedCount = 0;

}
