package com.example.ttcrs.dto.request.transportplan;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.example.ttcrs.dto.request.resource.ResourceReturnDepotDTO;

/**
 * Input DTO for the "Create Transport Plan" wizard (Step 2 → final submit).
 *
 * <p>All resource selections made by the dispatcher are sent in one payload so the
 * entire creation can be handled inside a single {@code @Transactional} method.
 *
 * <p>Each resource carries its own list of allowed return depots via
 * {@link ResourceReturnDepotDTO}, so the algorithm can apply per-resource
 * constraints instead of a single shared depot set.
 *
 * <p>Source/origin depots (where containers currently are) are derived automatically
 * from the selected containers' {@code currentLocationCode} — they are NOT supplied
 * here.
 */
@Getter
@Setter
public class CreateTransportPlanInputDTO {

    /** IDs of the PLANNED requests to include in this transport plan. */
    @NotEmpty(message = "requestIds must not be empty")
    private List<Long> requestIds;

    /**
     * Per-container return depot configuration.
     * Each entry maps one container (by DB id) to its allowed return depot codes.
     */
    private List<ResourceReturnDepotDTO> containerReturnDepots;

    /** Per-trailer return depot configuration. */
    private List<ResourceReturnDepotDTO> trailerReturnDepots;

    /** Per-truck return depot configuration. */
    private List<ResourceReturnDepotDTO> truckReturnDepots;

    /** IDs of the trucks the dispatcher has made available for this plan. */
    private List<Long> truckIds;

    /** IDs of the trailers the dispatcher has made available for this plan. */
    private List<Long> trailerIds;

    /** IDs of the containers the dispatcher has made available for this plan. */
    private List<Long> containerIds;
}
