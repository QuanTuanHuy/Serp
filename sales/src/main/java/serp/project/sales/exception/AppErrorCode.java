package serp.project.sales.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
        UNIMPLEMENTED("SALES-APP-001", "Tính năng chưa được triển khai", HttpStatus.NOT_IMPLEMENTED),
        UNEXPECTED_EXCEPTION("SALES-APP-002", "Đã xảy ra lỗi không mong muốn", HttpStatus.INTERNAL_SERVER_ERROR),

        NOT_FOUND("SALES-BIZ-001", "Không tìm thấy đối tượng hoặc không có quyền truy cập", HttpStatus.NOT_FOUND),
        INVALID_STATUS_TRANSITION("SALES-BIZ-002", "Chuyển trạng thái không hợp lệ", HttpStatus.BAD_REQUEST),
        CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS("SALES-BIZ-003", "Không thể cập nhật đơn hàng ở trạng thái hiện tại",
                        HttpStatus.BAD_REQUEST),
        ORDER_NOT_APPROVED_YET("SALES-BIZ-004", "Đơn hàng chưa được phê duyệt", HttpStatus.BAD_REQUEST),
        INSUFFICIENT_PRODUCT_QUANTITY("SALES-BIZ-005", "Số lượng sản phẩm không đủ",
                        HttpStatus.BAD_REQUEST),
        DATA_INTEGRITY_VIOLATION("SALES-BIZ-006", "Vi phạm toàn vẹn dữ liệu", HttpStatus.BAD_REQUEST),
        CANNOT_DELETE_ORDER_IN_CURRENT_STATUS("SALES-BIZ-007", "Không thể xóa đơn hàng ở trạng thái hiện tại",
                        HttpStatus.BAD_REQUEST),
        INSUFFICIENT_INVENTORY_TO_ALLOCATE("SALES-BIZ-008", "Không đủ tồn kho để phân bổ cho đơn hàng",
                        HttpStatus.BAD_REQUEST),
        NEED_TO_BE_RESERVED_OR_DELIVERED("SALES-BIZ-009", "Vẫn còn các mặt hàng cần được giữ chỗ hoặc giao",
                        HttpStatus.BAD_REQUEST),

        UNKNOWN_ENUM_VALUE("SALES-VAL-001", "Giá trị không xác định", HttpStatus.BAD_REQUEST),
        REQUEST_VALIDATION_FAILED("SALES-VAL-002", "Xác thực yêu cầu không thành công", HttpStatus.BAD_REQUEST),

        UNAUTHORIZED("SALES-SEC-001", "Không được phép truy cập", HttpStatus.UNAUTHORIZED),
        CANNOT_ACCESS("SALES-SEC-002", "Không thể truy cập tài nguyên được yêu cầu", HttpStatus.FORBIDDEN),
        ;

        private final String code;
        private final String message;
        private final HttpStatus status;

        AppErrorCode(String code, String message, HttpStatus status) {
                this.code = code;
                this.message = message;
                this.status = status;
        }

}
