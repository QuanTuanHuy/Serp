package com.example.ttcrs.dto.request.request;

import com.example.ttcrs.constant.RequestStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO dùng để cập nhật hàng loạt trạng thái của nhiều Request.
 *
 * <p>Dùng trong luồng tạo Transport Plan:
 * <ul>
 *   <li>Bước "Next" (Step 1 → Step 2): cập nhật các request đã chọn sang PLANNED.</li>
 *   <li>Bước "Back" (Step 2 → Step 1): hoàn trả trạng thái về PENDING.</li>
 * </ul>
 */
@Getter
@Setter
public class UpdateRequestsStatusDTO {

    /** Danh sách ID của các request cần cập nhật trạng thái. */
    @NotEmpty(message = "requestIds must not be empty")
    private List<Long> requestIds;

    /** Trạng thái mới cần áp dụng cho tất cả request trong danh sách. */
    @NotNull(message = "status must not be null")
    private RequestStatus status;
}
