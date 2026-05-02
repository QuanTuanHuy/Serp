/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.AccountTier;
import serp.project.crm.core.domain.enums.PreferredTimeSlot;

import java.time.DayOfWeek;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateAccountRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;
    
    @Size(max = 50, message = "Company size must not exceed 50 characters")
    private String companySize;
    
    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    // Address
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;
    
    @Size(max = 20, message = "Zip code must not exceed 20 characters")
    private String zipCode;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
    
    // Business details
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;
    
    @Size(max = 100, message = "Payment terms must not exceed 100 characters")
    private String paymentTerms;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private AccountTier tier;

    private List<PreferredTimeSlot> preferredTimeSlots;

    private List<DayOfWeek> preferredDays;

    @Size(max = 50, message = "Language must not exceed 50 characters")
    private String language;

    @Size(max = 100, message = "Timezone must not exceed 100 characters")
    private String timezone;
}
