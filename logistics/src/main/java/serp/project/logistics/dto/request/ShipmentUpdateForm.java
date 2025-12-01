package serp.project.logistics.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ShipmentUpdateForm {

    private String shipmentName;

    private String note;

    private LocalDate expectedDeliveryDate;

}
