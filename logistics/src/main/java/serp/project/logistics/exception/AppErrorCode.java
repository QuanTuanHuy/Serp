package serp.project.logistics.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
    UNIMPLEMENTED("LOGISTICS-APP-001", "Tính năng chưa được triển khai", HttpStatus.NOT_IMPLEMENTED),
    UNEXPECTED_EXCEPTION("LOGISTICS-APP-002", "Đã xảy ra lỗi không mong muốn", HttpStatus.INTERNAL_SERVER_ERROR),

    NOT_FOUND("LOGISTICS-BIZ-001", "Không tìm thấy đối tượng hoặc không có quyền truy cập", HttpStatus.NOT_FOUND),
    INVALID_STATUS_TRANSITION("LOGISTICS-BIZ-002", "Chuyển trạng thái không hợp lệ", HttpStatus.BAD_REQUEST),
    ORDER_NOT_APPROVED_YET("LOGISTICS-BIZ-004", "Đơn hàng chưa được phê duyệt", HttpStatus.BAD_REQUEST),
    EXCEED_REMAINING_QUANTITY("LOGISTICS-BIZ-005", "Số lượng vượt quá mức cho phép còn lại",
            HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION("LOGISTICS-BIZ-006", "Vi phạm toàn vẹn dữ liệu", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_QUANTITY("LOGISTICS-BIZ-007", "Số lượng không đủ", HttpStatus.BAD_REQUEST),
    INVALID_ORDER_STATUS("LOGISTICS-BIZ-008", "Trạng thái đơn hàng không hợp lệ cho thao tác này",
            HttpStatus.BAD_REQUEST),
    INVALID_SHIPMENT_STATUS("LOGISTICS-BIZ-009", "Trạng thái lô hàng không hợp lệ cho thao tác này",
            HttpStatus.BAD_REQUEST),

    UNKNOWN_ENUM_VALUE("LOGISTICS-VAL-001", "Giá trị không xác định", HttpStatus.BAD_REQUEST),
    REQUEST_VALIDATION_FAILED("LOGISTICS-VAL-002", "Xác thực yêu cầu không thành công", HttpStatus.BAD_REQUEST),

    UNAUTHORIZED("LOGISTICS-SEC-001", "Không được phép truy cập", HttpStatus.UNAUTHORIZED),
    CANNOT_ACCESS("LOGISTICS-SEC-002", "Không thể truy cập tài nguyên được yêu cầu", HttpStatus.FORBIDDEN),
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
