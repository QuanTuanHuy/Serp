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
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateLeadRequest {
    // Company information
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String company;
    
    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;
    
    @Size(max = 50, message = "Company size must not exceed 50 characters")
    private String companySize;
    
    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;

    // Contact information
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Job title must not exceed 100 characters")
    private String jobTitle;

    // Address
    @Size(max = 255, message = "Street must not exceed 255 characters")
    private String street;
    
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;
    
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;
    
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;
    
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    // Lead details
    private LeadSource leadSource;
    private LeadStatus leadStatus;
    private Long assignedTo;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated value must be greater than 0")
    private BigDecimal estimatedValue;
    
    @Min(value = 0, message = "Probability must be at least 0")
    @Max(value = 100, message = "Probability must not exceed 100")
    private Integer probability;
    
    @Future(message = "Expected close date must be in the future")
    private LocalDate expectedCloseDate;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
