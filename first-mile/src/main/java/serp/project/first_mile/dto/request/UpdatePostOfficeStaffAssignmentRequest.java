/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostOfficeStaffAssignmentRequest {

    @JsonProperty("assigned_from")
    private LocalDate assignedFrom;

    @JsonProperty("assigned_to")
    private LocalDate assignedTo;

    @JsonProperty("is_primary")
    private Boolean isPrimary;

    @JsonProperty("notes")
    private String notes;
}
