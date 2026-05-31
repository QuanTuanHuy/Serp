package serp.project.school_bus_service.shared.base;

import serp.project.school_bus_service.dto.response.BaseResponse;

import java.util.List;
import java.util.function.Function;

public abstract class BaseMapper {

    protected <T extends BaseResponse> T enrich(T response, BaseEntity entity) {
        response.setId(entity.getId());
        response.setTenantId(entity.getTenantId());
        response.setIsActive(entity.getIsActive());
        response.setIsDeleted(entity.getIsDeleted());
        response.setCreatedAt(entity.getCreatedAt());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setUpdatedBy(entity.getUpdatedBy());
        return response;
    }

    protected <T, R> List<R> mapList(List<T> items, Function<T, R> mapper) {
        return items == null ? List.of() : items.stream().map(mapper).toList();
    }
}
