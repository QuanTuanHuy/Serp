package serp.project.logistics2.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryPlanCreationForm {

    @NotBlank(message = "Facility ID must not be blank")
    private String facilityId;

    @NotNull(message = "Delivery date must not be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;

    @NotEmpty(message = "Delivery slip IDs must not be empty")
    private List<String> deliverySlipIds;

    @NotEmpty(message = "Vehicle-shipper assignments must not be empty")
    private List<String> vehicleShipperIds;

}
