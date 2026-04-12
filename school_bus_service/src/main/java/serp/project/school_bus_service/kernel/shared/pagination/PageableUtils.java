package serp.project.school_bus_service.kernel.shared.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import serp.project.school_bus_service.application.dto.request.BaseParamsRequest;

import java.util.Set;

public final class PageableUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private PageableUtils() {
    }

    public static Pageable from(BaseParamsRequest request, Set<String> allowedSorts, String defaultSortBy) {
        BaseParamsRequest safeRequest = request == null ? new BaseParamsRequest() {
        } : request;
        int page = safeRequest.getPage() == null || safeRequest.getPage() < 0 ? DEFAULT_PAGE : safeRequest.getPage();
        int size = safeRequest.getSize() == null ? DEFAULT_SIZE : Math.max(1, Math.min(safeRequest.getSize(), MAX_SIZE));
        String sortBy = resolveSortBy(safeRequest.getSortBy(), allowedSorts, defaultSortBy);
        Sort.Direction direction = "ASC".equalsIgnoreCase(safeRequest.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private static String resolveSortBy(String requestedSortBy, Set<String> allowedSorts, String defaultSortBy) {
        if (requestedSortBy == null || requestedSortBy.isBlank()) {
            return defaultSortBy;
        }
        return allowedSorts.contains(requestedSortBy) ? requestedSortBy : defaultSortBy;
    }
}
