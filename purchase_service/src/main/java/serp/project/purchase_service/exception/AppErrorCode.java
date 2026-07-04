package serp.project.purchase_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
    UNIMPLEMENTED("PURCHASE-APP-001", "Tính năng chưa được triển khai", HttpStatus.NOT_IMPLEMENTED),
    UNEXPECTED_EXCEPTION("PURCHASE-APP-002", "Đã xảy ra lỗi không mong muốn", HttpStatus.INTERNAL_SERVER_ERROR),

    NOT_FOUND("PURCHASE-BIZ-001", "Không tìm thấy đối tượng hoặc không có quyền truy cập", HttpStatus.NOT_FOUND),
    INVALID_STATUS_TRANSITION("PURCHASE-BIZ-002", "Chuyển trạng thái không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_NOT_READY_FOR_DELIVERY("PURCHASE-BIZ-003", "Đơn hàng chưa sẵn sàng để giao", HttpStatus.BAD_REQUEST),
    ORDER_NOT_APPROVED_YET("PURCHASE-BIZ-004", "Đơn hàng chưa được phê duyệt", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_ORDER_IN_CURRENT_STATUS("PURCHASE-BIZ-005", "Không thể cập nhật đơn hàng ở trạng thái hiện tại",
            HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION("PURCHASE-BIZ-006", "Vi phạm toàn vẹn dữ liệu", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_ORDER_IN_CURRENT_STATUS("PURCHASE-BIZ-007", "Không thể xóa đơn hàng ở trạng thái hiện tại",
            HttpStatus.BAD_REQUEST),

    UNKNOWN_ENUM_VALUE("PURCHASE-VAL-001", "Giá trị không xác định", HttpStatus.BAD_REQUEST),
    REQUEST_VALIDATION_FAILED("PURCHASE-VAL-002", "Xác thực yêu cầu không thành công", HttpStatus.BAD_REQUEST),

    CANNOT_ACCESS("PURCHASE-SEC-002", "Không thể truy cập tài nguyên được yêu cầu", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("PURCHASE-SEC-001", "Không được phép truy cập", HttpStatus.UNAUTHORIZED),
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
