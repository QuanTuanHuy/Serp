package serp.project.school_bus_service.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.school_bus_service.application.dto.response.GeneralResponse;
import serp.project.school_bus_service.application.dto.response.MapLocationResponse;
import serp.project.school_bus_service.core.service.IMapService;
import serp.project.school_bus_service.kernel.shared.auth.AuthUtils;
import serp.project.school_bus_service.kernel.shared.base.AbstractBaseController;

import java.util.List;

@RestController
@RequestMapping("/maps")
public class MapController extends AbstractBaseController {

    private final IMapService mapService;

    public MapController(IMapService mapService, AuthUtils authUtils) {
        super(authUtils);
        this.mapService = mapService;
    }

    @GetMapping("/geocode")
    public ResponseEntity<GeneralResponse<List<MapLocationResponse>>> geocode(
            @RequestParam("q") String query) {
        return ok("Fetched geocode results", mapService.geocode(query));
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<GeneralResponse<MapLocationResponse>> reverseGeocode(
            @RequestParam("lat") Double latitude,
            @RequestParam("lng") Double longitude) {
        return ok("Fetched reverse geocode result", mapService.reverseGeocode(latitude, longitude));
    }
}
