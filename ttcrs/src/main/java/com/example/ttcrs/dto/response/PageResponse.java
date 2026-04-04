package com.example.ttcrs.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Wrapper cho response có phân trang.
 *
 * Cấu trúc trong trường {@code data} của {@link ApiResponse}:
 * <pre>
 * {
 *   "items": [...],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 100,
 *   "totalPages": 5,
 *   "last": false
 * }
 * </pre>
 *
 * @param <T> kiểu dữ liệu của từng phần tử trong danh sách
 */
@Getter
@Builder
public class PageResponse<T> {

    /** Danh sách dữ liệu của trang hiện tại */
    private List<T> items;

    /** Số trang hiện tại (zero-based) */
    private int page;

    /** Số phần tử mỗi trang */
    private int size;

    /** Tổng số phần tử trong toàn bộ kết quả */
    private long totalElements;

    /** Tổng số trang */
    private int totalPages;

    /** Có phải trang cuối không */
    private boolean last;

    // -------------------------------------------------------------------------
    // Static factory từ Spring Page
    // -------------------------------------------------------------------------

    /**
     * Tạo {@link PageResponse} từ Spring {@link Page} object.
     *
     * @param page   đối tượng Page từ Spring Data JPA
     * @param <T>    kiểu dữ liệu của item
     * @return PageResponse đã được map
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
