package serp.project.tms_payment_service.dto.payment;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateOrderRequest {

    @NotBlank(message = "App user cannot be blank")
    @Size(max = 50, message = "App user exceeds max length")
    private String appUser;

    @NotNull(message = "Amount cannot be null")
    @Min(value = 1000, message = "Amount must be >= 1000")
    private Long amount;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 256, message = "Description exceeds max length")
    private String description;

    @NotNull(message = "Items cannot be null")
    @NotEmpty(message = "Items must not be empty")
    private List<PaymentOrderItem> items;

    @Size(max = 20, message = "Bank code exceeds max length")
    private String bankCode;

    @Min(value = 300, message = "Expire duration must be >= 300")
    @Max(value = 2592000, message = "Expire duration must be <= 2592000")
    private Long expireDurationSeconds;

    private PaymentEmbedData embedData;

    @Size(max = 256, message = "Title exceeds max length")
    private String title;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Invalid phone format")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email exceeds max length")
    private String email;

    @Positive(message = "Tenant ID must be positive")
    private Long tenantId;

    @Positive(message = "Actor ID must be positive")
    private Long actorId;

    @Positive(message = "User ID must be positive")
    private Long userId;

    @Size(max = 1024, message = "Address exceeds max length")
    private String address;

    @Size(max = 50, message = "Sub app ID exceeds max length")
    private String subAppId;
}
