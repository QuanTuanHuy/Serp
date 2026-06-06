package serp.project.payment_service.enums;

import lombok.Getter;

/**
 * Enum chứa các mã lỗi chi tiết (sub_return_code) từ ZaloPay Query API
 * Theo tài liệu: https://sb-openapi.zalopay.vn POST /v2/query
 */
@Getter
public enum ZaloPaySubReturnCode {
    
    TIME_INVALID(-54, "Đơn hàng đã hết hạn thanh toán", 
            "Merchant tạo lại đơn hàng khác"),
    
    ZPW_BALANCE_NOT_ENOUGH(-63, "Đơn hàng thanh toán thất bại do tài khoản người dùng không đủ tiền", 
            "Trong trường hợp đơn hàng còn hạn thanh toán, người dùng có thể nạp thêm tiền vào tài khoản và thực hiện thanh toán lại đơn hàng"),
    
    APPTRANSID_INVALID(-92, "Giá trị app_trans_id trong yêu cầu truy vấn không đúng định dạng", 
            "Merchant kiểm tra và thử lại với yêu cầu truy vấn với giá trị app_trans_id hợp lệ"),
    
    ORDER_NOT_EXIST(-101, "Giá trị app_trans_id không tồn tại", 
            "Merchant kiểm tra và thử lại yêu cầu truy vấn với giá trị app_trans_id khác"),
    
    BANK_ERROR(-217, "Lỗi từ hệ thống Ngân hàng phát hành thẻ", 
            "Merchant cần liên hệ Zalopay để biết thêm chi tiết"),
    
    PROMOTION_ERROR_332(-332, "Đơn hàng thanh toán thất bại do thể lệ chương trình khuyến mã", 
            "Merchant cần liên hệ Zalopay để biết thêm chi tiết"),
    
    PROMOTION_ERROR_333(-333, "Đơn hàng thanh toán thất bại do thể lệ chương trình khuyến mã", 
            "Merchant cần liên hệ Zalopay để biết thêm chi tiết"),
    
    ILLEGAL_DATA_REQUEST(-401, "Các tham số trong yêu cầu sai định dạng", 
            "Khởi tạo yêu cầu truy vấn không thành công. Merchant cần kiểm tra định dạng của các tham số hoặc các tham số còn thiếu và khởi tạo lại yêu câu truy vấn với giá trị các tham số hợp lệ"),
    
    ILLEGAL_APP_OR_SIGNATURE(-402, "Xác thực thông tin merchant thất bại", 
            "Khởi tạo yêu cầu truy vấn không thành công. Merchant cần kiểm tra thông tin kết nối, chữ ký xác thực đối chiếu với các thông tin được cung cấp từ Zalopay và khởi tạo lại yêu cầu truy vấn với thông tin xác thực hợp lệ"),
    
    LIMIT_REQUEST_REACH(-429, "Yêu cầu bị từ chối vì vượt quá quá tần suất cho phép", 
            "Khởi tạo yêu cầu truy vấn không thành công. Merchant cần tạo lại yêu cầu truy vấn sau một khoảng thời gian"),
    
    SYSTEM_ERROR(-500, "Hệ thống gặp sự cố", 
            "Khởi tạo yêu cầu truy vấn không thành công. Merchant liên hệ Zalopay để biết thêm chi tiết"),
    
    SYSTEM_MAINTENANCE(-999, "Hệ thống đang được bảo trì", 
            "Khởi tạo yêu cầu truy vấn không thành công. Merchant cần khởi tạo lại yêu cầu truy vấn sau khi bảo trì đã hoàn tất"),
    
    EXCEED_MAX_FUND_OUT_PER_DAY_1330(-1330, "Đơn hàng thanh toán thất bại do tài khoản người dùng vượt quá hạn mức giao dịch trong một ngày", 
            "Người dùng có thể thanh toán vào các ngày kế tiếp"),
    
    EXCEED_MAX_FUND_OUT_PER_DAY_1331(-1331, "Đơn hàng thanh toán thất bại do tài khoản người dùng vượt quá hạn mức giao dịch trong một ngày", 
            "Người dùng có thể thanh toán vào các ngày kế tiếp"),
    
    EXCEED_MAX_FUND_OUT_PER_DAY_1332(-1332, "Đơn hàng thanh toán thất bại do tài khoản người dùng vượt quá hạn mức giao dịch trong một ngày", 
            "Người dùng có thể thanh toán vào các ngày kế tiếp"),
    
    EXCEED_MAX_FUND_OUT_PER_DAY_1333(-1333, "Đơn hàng thanh toán thất bại do tài khoản người dùng vượt quá hạn mức giao dịch trong một ngày", 
            "Người dùng có thể thanh toán vào các ngày kế tiếp"),
    
    EXCEED_MAX_FUND_OUT_PER_MONTH_1340(-1340, "Đơn hàng thất bại do tài khoản người dùng vượt quá hạn mức giao dịch trong một tháng", 
            "Người dùng có thể thanh toán vào các tháng kế tiếp"),
    
    EXCEED_MAX_FUND_OUT_PER_MONTH_1341(-1341, "Đơn hàng thất bại do tài khoản người dùng vượt quá hạn mức giao dịch trong một tháng", 
            "Người dùng có thể thanh toán vào các tháng kế tiếp"),
    
    EXCEED_MAX_FUND_OUT_PER_MONTH_1342(-1342, "Đơn hàng thất bại do tài khoản người dùng vượt quá hạn mức giao dịch trong một tháng", 
            "Người dùng có thể thanh toán vào các tháng kế tiếp"),
    
    EXCEED_MAX_FUND_OUT_PER_MONTH_1343(-1343, "Đơn hàng thất bại do tài khoản người dùng vượt quá hạn mức giao dịch trong một tháng", 
            "Người dùng có thể thanh toán vào các tháng kế tiếp"),
    
    // ========== Refund Error Codes ==========
    
    REFUND_PENDING(-1, "Hoàn tiền chờ phê duyệt", 
            "Yêu cầu hoàn tiền đang chờ phê duyệt. Merchant liên hệ Zalopay để biết thêm chi tiết"),
    
    REFUND_TYPE_INVALID(-2, "Đơn hàng không được phép hoàn tiền", 
            "Hoàn tiền không thành công. Merchant liên hệ Zalopay để biết thêm chi tiết"),
    
    REFUND_EXPIRED(-13, "Vượt quá thời hạn cho phép của việc khởi tạo yêu cầu hoàn tiền", 
            "Merchant kiểm tra và khởi tạo lại yêu cầu hoàn tiền với giá trị tham số timestamp hợp lệ (không quá 15 phút với thời điểm khởi tạo yêu cầu)"),
    
    REFUND_AMOUNT_INVALID(-14, "Số tiền yêu cầu hoàn không hợp lệ", 
            "Hoàn tiền không thành công. Merchant cần kiểm tra và khởi tạo lại yêu cầu hoàn tiền với số tiền hợp lệ"),
    
    INSERT_REFUND_LOG_AR_FAIL(-16, "Hoàn tiền đang được xử lý", 
            "Hoàn tiền đang xử lý. Merchant liên hệ Zalopay để biết thêm chi tiết"),
    
    NOT_SUPPORT_PARTIAL_REFUND(-32, "Giao dịch không hỗ trợ hoàn tiền một phần", 
            "Hoàn tiền từng phần cho thanh toán qua ví điện tử (trừ ZaloPay)"),
    
    M_REFUND_ID_NOT_FOUND(-101, "Giá trị m_refund_id không tồn tại", 
            "Merchant kiểm tra và thử lại yêu cầu truy vấn hoàn tiền với giá trị m_refund_id khác"),
    
    UNKNOWN(0, "Mã lỗi không xác định", "Vui lòng liên hệ hỗ trợ");
    
    private final int code;
    private final String description;
    private final String note;
    
    ZaloPaySubReturnCode(int code, String description, String note) {
        this.code = code;
        this.description = description;
        this.note = note;
    }
    
    /**
     * Lấy enum từ code
     */
    public static ZaloPaySubReturnCode fromCode(int code) {
        for (ZaloPaySubReturnCode subCode : values()) {
            if (subCode.code == code) {
                return subCode;
            }
        }
        return UNKNOWN;
    }
    
    /**
     * Kiểm tra xem có phải lỗi từ phía người dùng không
     * (có thể retry sau khi user thực hiện hành động)
     */
    public boolean isUserActionable() {
        return this == ZPW_BALANCE_NOT_ENOUGH || 
               this == EXCEED_MAX_FUND_OUT_PER_DAY_1330 ||
               this == EXCEED_MAX_FUND_OUT_PER_DAY_1331 ||
               this == EXCEED_MAX_FUND_OUT_PER_DAY_1332 ||
               this == EXCEED_MAX_FUND_OUT_PER_DAY_1333 ||
               this == EXCEED_MAX_FUND_OUT_PER_MONTH_1340 ||
               this == EXCEED_MAX_FUND_OUT_PER_MONTH_1341 ||
               this == EXCEED_MAX_FUND_OUT_PER_MONTH_1342 ||
               this == EXCEED_MAX_FUND_OUT_PER_MONTH_1343;
    }
    
    /**
     * Kiểm tra xem có phải lỗi từ phía merchant không
     * (cần merchant kiểm tra và sửa)
     */
    public boolean isMerchantError() {
        return this == APPTRANSID_INVALID || 
               this == ORDER_NOT_EXIST ||
               this == ILLEGAL_DATA_REQUEST ||
               this == ILLEGAL_APP_OR_SIGNATURE;
    }
    
    /**
     * Kiểm tra xem có phải lỗi hệ thống không
     * (có thể retry sau)
     */
    public boolean isSystemError() {
        return this == SYSTEM_ERROR || 
               this == SYSTEM_MAINTENANCE ||
               this == LIMIT_REQUEST_REACH;
    }
    
    /**
     * Kiểm tra xem có nên retry không
     */
    public boolean shouldRetry() {
        return isSystemError() || this == LIMIT_REQUEST_REACH;
    }
    
    /**
     * Lấy thông báo đầy đủ (description + note)
     */
    public String getFullMessage() {
        return description + ". " + note;
    }
}
