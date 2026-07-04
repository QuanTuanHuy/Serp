package com.example.ttcrs.service;

import com.example.ttcrs.constant.RequestStatus;
import com.example.ttcrs.constant.RequestType;
import com.example.ttcrs.dto.request.request.CreateRequestDTO;
import com.example.ttcrs.dto.request.request.RequestFilterDTO;
import com.example.ttcrs.dto.request.request.UpdateRequestDTO;
import com.example.ttcrs.dto.request.request.UpdateRequestsStatusDTO;
import com.example.ttcrs.dto.request.transportplan.CreateTransportPlanInputDTO;
import com.example.ttcrs.dto.response.PageResponse;
import com.example.ttcrs.dto.response.RequestResponseDTO;
import com.example.ttcrs.entity.RequestEntity;
import com.example.ttcrs.repository.RequestRepository;
import com.example.ttcrs.repository.RequestRepository.RequestSpecs;
import com.example.ttcrs.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service xử lý business logic cho Request.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final AuthUtils authUtils;
    private final AlgorithmService algorithmService;

    // =========================================================================
    // Public methods
    // =========================================================================

    /**
     * Lấy danh sách Request có phân trang và bộ lọc động cho Dispatcher.
     *
     * <p>Luồng xử lý:
     * <ol>
     *   <li>Lấy tenant_id từ JWT token (bắt buộc — ném exception nếu không có).</li>
     *   <li>Build {@link Specification} từ các filter trong {@link RequestFilterDTO}.</li>
     *   <li>Build {@link Pageable} từ page/size/sort parameters.</li>
     *   <li>Query database và map kết quả sang DTO.</li>
     * </ol>
     *
     * @param filter các tham số filter và phân trang từ request
     * @return {@link PageResponse} chứa danh sách {@link RequestResponseDTO}
     * @throws IllegalStateException nếu không lấy được tenant_id từ token
     */
    public PageResponse<RequestResponseDTO> getRequests(RequestFilterDTO filter) {
        // 1. Lấy tenant_id — bắt buộc phải có
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token. Vui lòng kiểm tra lại JWT."
                ));

        log.debug("Fetching requests for tenantId={}, filter={}", tenantId, filter);

        // 2. Build Specification
        Specification<RequestEntity> spec = buildSpecification(tenantId, filter);

        // 3. Build Pageable (xử lý sort direction an toàn)
        Pageable pageable = buildPageable(filter);

        // 4. Query và map sang DTO
        Page<RequestResponseDTO> resultPage = requestRepository
                .findAll(spec, pageable)
                .map(RequestResponseDTO::fromEntity);

        return PageResponse.from(resultPage);
    }

    /**
     * Tạo hàng loạt N request cho một customer (dành cho Dispatcher).
     *
     * <p>Luồng xử lý:
     * <ol>
     *   <li>Lấy tenantId + userId từ JWT.</li>
     *   <li>Tạo {@code quantity} bản sao {@link RequestEntity} với status PENDING.</li>
     *   <li>Lưu tất cả vào DB bằng {@code saveAll}.</li>
     *   <li>Trả về danh sách {@link RequestResponseDTO}.</li>
     * </ol>
     *
     * @param dto tham số tạo request từ dispatcher
     * @return danh sách request vừa tạo (size == dto.quantity)
     */
    @Transactional
    public List<RequestResponseDTO> createRequests(CreateRequestDTO dto) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token. Vui lòng kiểm tra lại JWT."
                ));

        Long createdBy = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định user từ token."
                ));

        log.info("Creating {} requests for customerId={}, tenantId={}, type={}",
                dto.getQuantity(), dto.getCustomerId(), tenantId, dto.getType());

                if (dto.getType() != RequestType.OE) {
                        if (dto.getSrcLocationCode() == null || dto.getSrcLocationCode().isBlank()) {
                                throw new IllegalArgumentException("srcLocationCode không được để trống");
                        }
                        if (dto.getContainerCode() == null || dto.getContainerCode().isBlank()) {
                                throw new IllegalArgumentException("containerCode không được để trống với loại request " + dto.getType());
                        }
                }
                if (dto.getType() != RequestType.IE) {
                        if (dto.getDestLocationCode() == null || dto.getDestLocationCode().isBlank()) {
                                throw new IllegalArgumentException("destLocationCode không được để trống");
                        }
                }

        List<RequestEntity> entities = new ArrayList<>(dto.getQuantity());
        for (int i = 0; i < dto.getQuantity(); i++) {
            entities.add(RequestEntity.builder()
                    .tenantId(tenantId)
                    .customerId(dto.getCustomerId())
                    .type(dto.getType())
                    .srcLocationCode(dto.getSrcLocationCode())
                    .destLocationCode(dto.getDestLocationCode())
                    .weight(dto.getWeight())
                    .containerCode(dto.getContainerCode())
                    .dropTrailerRequired(dto.getDropTrailerRequired())
                    .earlyAtSrc(dto.getEarlyAtSrc())
                    .lateAtSrc(dto.getLateAtSrc())
                    .earlyAtDest(dto.getEarlyAtDest())
                    .lateAtDest(dto.getLateAtDest())
                    .status(RequestStatus.PENDING)
                    .createdBy(createdBy)
                    .build());
        }

        List<RequestEntity> saved = requestRepository.saveAll(entities);

        return saved.stream()
                .map(RequestResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Cập nhật hàng loạt trạng thái cho nhiều Request (dành cho Dispatcher).
     *
     * <p>Dùng trong luồng tạo Transport Plan:
     * <ul>
     *   <li>"Next" (Step 1 → 2): cập nhật sang {@code PLANNED}.</li>
     *   <li>"Back" (Step 2 → 1): hoàn trả về {@code PENDING}.</li>
     * </ul>
     *
     * <p>Chỉ cập nhật các request thuộc đúng tenant hiện tại.
     * Các ID không tồn tại hoặc thuộc tenant khác sẽ bị bỏ qua.
     *
     * @param dto danh sách request ID và trạng thái mới
     * @return danh sách DTO của các request đã được cập nhật
     */
    @Transactional
    public List<RequestResponseDTO> updateRequestsStatus(UpdateRequestsStatusDTO dto) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token."
                ));

        log.info("Updating status of {} requests to {} for tenantId={}",
                dto.getRequestIds().size(), dto.getStatus(), tenantId);

        List<RequestEntity> requests = requestRepository.findAllById(dto.getRequestIds());

        // Chỉ cập nhật các request thuộc đúng tenant
        List<RequestEntity> owned = requests.stream()
                .filter(r -> r.getTenantId().equals(tenantId))
                .toList();

        owned.forEach(r -> {
            r.setStatus(dto.getStatus());
            // When reverting to PLANNED, detach from any transport plan
            if (dto.getStatus() == RequestStatus.PLANNED) {
                r.setTransportPlanId(null);
            }
        });
        List<RequestEntity> saved = requestRepository.saveAll(owned);

        return saved.stream()
                .map(RequestResponseDTO::fromEntity)
                .toList();
    }

    /**
     * Creates a transport plan in a single atomic transaction.
     *
     * <p>This is the "Create Plan" final-submit handler. Everything runs inside
     * one {@code @Transactional} boundary so that either the entire operation
     * succeeds or the whole thing rolls back — no partial state is persisted.
     *
     * <p>Current behaviour (algorithm not yet integrated):
     * <ol>
     *   <li>Validates that all supplied request IDs belong to the current tenant
     *       and are in {@code PLANNED} status.</li>
     *   <li>Logs the full plan input (requestIds, depot codes, resource IDs) so
     *       the algorithm can pick it up in a later phase.</li>
     * </ol>
     *
     * <p>When the routing algorithm is added, its invocation (and any resulting
     * DB writes for {@code transport_plans} rows) must be placed inside this
     * same method so they stay within the transaction.
     *
     * @param dto the complete plan input from the dispatcher
     * @throws IllegalStateException if the tenant cannot be resolved from the token
     * @throws IllegalArgumentException if any supplied request ID is not PLANNED
     *                                  or does not belong to this tenant
     */
    /**
     * Creates a transport plan in a single atomic transaction.
     *
     * <p>Validates all requests, then delegates to {@link AlgorithmService}
     * to build the routing input, run the solver, and return the JSON result.
     *
     * <p>The algorithm runs outside the DB transaction (via
     * {@code Propagation.NOT_SUPPORTED}) to avoid holding a DB connection for
     * the full 60-second solver window. Validation still happens inside this
     * method before the algorithm is invoked.
     *
     * @param dto the complete plan input from the dispatcher
     * @return Gson-serialised {@link com.example.ttcrs.algorithm.models.output.TruckMoocContainerOutputJson}
     */
    @Transactional
    public String createTransportPlanInput(CreateTransportPlanInputDTO dto) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Cannot resolve tenant from token."));

        log.info("Creating transport plan for tenantId={}, requestIds={}, " +
                        "truckIds={}, trailerIds={}, containerIds={}",
                tenantId,
                dto.getRequestIds(),
                dto.getTruckIds(),
                dto.getTrailerIds(),
                dto.getContainerIds());

        // Validate: all supplied requests must belong to this tenant and be PLANNED
        List<RequestEntity> requests = requestRepository.findAllById(dto.getRequestIds());

        List<RequestEntity> invalid = requests.stream()
                .filter(r -> !r.getTenantId().equals(tenantId)
                        || r.getStatus() != RequestStatus.PLANNED)
                .toList();

        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException(
                    "Some requests are not in PLANNED status or do not belong to this tenant: "
                            + invalid.stream().map(r -> r.getId().toString()).toList());
        }

        log.info("Validation passed for tenantId={}. Invoking routing algorithm...", tenantId);
        return algorithmService.executeAlgorithm(dto, tenantId);
    }

    /**
     * Lấy danh sách Request có phân trang cho Customer.
     * Lọc theo tenantId + customer_id = userId từ JWT.
     */
    public PageResponse<RequestResponseDTO> getCustomerRequests(RequestFilterDTO filter) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Cannot resolve tenant from token."));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Cannot resolve userId from token."));

        Specification<RequestEntity> spec = Specification
                .where(RequestSpecs.withTenantId(tenantId))
                .and(RequestSpecs.withCustomerId(userId))
                .and(RequestSpecs.withStatuses(filter.getStatuses()))
                .and(RequestSpecs.withType(filter.getType()))
                .and(RequestSpecs.withSrcLocationCode(filter.getSrcLocationCode()))
                .and(RequestSpecs.withDestLocationCode(filter.getDestLocationCode()))
                .and(RequestSpecs.withCreatedBetween(filter.getCreatedFrom(), filter.getCreatedTo()));

        Page<RequestResponseDTO> resultPage = requestRepository
                .findAll(spec, buildPageable(filter))
                .map(RequestResponseDTO::fromEntity);

        return PageResponse.from(resultPage);
    }

    /**
     * Lấy chi tiết một Request theo ID (dành cho Customer).
     * Kiểm tra tenantId + customer_id = userId để đảm bảo chỉ thấy request của mình.
     */
    public RequestResponseDTO getCustomerRequestById(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException("Cannot resolve tenant from token."));
        Long userId = authUtils.getCurrentUserId()
                .orElseThrow(() -> new IllegalStateException("Cannot resolve userId from token."));

        RequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        if (!entity.getTenantId().equals(tenantId) || !userId.equals(entity.getCustomerId())) {
            throw new IllegalArgumentException("Request not found: " + id);
        }

        return RequestResponseDTO.fromEntity(entity);
    }

    /**
     * Lấy chi tiết một Request theo ID (dành cho Dispatcher).
     *
     * @param id ID của request
     * @return {@link RequestResponseDTO}
     * @throws IllegalArgumentException nếu không tìm thấy hoặc không thuộc tenant
     */
    public RequestResponseDTO getRequestById(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token."
                ));

        RequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Request not found: " + id);
        }

        return RequestResponseDTO.fromEntity(entity);
    }

    /**
     * Cập nhật một Request theo ID.
     * Chỉ được phép cập nhật khi request đang ở trạng thái PENDING.
     *
     * @param id  ID của request cần cập nhật
     * @param dto các trường cần thay đổi (null = giữ nguyên)
     * @return {@link RequestResponseDTO} sau khi cập nhật
     * @throws IllegalArgumentException nếu không tìm thấy, không thuộc tenant, hoặc không ở trạng thái PENDING
     */
    @Transactional
    public RequestResponseDTO updateRequest(Long id, UpdateRequestDTO dto) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token."
                ));

        RequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Request not found: " + id);
        }
        if (entity.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Chỉ có thể cập nhật request ở trạng thái PENDING. Current status: " + entity.getStatus());
        }

        log.info("Updating request id={}, tenantId={}", id, tenantId);

        if (entity.getType() != RequestType.OE
                && dto.getSrcLocationCode() != null
                && !dto.getSrcLocationCode().isBlank())
            entity.setSrcLocationCode(dto.getSrcLocationCode());
        if (dto.getDestLocationCode() != null && !dto.getDestLocationCode().isBlank())
            entity.setDestLocationCode(dto.getDestLocationCode());
        if (dto.getEarlyAtSrc()       != null) entity.setEarlyAtSrc(dto.getEarlyAtSrc());
        if (dto.getLateAtSrc()        != null) entity.setLateAtSrc(dto.getLateAtSrc());
        if (dto.getEarlyAtDest()      != null) entity.setEarlyAtDest(dto.getEarlyAtDest());
        if (dto.getLateAtDest()       != null) entity.setLateAtDest(dto.getLateAtDest());
        if (dto.getWeight()           != null) entity.setWeight(dto.getWeight());
        if (dto.getContainerSize()    != null) entity.setContainerSize(dto.getContainerSize());
        if (dto.getDropTrailerRequired() != null) entity.setDropTrailerRequired(dto.getDropTrailerRequired());
        if (dto.getContainerCode()      != null) entity.setContainerCode(dto.getContainerCode());
        if (dto.getReason()           != null) entity.setReason(dto.getReason());

        return RequestResponseDTO.fromEntity(requestRepository.save(entity));
    }

    /**
     * Xoá mềm một Request theo ID (đặt isDeleted = true).
     * Chỉ được phép xoá khi request đang ở trạng thái PENDING.
     *
     * @param id ID của request cần xoá
     * @throws IllegalArgumentException nếu không tìm thấy, không thuộc tenant, hoặc không ở trạng thái PENDING
     */
    @Transactional
    public void deleteRequest(Long id) {
        Long tenantId = authUtils.getCurrentTenantId()
                .orElseThrow(() -> new IllegalStateException(
                        "Không thể xác định tenant từ token."
                ));

        RequestEntity entity = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        if (!entity.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Request not found: " + id);
        }
        if (entity.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Chỉ có thể xoá request ở trạng thái PENDING. Current status: " + entity.getStatus());
        }

        log.info("Soft-deleting request id={}, tenantId={}", id, tenantId);
        entity.setIsDeleted(true);
        requestRepository.save(entity);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Tổng hợp tất cả điều kiện filter thành một Specification.
     *
     * <p>Luôn bắt đầu với {@code withTenantId} để đảm bảo tenant isolation,
     * sau đó AND với các filter tùy chọn khác.
     */
    private Specification<RequestEntity> buildSpecification(Long tenantId, RequestFilterDTO filter) {
        return Specification
                // [BẮT BUỘC] tenant isolation
                .where(RequestSpecs.withTenantId(tenantId))
                // Các filter tùy chọn
                .and(RequestSpecs.withStatuses(filter.getStatuses()))
                .and(RequestSpecs.withType(filter.getType()))
                .and(RequestSpecs.withSrcLocationCode(filter.getSrcLocationCode()))
                .and(RequestSpecs.withDestLocationCode(filter.getDestLocationCode()))
                .and(RequestSpecs.withCreatedBetween(filter.getCreatedFrom(), filter.getCreatedTo()));
    }

    /**
     * Build {@link Pageable} từ các tham số phân trang và sắp xếp.
     *
     * <p>Validate {@code sortDirection} để tránh lỗi nếu client truyền giá trị không hợp lệ.
     * Mặc định sắp xếp theo {@code createdAt DESC}.
     */
    private Pageable buildPageable(RequestFilterDTO filter) {
        // Normalize sort direction — mặc định DESC nếu không hợp lệ
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(filter.getSortDirection());
        } catch (IllegalArgumentException e) {
            log.warn("sortDirection không hợp lệ: '{}', dùng mặc định DESC", filter.getSortDirection());
            direction = Sort.Direction.DESC;
        }

        // Validate sortBy field để tránh HQL injection
        // Chỉ cho phép các tên field hợp lệ của RequestEntity
        String sortBy = resolveSortField(filter.getSortBy());

        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    /**
     * Ánh xạ tên sort field từ client sang tên Java field trong {@link RequestEntity}.
     *
     * <p>Chỉ cho phép whitelist các field để tránh lỗi hoặc HQL injection.
     * Mặc định về {@code createdAt} nếu field không được phép.
     *
     * @param requestedField tên field client truyền lên
     * @return tên Java field hợp lệ trong RequestEntity
     */
    private String resolveSortField(String requestedField) {
        if (requestedField == null) return "createdAt";

        return switch (requestedField.toLowerCase()) {
            case "id"                 -> "id";
            case "status"             -> "status";
            case "type"               -> "type";
            case "srclocationcode"    -> "srcLocationCode";
            case "destlocationcode"   -> "destLocationCode";
            case "weight"             -> "weight";
            case "createdat",
                 "created_at"         -> "createdAt";
            case "lastupdatedstamp",
                 "last_updated_stamp" -> "lastUpdatedStamp";
            default -> {
                log.warn("sortBy field không hợp lệ: '{}', dùng mặc định createdAt", requestedField);
                yield "createdAt";
            }
        };
    }
}
