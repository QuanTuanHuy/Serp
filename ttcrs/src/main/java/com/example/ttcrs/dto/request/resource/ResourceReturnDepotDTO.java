package com.example.ttcrs.dto.request.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.example.ttcrs.dto.request.transportplan.CreateTransportPlanInputDTO;

/**
 * Maps one resource (truck, trailer, or container) to its allowed return depots.
 *
 * <p>Sent by the dispatcher in {@link CreateTransportPlanInputDTO} so that
 * each resource can have an independent set of return depot options, matching
 * the {@code returnDepotCodes} field in the algorithm's equipment models.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceReturnDepotDTO {

    /** DB id of the resource (truck / trailer / container). */
    private Long resourceId;

    /**
     * Location codes of depots this resource is allowed to return to
     * after completing its services.
     */
    private List<String> returnDepotCodes;
}
