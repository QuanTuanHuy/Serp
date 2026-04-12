package serp.project.school_bus_service.core.service;

import serp.project.school_bus_service.application.dto.response.MapLocationResponse;

import java.util.List;

public interface IMapService {

    List<MapLocationResponse> geocode(String query);

    MapLocationResponse reverseGeocode(Double latitude, Double longitude);
}
