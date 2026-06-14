/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarAssignmentEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarExceptionEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileBlockEntity;
import serp.project.pmcore.domain.resourcecalendar.entity.ResourceCalendarProfileEntity;
import serp.project.pmcore.domain.resourcecalendar.enums.ResourceCalendarExceptionType;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarAssignmentPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarExceptionPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfileBlockPort;
import serp.project.pmcore.domain.resourcecalendar.port.IResourceCalendarProfilePort;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarAssignmentModel;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarExceptionModel;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarProfileBlockModel;
import serp.project.pmcore.infrastructure.store.model.ResourceCalendarProfileModel;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarAssignmentRepository;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarExceptionRepository;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarProfileBlockRepository;
import serp.project.pmcore.infrastructure.store.repository.IResourceCalendarProfileRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResourceCalendarSettingsAdapter implements IResourceCalendarProfilePort,
        IResourceCalendarProfileBlockPort,
        IResourceCalendarAssignmentPort,
        IResourceCalendarExceptionPort {
    private final IResourceCalendarProfileRepository profileRepository;
    private final IResourceCalendarProfileBlockRepository blockRepository;
    private final IResourceCalendarAssignmentRepository assignmentRepository;
    private final IResourceCalendarExceptionRepository exceptionRepository;

    @Override
    public List<ResourceCalendarProfileEntity> listProfiles(Long tenantId) {
        return profileRepository.findByTenantIdOrderByNameAsc(tenantId).stream()
                .map(this::toProfileEntity)
                .toList();
    }

    @Override
    public Optional<ResourceCalendarProfileEntity> getProfileById(Long tenantId, Long profileId) {
        return profileRepository.findByIdAndTenantId(profileId, tenantId)
                .map(this::toProfileEntity);
    }

    @Override
    public ResourceCalendarProfileEntity saveProfile(ResourceCalendarProfileEntity profile) {
        return toProfileEntity(profileRepository.save(toProfileModel(profile)));
    }

    @Override
    public void deleteProfile(Long tenantId, Long profileId) {
        profileRepository.deleteByIdAndTenantId(profileId, tenantId);
    }

    @Override
    public List<ResourceCalendarProfileBlockEntity> listByProfileId(Long profileId) {
        return blockRepository.findByProfileIdOrderByDayOfWeekAscStartTimeAsc(profileId).stream()
                .map(this::toBlockEntity)
                .toList();
    }

    @Override
    public List<ResourceCalendarProfileBlockEntity> replaceBlocks(Long profileId,
                                                                  List<ResourceCalendarProfileBlockEntity> blocks) {
        blockRepository.deleteByProfileId(profileId);
        if (blocks == null || blocks.isEmpty()) {
            return List.of();
        }
        return blockRepository.saveAll(blocks.stream()
                        .map(block -> toBlockModel(profileId, block))
                        .toList())
                .stream()
                .map(this::toBlockEntity)
                .toList();
    }

    @Override
    public List<ResourceCalendarAssignmentEntity> replaceProfileAssignments(Long tenantId,
                                                                           Long profileId,
                                                                           List<ResourceCalendarAssignmentEntity> assignments) {
        assignmentRepository.deleteByTenantIdAndProfileId(tenantId, profileId);
        if (assignments == null || assignments.isEmpty()) {
            return List.of();
        }
        return assignmentRepository.saveAll(assignments.stream()
                        .map(assignment -> toAssignmentModel(tenantId, profileId, assignment))
                        .toList())
                .stream()
                .map(this::toAssignmentEntity)
                .toList();
    }

    @Override
    public List<ResourceCalendarAssignmentEntity> listByProfileId(Long tenantId, Long profileId) {
        return assignmentRepository.findByTenantIdAndProfileIdOrderByUserIdAsc(tenantId, profileId).stream()
                .map(this::toAssignmentEntity)
                .toList();
    }

    @Override
    public List<ResourceCalendarAssignmentEntity> listActiveAssignments(Long tenantId) {
        return assignmentRepository.findByTenantIdOrderByUserIdAscEffectiveFromAsc(tenantId).stream()
                .map(this::toAssignmentEntity)
                .toList();
    }

    @Override
    public List<ResourceCalendarExceptionEntity> listExceptions(Long tenantId,
                                                               List<Long> userIds,
                                                               Long windowStart,
                                                               Long windowEnd) {
        if (userIds == null || userIds.isEmpty() || windowStart == null || windowEnd == null) {
            return List.of();
        }
        return exceptionRepository
                .findByTenantIdAndUserIdInAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
                        tenantId,
                        userIds,
                        toLocalDateTime(windowEnd),
                        toLocalDateTime(windowStart)
                )
                .stream()
                .map(this::toExceptionEntity)
                .toList();
    }

    @Override
    public Optional<ResourceCalendarExceptionEntity> getExceptionById(Long tenantId, Long exceptionId) {
        return exceptionRepository.findByIdAndTenantId(exceptionId, tenantId)
                .map(this::toExceptionEntity);
    }

    @Override
    public ResourceCalendarExceptionEntity saveException(ResourceCalendarExceptionEntity exception) {
        return toExceptionEntity(exceptionRepository.save(toExceptionModel(exception)));
    }

    @Override
    public void deleteException(Long tenantId, Long exceptionId) {
        exceptionRepository.deleteByIdAndTenantId(exceptionId, tenantId);
    }

    private ResourceCalendarProfileEntity toProfileEntity(ResourceCalendarProfileModel model) {
        return ResourceCalendarProfileEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .name(model.getName())
                .description(model.getDescription())
                .timezone(model.getTimezone())
                .isDefault(Boolean.TRUE.equals(model.getIsDefault()))
                .createdAt(toEpochMillis(model.getCreatedAt()))
                .createdBy(model.getCreatedBy())
                .updatedAt(toEpochMillis(model.getUpdatedAt()))
                .updatedBy(model.getUpdatedBy())
                .build();
    }

    private ResourceCalendarProfileModel toProfileModel(ResourceCalendarProfileEntity entity) {
        return ResourceCalendarProfileModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .timezone(entity.getTimezone())
                .isDefault(Boolean.TRUE.equals(entity.getIsDefault()))
                .build();
    }

    private ResourceCalendarProfileBlockEntity toBlockEntity(ResourceCalendarProfileBlockModel model) {
        return ResourceCalendarProfileBlockEntity.builder()
                .id(model.getId())
                .profileId(model.getProfileId())
                .dayOfWeek(model.getDayOfWeek())
                .startTime(model.getStartTime())
                .endTime(model.getEndTime())
                .capacityFactor(model.getCapacityFactor())
                .build();
    }

    private ResourceCalendarProfileBlockModel toBlockModel(Long profileId, ResourceCalendarProfileBlockEntity entity) {
        return ResourceCalendarProfileBlockModel.builder()
                .profileId(profileId)
                .dayOfWeek(entity.getDayOfWeek())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .capacityFactor(entity.getCapacityFactor())
                .build();
    }

    private ResourceCalendarAssignmentEntity toAssignmentEntity(ResourceCalendarAssignmentModel model) {
        return ResourceCalendarAssignmentEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .userId(model.getUserId())
                .profileId(model.getProfileId())
                .effectiveFrom(model.getEffectiveFrom())
                .effectiveTo(model.getEffectiveTo())
                .build();
    }

    private ResourceCalendarAssignmentModel toAssignmentModel(Long tenantId,
                                                             Long profileId,
                                                             ResourceCalendarAssignmentEntity entity) {
        return ResourceCalendarAssignmentModel.builder()
                .tenantId(tenantId)
                .userId(entity.getUserId())
                .profileId(profileId)
                .effectiveFrom(entity.getEffectiveFrom())
                .effectiveTo(entity.getEffectiveTo())
                .build();
    }

    private ResourceCalendarExceptionEntity toExceptionEntity(ResourceCalendarExceptionModel model) {
        return ResourceCalendarExceptionEntity.builder()
                .id(model.getId())
                .tenantId(model.getTenantId())
                .userId(model.getUserId())
                .exceptionType(ResourceCalendarExceptionType.valueOf(model.getExceptionType()))
                .startAt(model.getStartAt())
                .endAt(model.getEndAt())
                .capacityFactor(model.getCapacityFactor())
                .reason(model.getReason())
                .build();
    }

    private ResourceCalendarExceptionModel toExceptionModel(ResourceCalendarExceptionEntity entity) {
        return ResourceCalendarExceptionModel.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .userId(entity.getUserId())
                .exceptionType(entity.getExceptionType().name())
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .capacityFactor(entity.getCapacityFactor())
                .reason(entity.getReason())
                .build();
    }

    private LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    private Long toEpochMillis(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
