package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.SchoolScheduleDayEntity;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.List;

public interface SchoolScheduleDayRepository extends BaseRepository<SchoolScheduleDayEntity, Long> {

    List<SchoolScheduleDayEntity> findByScheduleIdAndIsDeletedFalse(Long scheduleId);

    @Modifying
    @Query("UPDATE SchoolScheduleDayEntity d SET d.isDeleted = true, d.updatedBy = :actor WHERE d.schedule.id = :scheduleId AND d.isDeleted = false")
    int softDeleteByScheduleId(@Param("scheduleId") Long scheduleId, @Param("actor") String actor);
}
