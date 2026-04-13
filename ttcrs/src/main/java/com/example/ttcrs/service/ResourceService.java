package com.example.ttcrs.service;

import com.example.ttcrs.dto.request.CreateContainerDTO;
import com.example.ttcrs.dto.request.CreateDriverDTO;
import com.example.ttcrs.dto.request.CreateTrailerDTO;
import com.example.ttcrs.dto.request.CreateTruckDTO;
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
}
