package com.example.ttcrs.service;

import com.example.ttcrs.dto.request.resource.CreateContainerDTO;
import com.example.ttcrs.dto.request.resource.CreateDriverDTO;
import com.example.ttcrs.dto.request.resource.CreateTrailerDTO;
import com.example.ttcrs.dto.request.resource.CreateTruckDTO;
import com.example.ttcrs.dto.request.resource.UpdateContainerDTO;
import com.example.ttcrs.dto.request.resource.UpdateDriverDTO;
import com.example.ttcrs.dto.request.resource.UpdateTrailerDTO;
import com.example.ttcrs.dto.request.resource.UpdateTruckDTO;
import com.example.ttcrs.dto.response.ContainerResponseDTO;
import com.example.ttcrs.dto.response.DriverResponseDTO;
import com.example.ttcrs.dto.response.TrailerResponseDTO;
import com.example.ttcrs.dto.response.TruckResponseDTO;
import com.example.ttcrs.entity.ContainerEntity;
import com.example.ttcrs.entity.DriverEntity;
import com.example.ttcrs.entity.TrailerEntity;
import com.example.ttcrs.entity.TruckEntity;
import com.example.ttcrs.repository.ContainerRepository;
import com.example.ttcrs.repository.DriverRepository;
import com.example.ttcrs.repository.TrailerRepository;
import com.example.ttcrs.repository.TruckRepository;
import com.example.ttcrs.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ContainerRepository containerRepository;
    private final TruckRepository truckRepository;
    private final TrailerRepository trailerRepository;
    private final DriverRepository driverRepository;
    private final AuthUtils authUtils;

    private Long resolveTenantId() {
        return authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token. Vui lòng kiểm tra lại JWT."));
    }

    // ── Containers ────────────────────────────────────────────────────────

    public List<ContainerResponseDTO> getContainers() {
        Long tenantId = resolveTenantId();
        log.debug("Fetching containers for tenantId={}", tenantId);
        return containerRepository.findAllByTenantId(tenantId)
                .stream().map(ContainerResponseDTO::fromEntity).toList();
    }

    @Transactional
    public ContainerResponseDTO createContainer(CreateContainerDTO dto) {
        Long tenantId = resolveTenantId();
        String code = dto.getCode().trim().toUpperCase();
        log.info("Creating container code={}, tenantId={}", code, tenantId);
        if (containerRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Container code '" + code + "' already exists.");
        }
        ContainerEntity entity = ContainerEntity.builder()
                .tenantId(tenantId)
                .code(code)
                .size(dto.getSize())
                .currentLocationCode(dto.getCurrentLocationCode())
                .build();
        return ContainerResponseDTO.fromEntity(containerRepository.save(entity));
    }

    @Transactional
    public ContainerResponseDTO updateContainer(Long id, UpdateContainerDTO dto) {
        Long tenantId = resolveTenantId();
        ContainerEntity entity = containerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Container not found: " + id));
        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Container not found: " + id);
        }
        log.info("Updating container id={}, tenantId={}", id, tenantId);
        if (dto.getStatus()              != null) entity.setStatus(dto.getStatus());
        if (dto.getCurrentLocationCode() != null) entity.setCurrentLocationCode(dto.getCurrentLocationCode());
        return ContainerResponseDTO.fromEntity(containerRepository.save(entity));
    }

    @Transactional
    public void deleteContainer(Long id) {
        Long tenantId = resolveTenantId();
        ContainerEntity entity = containerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Container not found: " + id));
        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Container not found: " + id);
        }
        log.info("Soft-deleting container id={}, tenantId={}", id, tenantId);
        entity.setIsDeleted(true);
        containerRepository.save(entity);
    }

    // ── Trucks ────────────────────────────────────────────────────────────

    public List<TruckResponseDTO> getTrucks() {
        Long tenantId = resolveTenantId();
        log.debug("Fetching trucks for tenantId={}", tenantId);
        return truckRepository.findAllByTenantId(tenantId)
                .stream().map(TruckResponseDTO::fromEntity).toList();
    }

    @Transactional
    public TruckResponseDTO createTruck(CreateTruckDTO dto) {
        Long tenantId = resolveTenantId();
        String code = dto.getCode().trim().toUpperCase();
        log.info("Creating truck code={}, tenantId={}", code, tenantId);
        if (truckRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Truck code '" + code + "' already exists.");
        }
        TruckEntity entity = TruckEntity.builder()
                .tenantId(tenantId)
                .code(code)
                .currentLocationCode(dto.getCurrentLocationCode())
                .build();
        return TruckResponseDTO.fromEntity(truckRepository.save(entity));
    }

    @Transactional
    public TruckResponseDTO updateTruck(Long id, UpdateTruckDTO dto) {
        Long tenantId = resolveTenantId();
        TruckEntity entity = truckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found: " + id));
        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Truck not found: " + id);
        }
        log.info("Updating truck id={}, tenantId={}", id, tenantId);
        if (dto.getStatus()              != null) entity.setStatus(dto.getStatus());
        if (dto.getCurrentLocationCode() != null) entity.setCurrentLocationCode(dto.getCurrentLocationCode());
        return TruckResponseDTO.fromEntity(truckRepository.save(entity));
    }

    @Transactional
    public void deleteTruck(Long id) {
        Long tenantId = resolveTenantId();
        TruckEntity entity = truckRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Truck not found: " + id));
        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Truck not found: " + id);
        }
        log.info("Soft-deleting truck id={}, tenantId={}", id, tenantId);
        entity.setIsDeleted(true);
        truckRepository.save(entity);
    }

    // ── Trailers ──────────────────────────────────────────────────────────

    public List<TrailerResponseDTO> getTrailers() {
        Long tenantId = resolveTenantId();
        log.debug("Fetching trailers for tenantId={}", tenantId);
        return trailerRepository.findAllByTenantId(tenantId)
                .stream().map(TrailerResponseDTO::fromEntity).toList();
    }

    @Transactional
    public TrailerResponseDTO createTrailer(CreateTrailerDTO dto) {
        Long tenantId = resolveTenantId();
        String code = dto.getCode().trim().toUpperCase();
        log.info("Creating trailer code={}, tenantId={}", code, tenantId);
        if (trailerRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Trailer code '" + code + "' already exists.");
        }
        TrailerEntity entity = TrailerEntity.builder()
                .tenantId(tenantId)
                .code(code)
                .currentLocationCode(dto.getCurrentLocationCode())
                .build();
        return TrailerResponseDTO.fromEntity(trailerRepository.save(entity));
    }

    @Transactional
    public TrailerResponseDTO updateTrailer(Long id, UpdateTrailerDTO dto) {
        Long tenantId = resolveTenantId();
        TrailerEntity entity = trailerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trailer not found: " + id));
        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Trailer not found: " + id);
        }
        log.info("Updating trailer id={}, tenantId={}", id, tenantId);
        if (dto.getStatus()              != null) entity.setStatus(dto.getStatus());
        if (dto.getCurrentLocationCode() != null) entity.setCurrentLocationCode(dto.getCurrentLocationCode());
        return TrailerResponseDTO.fromEntity(trailerRepository.save(entity));
    }

    @Transactional
    public void deleteTrailer(Long id) {
        Long tenantId = resolveTenantId();
        TrailerEntity entity = trailerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trailer not found: " + id));
        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Trailer not found: " + id);
        }
        log.info("Soft-deleting trailer id={}, tenantId={}", id, tenantId);
        entity.setIsDeleted(true);
        trailerRepository.save(entity);
    }

    // ── Drivers ───────────────────────────────────────────────────────────

    public List<DriverResponseDTO> getDrivers() {
        Long tenantId = resolveTenantId();
        log.debug("Fetching drivers for tenantId={}", tenantId);
        return driverRepository.findAllByTenantId(tenantId)
                .stream().map(DriverResponseDTO::fromEntity).toList();
    }

    @Transactional
    public DriverResponseDTO createDriver(CreateDriverDTO dto) {
        Long tenantId = resolveTenantId();
        log.info("Creating driver name={}, tenantId={}", dto.getName(), tenantId);
        DriverEntity entity = DriverEntity.builder()
                .tenantId(tenantId)
                .name(dto.getName().trim())
                .build();
        return DriverResponseDTO.fromEntity(driverRepository.save(entity));
    }

    @Transactional
    public DriverResponseDTO updateDriver(Long id, UpdateDriverDTO dto) {
        Long tenantId = resolveTenantId();
        DriverEntity entity = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + id));
        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Driver not found: " + id);
        }
        log.info("Updating driver id={}, tenantId={}", id, tenantId);
        if (dto.getName()   != null && !dto.getName().isBlank()) entity.setName(dto.getName().trim());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        return DriverResponseDTO.fromEntity(driverRepository.save(entity));
    }

    @Transactional
    public void deleteDriver(Long id) {
        Long tenantId = resolveTenantId();
        DriverEntity entity = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + id));
        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Driver not found: " + id);
        }
        log.info("Soft-deleting driver id={}, tenantId={}", id, tenantId);
        entity.setIsDeleted(true);
        driverRepository.save(entity);
    }
}
