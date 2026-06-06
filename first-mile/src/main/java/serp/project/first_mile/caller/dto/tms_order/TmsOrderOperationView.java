/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller.dto.tms_order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import serp.project.first_mile.enums.OrderPickupMethod;
import serp.project.first_mile.enums.OrderStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsOrderOperationView {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private Long id;
    private String orderCode;
    private String customerOrderCode;
    private OrderStatus status;
    private Boolean isConfirm;
    private String originPostOfficeCode;
    private String senderName;
    private String senderPhone;
    private String senderProvinceCode;
    private String senderWardCode;
    private String senderAddressDetail;
    private Double senderLatitude;
    private Double senderLongitude;
    private LocalDateTime pickupTimeStart;
    private LocalDateTime pickupTimeEnd;
    private Double totalWeight;
    private Double totalVolume;
    private OrderPickupMethod pickupMethod;
    private String createdBy;
    private LocalDateTime createdAt;
    private Long tenantId;

    @JsonIgnore
    public Point getSenderLocation() {
        if (senderLatitude == null || senderLongitude == null) {
            return null;
        }
        return GEOMETRY_FACTORY.createPoint(new Coordinate(senderLongitude, senderLatitude));
    }
}
