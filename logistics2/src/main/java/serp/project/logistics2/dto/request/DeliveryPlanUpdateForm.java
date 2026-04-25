package serp.project.logistics2.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class DeliveryPlanUpdateForm {

    List<String> additionalDeliverySlipIds;
    List<String> additionalVehicleShipperIds;
    List<String> removedDeliverySlipIds;
    List<String> removedVehicleShipperIds;

}
