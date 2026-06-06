/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostOfficeStaffRequest {

    @JsonProperty("max_daily_stops")
    @PositiveOrZero
    private Integer maxDailyStops;

    @JsonProperty("max_daily_parcels")
    @PositiveOrZero
    private Integer maxDailyParcels;

    @JsonProperty("phone_number")
    @Size(max = 15)
    private String phoneNumber;

    @JsonProperty("notes")
    private String notes;
}