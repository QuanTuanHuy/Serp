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
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.ContactType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateContactRequest {
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Job position must not exceed 100 characters")
    private String jobPosition;
    
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
    
    private ContactType contactType;
    private ActiveStatus activeStatus;
    
    @Size(max = 255, message = "LinkedIn URL must not exceed 255 characters")
    private String linkedInUrl;
    
    @Size(max = 50, message = "Twitter handle must not exceed 50 characters")
    private String twitterHandle;
    
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
