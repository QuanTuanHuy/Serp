package com.example.ttcrs.service;

import com.example.ttcrs.dto.request.RequestFilterDTO;
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

/**
 * Service xử lý business logic cho Request.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final AuthUtils authUtils;

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
