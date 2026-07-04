package serp.project.logistics2.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AppErrorCode {
        UNIMPLEMENTED("LOGISTICS2-APP-001", "Tính năng chưa được triển khai", HttpStatus.NOT_IMPLEMENTED),
        UNEXPECTED_EXCEPTION("LOGISTICS2-APP-002", "Đã xảy ra lỗi không mong muốn", HttpStatus.INTERNAL_SERVER_ERROR),

        NOT_FOUND("LOGISTICS2-BIZ-001", "Không tìm thấy đối tượng hoặc không có quyền truy cập", HttpStatus.NOT_FOUND),
        INVALID_STATUS_TRANSITION("LOGISTICS2-BIZ-002", "Chuyển trạng thái không hợp lệ", HttpStatus.BAD_REQUEST),
        ORDER_NOT_APPROVED_YET("LOGISTICS2-BIZ-004", "Đơn hàng chưa được phê duyệt", HttpStatus.BAD_REQUEST),
        EXCEED_REMAINING_QUANTITY("LOGISTICS2-BIZ-005", "Số lượng vượt quá mức cho phép còn lại",
                        HttpStatus.BAD_REQUEST),
        DATA_INTEGRITY_VIOLATION("LOGISTICS2-BIZ-006", "Vi phạm toàn vẹn dữ liệu", HttpStatus.BAD_REQUEST),
        PLAN_IN_OPTIMIZATION("LOGISTICS2-BIZ-007", "Kế hoạch giao hàng hiện đang được tối ưu hóa",
                        HttpStatus.BAD_REQUEST),
        DELIVERY_SLIP_ALREADY_ASSIGNED("LOGISTICS2-BIZ-008", "Phiếu giao hàng đã được gán",
                        HttpStatus.BAD_REQUEST),
        VEHICLE_SHIPPER_NOT_AVAILABLE("LOGISTICS2-BIZ-009",
                        "Phân công xe và người giao hàng không khả dụng cho ngày giao hàng",
                        HttpStatus.BAD_REQUEST),
        CANNOT_DELETE_DELIVERY_PLAN("LOGISTICS2-BIZ-010",
                        "Không thể xóa kế hoạch giao hàng khi trạng thái tối ưu hóa là OPTIMIZING hoặc cao hơn",
                        HttpStatus.BAD_REQUEST),
        ROUTE_ALREADY_SELECTED("LOGISTICS2-BIZ-011", "Tuyến đường đã được chọn cho giao hàng",
                        HttpStatus.BAD_REQUEST),
        PREVIOUS_STOP_NOT_ARRIVED("LOGISTICS2-BIZ-012", "Điểm dừng trước đó chưa đến nơi",
                        HttpStatus.BAD_REQUEST),
        ANOTHER_ROUTE_IN_PROGRESS("LOGISTICS2-BIZ-013",
                        "Một tuyến đường khác đang được thực hiện cho cùng xe và người giao hàng. Vui lòng hoàn tất tuyến đó trước khi chọn tuyến mới.",
                        HttpStatus.BAD_REQUEST),
        DELIVERY_SLIP_NOT_AVAILABLE("LOGISTICS2-BIZ-014",
                        "Phiếu giao hàng không khả dụng để phân công", HttpStatus.BAD_REQUEST),
        VEHICLE_SHIPPER_NOT_FOUND("LOGISTICS2-BIZ-015",
                        "Không tìm thấy phân công xe và người giao hàng hoặc không có quyền truy cập",
                        HttpStatus.NOT_FOUND),
        INSUFFICIENT_QUANTITY("LOGISTICS2-BIZ-016", "Số lượng không đủ", HttpStatus.BAD_REQUEST),
        VEHICLE_ALREADY_ASSIGNED("LOGISTICS2-BIZ-017", "Xe đã được phân công cho ngày đã chọn", HttpStatus.BAD_REQUEST),
        EMPTY_DELIVERY_SLIPS("LOGISTICS2-BIZ-018", "Kế hoạch giao hàng phải có ít nhất 01 phiếu giao hàng",
                        HttpStatus.BAD_REQUEST),
        EMPTY_VEHICLE_SHIPPERS("LOGISTICS2-BIZ-019", "Kế hoạch giao hàng phải có ít nhất 01 xe giao hàng",
                        HttpStatus.BAD_REQUEST),

        UNKNOWN_ENUM_VALUE("LOGISTICS2-VAL-001", "Giá trị không xác định", HttpStatus.BAD_REQUEST),
        REQUEST_VALIDATION_FAILED("LOGISTICS2-VAL-002", "Xác thực yêu cầu không thành công", HttpStatus.BAD_REQUEST),

        UNAUTHORIZED("LOGISTICS2-SEC-001", "Không được phép truy cập", HttpStatus.UNAUTHORIZED),
        CANNOT_ACCESS("LOGISTICS2-SEC-002", "Không thể truy cập tài nguyên được yêu cầu", HttpStatus.FORBIDDEN),
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
