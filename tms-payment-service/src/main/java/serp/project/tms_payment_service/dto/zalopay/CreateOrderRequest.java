package serp.project.tms_payment_service.dto.zalopay;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    /**
     * ID người dùng/username/tên/số điện thoại/email
     */
    @NotBlank(message = "App user không được để trống")
    @Size(max = 50, message = "App user không được vượt quá 50 ký tự")
    private String appUser;
    
    /**
     * Giá trị đơn hàng (VND)
     */
    @NotNull(message = "Amount không được để trống")
    @Min(value = 1000, message = "Amount phải >= 1000 VND")
    private Long amount;
    
    /**
     * Mô tả đơn hàng
     */
    @NotBlank(message = "Description không được để trống")
    @Size(max = 256, message = "Description không được vượt quá 256 ký tự")
    private String description;
    
    /**
     * Danh sách sản phẩm trong đơn hàng
     */
    @NotNull(message = "Items không được để trống")
    @NotEmpty(message = "Items phải có ít nhất 1 sản phẩm")
    private List<OrderItem> items;
    
    /**
     * Mã ngân hàng (không bắt buộc cho QR, App to App)
     * Bắt buộc là "zalopayapp" cho Mobile Web to App
     */
    @Size(max = 20, message = "Bank code không được vượt quá 20 ký tự")
    private String bankCode;
    
    /**
     * Thời gian hết hạn đơn hàng (giây)
     * Min: 300s, Max: 2592000s
     */
    @Min(value = 300, message = "Expire duration phải >= 300 giây")
    @Max(value = 2592000, message = "Expire duration phải <= 2592000 giây")
    private Long expireDurationSeconds;
    
    /**
     * Dữ liệu embed tùy chỉnh
     */
    private EmbedData embedData;
    
    /**
     * Tiêu đề đơn hàng
     */
    @Size(max = 256, message = "Title không được vượt quá 256 ký tự")
    private String title;
    
    /**
     * Số điện thoại người dùng
     */
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone phải là số điện thoại hợp lệ (10-11 số)")
    private String phone;
    
    /**
     * Email người dùng
     */
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    /**
     * Tenant ID để tích hợp event thông báo nội bộ
     */
    @Positive(message = "Tenant ID phải là số dương")
    private Long tenantId;

    /**
     * Actor ID khởi tạo giao dịch (optional)
     */
    @Positive(message = "Actor ID phải là số dương")
    private Long actorId;

    /**
     * User ID nhận thông báo in-app (optional)
     */
    @Positive(message = "User ID phải là số dương")
    private Long userId;
    
    /**
     * Địa chỉ người dùng
     */
    @Size(max = 1024, message = "Address không được vượt quá 1024 ký tự")
    private String address;
    
    /**
     * Sub App ID (chỉ áp dụng với một số đối tác đặc biệt)
     */
    @Size(max = 50, message = "Sub app ID không được vượt quá 50 ký tự")
    private String subAppId;
}
