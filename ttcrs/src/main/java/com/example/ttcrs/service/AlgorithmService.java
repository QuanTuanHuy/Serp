package com.example.ttcrs.service;

import com.example.ttcrs.algorithm.models.equipments.Container;
import com.example.ttcrs.algorithm.models.equipments.Mooc;
import com.example.ttcrs.algorithm.models.equipments.MoocGroup;
import com.example.ttcrs.algorithm.models.equipments.Truck;
import com.example.ttcrs.algorithm.models.input.ConfigParam;
import com.example.ttcrs.algorithm.models.input.ContainerTruckMoocInput;
import com.example.ttcrs.algorithm.models.input.DistanceElement;
import com.example.ttcrs.algorithm.models.output.TruckMoocContainerOutputJson;
import com.example.ttcrs.algorithm.models.places.*;
import com.example.ttcrs.algorithm.models.requests.*;
import com.example.ttcrs.algorithm.solver.TruckContainerSolver;
import com.example.ttcrs.algorithm.solver.init.FPIUSInit;
import com.example.ttcrs.algorithm.solver.opt.ALNS;
import com.example.ttcrs.algorithm.vrp.Checkin;
import com.example.ttcrs.algorithm.vrp.Intervals;
import com.example.ttcrs.constant.RequestType;
import com.example.ttcrs.dto.request.transportplan.CreateTransportPlanInputDTO;
import com.example.ttcrs.entity.*;
import com.example.ttcrs.repository.*;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds the algorithm input from dispatcher selections, computes a real-world
 * distance matrix via the public OSRM routing API, executes the
 * {@link TruckContainerSolver}, and returns the JSON-serialised solution.
 *
 * <p><b>Thread-safety note:</b> {@code TruckContainerSolver} uses static fields
 * ({@code nVehicle}, {@code nRequest}). Concurrent plan-creation requests on the
 * same JVM instance will interfere. For now this is acceptable for single-dispatcher
 * use; wrap the solver block in {@code synchronized(AlgorithmService.class)} once
 * concurrent load is expected.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlgorithmService {

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String OSRM_TABLE_URL =
            "http://router.project-osrm.org/table/v1/driving/";

    /**
     * Fixed path where the algorithm input JSON is always written for debugging.
     * Overwritten on every plan-creation call.
     */
    private static final String DEBUG_INPUT_PATH =
            "src/main/java/com/example/ttcrs/algorithm/data/truck-container/input/formatted-input-sample.json";

    /**
     * Fixed path where the algorithm output JSON is always written for debugging.
     * Overwritten on every plan-creation call.
     */
    private static final String DEBUG_OUTPUT_PATH =
        "src/main/java/com/example/ttcrs/algorithm/data/truck-container/output/formatted-output-sample.json";

    private final RequestRepository    requestRepository;
    private final ContainerRepository  containerRepository;
    private final TruckRepository      truckRepository;
    private final TrailerRepository    trailerRepository;
    private final LocationRepository   locationRepository;

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Maps dispatcher input → algorithm input → runs solver → returns JSON result.
     *
     * @param dto      the full plan input from the dispatcher
     * @param tenantId resolved tenant (already validated in RequestService)
     * @return Gson-serialised {@link TruckMoocContainerOutputJson}
     */
    public synchronized String executeAlgorithm(CreateTransportPlanInputDTO dto, Long tenantId) {

        // ------------------------------------------------------------------ //
        // A. Load entities
        // ------------------------------------------------------------------ //
        List<RequestEntity> requests = requestRepository.findAllById(dto.getRequestIds());

        List<ContainerEntity> containers = dto.getContainerIds() == null || dto.getContainerIds().isEmpty()
                ? List.of()
                : containerRepository.findAllById(dto.getContainerIds());

        List<TruckEntity> trucks = dto.getTruckIds() == null || dto.getTruckIds().isEmpty()
                ? List.of()
                : truckRepository.findAllById(dto.getTruckIds());

        List<TrailerEntity> trailers = dto.getTrailerIds() == null || dto.getTrailerIds().isEmpty()
                ? List.of()
                : trailerRepository.findAllById(dto.getTrailerIds());

        Map<String, LocationEntity> locationMap = locationRepository
                .findAllByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(LocationEntity::getLocationCode, l -> l, (a, b) -> a));

        log.info("Algorithm input: {} requests, {} containers, {} trucks, {} trailers",
                requests.size(), containers.size(), trucks.size(), trailers.size());

        if (requests.isEmpty()) {
            throw new IllegalArgumentException("At least one request must be selected");
        }
        if (trucks.isEmpty()) {
            throw new IllegalArgumentException("At least one truck must be selected");
        }

        // ------------------------------------------------------------------ //
        // B. Map requests → algorithm request types
        // ------------------------------------------------------------------ //
        List<ExportEmptyRequests> exEmpty = new ArrayList<>();
        List<ExportLadenRequests> exLaden = new ArrayList<>();
        List<ImportEmptyRequests> imEmpty = new ArrayList<>();
        List<ImportLadenRequests> imLaden = new ArrayList<>();

        LocalDateTime defaultEarly = LocalDateTime.now();
        LocalDateTime defaultLate  = LocalDateTime.now().plusDays(1000);

        int reqIdx = 0;
        for (RequestEntity req : requests) {
            int id = reqIdx++;
            String src  = req.getSrcLocationCode();
            String dest = req.getDestLocationCode();
            String earlyAtSrc  = fmt(req.getEarlyAtSrc(),  defaultEarly);
            String lateAtSrc   = fmt(req.getLateAtSrc(),   defaultLate);
            String earlyAtDest = fmt(req.getEarlyAtDest(), defaultEarly);
            String lateAtDest  = fmt(req.getLateAtDest(),  defaultLate);
            double weight = req.getWeight() != null ? req.getWeight() : 0.0;

            if (req.getType() == RequestType.OE) {
                // Export Empty: pickup container at depot (src), load at warehouse (dest)
                exEmpty.add(new ExportEmptyRequests(
                        id, false, "", "", "", "", "", "", "", "",
                        earlyAtSrc, lateAtSrc, earlyAtDest, lateAtDest,
                        "", weight, src, dest, 0, 0));

            } else if (req.getType() == RequestType.OF) {
                // Export Laden: attach at warehouse (src), unload at port (dest)
                exLaden.add(new ExportLadenRequests(
                        id, false, "", "", "", "", "", "", "", "",
                        earlyAtSrc, lateAtDest,
                        "", weight, src, dest, 0, 0, 0));

            } else if (req.getType() == RequestType.IE) {
                // Import Empty: receive at warehouse (src), return empty to depot (dest)
                imEmpty.add(new ImportEmptyRequests(
                        id, false, "", "", "", "", "", "", "", "",
                        earlyAtSrc, lateAtDest,
                        "", weight, src, dest, 0, 0));

            } else if (req.getType() == RequestType.IF) {
                // Import Laden: pickup at port (src), unload at warehouse (dest)
                imLaden.add(new ImportLadenRequests(
                        id, false, "", "", "", "", "", "", "", "",
                        earlyAtSrc, lateAtSrc, earlyAtDest, lateAtDest,
                        "", weight, src, dest, 0, 0));
            }
        }

        // ------------------------------------------------------------------ //
        // C. Build places
        // ------------------------------------------------------------------ //

        // Build per-resource return depot maps (resourceId → String[])
        Map<Long, String[]> containerReturnMap = toReturnDepotMap(dto.getContainerReturnDepots());
        Map<Long, String[]> trailerReturnMap   = toReturnDepotMap(dto.getTrailerReturnDepots());
        Map<Long, String[]> truckReturnMap     = toReturnDepotMap(dto.getTruckReturnDepots());

        // Container depots = current locations of selected containers + all per-container return depots
        Set<String> containerDepotCodes = new LinkedHashSet<>();
        containers.forEach(c -> { if (c.getCurrentLocationCode() != null) containerDepotCodes.add(c.getCurrentLocationCode()); });
        containerReturnMap.values().forEach(codes -> { for (String c : codes) containerDepotCodes.add(c); });

        // Trailer (mooc) depots
        Set<String> trailerDepotCodes = new LinkedHashSet<>();
        trailers.forEach(t -> { if (t.getCurrentLocationCode() != null) trailerDepotCodes.add(t.getCurrentLocationCode()); });
        trailerReturnMap.values().forEach(codes -> { for (String c : codes) trailerDepotCodes.add(c); });

        // Truck depots
        Set<String> truckDepotCodes = new LinkedHashSet<>();
        trucks.forEach(t -> { if (t.getCurrentLocationCode() != null) truckDepotCodes.add(t.getCurrentLocationCode()); });
        truckReturnMap.values().forEach(codes -> { for (String c : codes) truckDepotCodes.add(c); });

        // Warehouses — extracted from requests
        Set<String> warehouseCodes = new LinkedHashSet<>();
        for (RequestEntity req : requests) {
            if (req.getType() == RequestType.OF || req.getType() == RequestType.IE)
                warehouseCodes.add(req.getSrcLocationCode());
            if (req.getType() == RequestType.OE || req.getType() == RequestType.IF)
                warehouseCodes.add(req.getDestLocationCode());
        }

        // Ports — extracted from requests
        Set<String> portCodes = new LinkedHashSet<>();
        for (RequestEntity req : requests) {
            if (req.getType() == RequestType.OF) portCodes.add(req.getDestLocationCode());
            if (req.getType() == RequestType.IF) portCodes.add(req.getSrcLocationCode());
        }

        DepotContainer[] depotContainerArr = containerDepotCodes.stream()
                .map(c -> new DepotContainer(c, c, 0, 0))
                .toArray(DepotContainer[]::new);

        DepotMooc[] depotMoocArr = trailerDepotCodes.stream()
                .map(c -> new DepotMooc(c, c, 0, 0))
                .toArray(DepotMooc[]::new);

        DepotTruck[] depotTruckArr = truckDepotCodes.stream()
                .map(c -> new DepotTruck(c, c))
                .toArray(DepotTruck[]::new);

        Warehouse[] warehouseArr = warehouseCodes.stream()
                .map(c -> new Warehouse(c, c, 0, 0,
                        new int[0], new int[0], new Checkin[0], new Intervals[0]))
                .toArray(Warehouse[]::new);

        Port[] portArr = portCodes.stream()
                .map(c -> new Port(c, c))
                .toArray(Port[]::new);

        // ------------------------------------------------------------------ //
        // D. Build equipment — each resource gets its own return depot array
        // ------------------------------------------------------------------ //
        Container[] containerArr = containers.stream()
                .map(c -> new Container(
                        c.getCode(), 0.0, "",
                        c.getCurrentLocationCode() != null ? c.getCurrentLocationCode() : "",
                        containerReturnMap.getOrDefault(c.getId(), new String[0]), false))
                .toArray(Container[]::new);

        Mooc[] moocArr = trailers.stream()
                .map(t -> {
                    String depot = t.getCurrentLocationCode() != null ? t.getCurrentLocationCode() : "";
                    return new Mooc(0, t.getCode(), "", 0, 0.0, "", 0,
                            depot, depot,
                            trailerReturnMap.getOrDefault(t.getId(), new String[0]),
                            new Intervals[0]);
                })
                .toArray(Mooc[]::new);

        String nowStr      = LocalDateTime.now().format(DT_FMT);
        String tomorrowStr = LocalDateTime.of(9999, 12, 31, 23, 59, 59).format(DT_FMT);

        Truck[] truckArr = trucks.stream()
                .map(t -> {
                    String depot = t.getCurrentLocationCode() != null ? t.getCurrentLocationCode() : "";
                    return new Truck(0, t.getCode(), 0.0, 0, "", "", depot, depot,
                            nowStr, tomorrowStr, "",
                            truckReturnMap.getOrDefault(t.getId(), new String[0]),
                            new Intervals[0]);
                })
                .toArray(Truck[]::new);

        // ------------------------------------------------------------------ //
        // E. Compute OSRM distance matrix
        // ------------------------------------------------------------------ //
        Set<String> allLocationCodes = new LinkedHashSet<>();
        allLocationCodes.addAll(containerDepotCodes);
        allLocationCodes.addAll(trailerDepotCodes);
        allLocationCodes.addAll(truckDepotCodes);
        allLocationCodes.addAll(warehouseCodes);
        allLocationCodes.addAll(portCodes);

        // Filter to locations that have coordinates
        List<String> geoLocations = allLocationCodes.stream()
                .filter(code -> {
                    LocationEntity loc = locationMap.get(code);
                    boolean hasCoords = loc != null && loc.getLat() != null && loc.getLng() != null;
                    if (!hasCoords) log.warn("Location '{}' has no coordinates — skipped from OSRM query", code);
                    return hasCoords;
                })
                .collect(Collectors.toList());

        DistanceElement[] distanceElements;
        if (geoLocations.size() < 2) {
            log.warn("Fewer than 2 geolocated locations — using dummy distance matrix");
            distanceElements = buildDummyMatrix(new ArrayList<>(allLocationCodes));
        } else {
            distanceElements = fetchOsrmMatrix(geoLocations, locationMap);
        }

        // ------------------------------------------------------------------ //
        // F. ConfigParam
        // ------------------------------------------------------------------ //
        ConfigParam params = new ConfigParam(
                30, 30, 0, 0, null,
                false, false, false, false, false, false,
                0, 0, 0, 0,
                LocalDateTime.now().format(DT_FMT));

        // ------------------------------------------------------------------ //
        // G. Assemble ContainerTruckMoocInput
        // ------------------------------------------------------------------ //
        ContainerTruckMoocInput input = new ContainerTruckMoocInput(
                new com.example.ttcrs.algorithm.models.requests.ExportContainerTruckMoocRequest[0],
                new com.example.ttcrs.algorithm.models.requests.ImportContainerTruckMoocRequest[0],
                new com.example.ttcrs.algorithm.models.requests.WarehouseTransportRequest[0],
                exEmpty.toArray(ExportEmptyRequests[]::new),
                exLaden.toArray(ExportLadenRequests[]::new),
                imEmpty.toArray(ImportEmptyRequests[]::new),
                imLaden.toArray(ImportLadenRequests[]::new),
                new com.example.ttcrs.algorithm.models.places.ShipCompany[0],
                depotContainerArr,
                depotMoocArr,
                depotTruckArr,
                warehouseArr,
                truckArr,
                moocArr,
                new MoocGroup[0],
                portArr,
                containerArr,
                distanceElements,
                distanceElements,   // same array used for both distance and travelTime
                params);

        log.info("Built ContainerTruckMoocInput: exEmpty={}, exLaden={}, imEmpty={}, imLaden={}, " +
                        "trucks={}, moocs={}, containers={}, depotContainers={}, depotMoocs={}, depotTrucks={}, " +
                        "warehouses={}, ports={}, distanceElements={}",
                exEmpty.size(), exLaden.size(), imEmpty.size(), imLaden.size(),
                truckArr.length, moocArr.length, containerArr.length,
                depotContainerArr.length, depotMoocArr.length, depotTruckArr.length,
                warehouseArr.length, portArr.length, distanceElements.length);

        // ------------------------------------------------------------------ //
        // H. Dump input JSON to a fixed debug file (overwritten every call)
        // ------------------------------------------------------------------ //
        try {
            String debugJson = new Gson().toJson(input);
            Path debugPath = Path.of(DEBUG_INPUT_PATH);
            Files.createDirectories(debugPath.getParent());
            Files.writeString(debugPath, debugJson);
            log.info("Debug input JSON written to: {}", debugPath.toAbsolutePath());
        } catch (Exception e) {
            log.warn("Could not write debug input JSON: {}", e.getMessage());
        }

        // ------------------------------------------------------------------ //
        // I. Run solver
        // ------------------------------------------------------------------ //
        return runSolver(input);
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private String runSolver(ContainerTruckMoocInput input) {
        Gson gson = new Gson();
        Path tmpIn  = null;
        Path tmpOut = null;
        try {
            tmpIn  = Files.createTempFile("plan-input-",  ".json");
            tmpOut = Files.createTempFile("plan-output-", ".txt");

            Files.writeString(tmpIn, gson.toJson(input));
            log.info("Wrote algorithm input to temp file: {}", tmpIn);

            TruckContainerSolver solver = new TruckContainerSolver();
            solver.readData(tmpIn.toString());
            solver.init();
            solver.stateModel();

            int nReq = TruckContainerSolver.nRequest;
            solver.lower_removal = Math.max(1, (int) (0.01 * nReq));
            solver.upper_removal = Math.max(1, (int) (0.25 * nReq));
            solver.timeLimit     = 60_000;
            solver.nIter         = 100;
            solver.maxStable     = 500;
            solver.sigma1 = 5;
            solver.sigma2 = 1;
            solver.sigma3 = (int)0.01;
            solver.nRemovalOperators = 8;
			solver.nInsertionOperators = 8;
            solver.rp = 0.1;
            solver.nw = 1;
            solver.shaw1st = 0.5;
            solver.shaw2nd = 0.2;
            solver.	shaw3rd = 0.1;
    
            solver.temperature = 200;
            solver.cooling_rate = 0.9995;
            solver.nTabu = 5;


            solver.setInitializationStrategy(new FPIUSInit());
            solver.initializeSolution();

            solver.setOptimizationStrategy(new ALNS(solver));
            solver.optimizeSolution(tmpOut.toString());

            TruckMoocContainerOutputJson result = solver.createFormatedSolution();
            String json = gson.toJson(result);

            try {
                Path debugOutputPath = Path.of(DEBUG_OUTPUT_PATH);
                Files.createDirectories(debugOutputPath.getParent());
                Files.writeString(debugOutputPath, json);
                log.info("Debug output JSON written to: {}", debugOutputPath.toAbsolutePath());
            } catch (Exception e) {
                log.warn("Could not write debug output JSON: {}", e.getMessage());
            }

            log.info("Algorithm completed. Result JSON length={}", json.length());
            return json;

        } catch (Exception e) {
            log.error("Algorithm execution failed", e);
            throw new RuntimeException("Algorithm execution failed: " + e.getMessage(), e);
        } finally {
            silentDelete(tmpIn);
            silentDelete(tmpOut);
        }
    }

    /**
     * Calls the public OSRM Table API and converts the response into a flat
     * {@link DistanceElement} array covering all i→j pairs.
     */
    private DistanceElement[] fetchOsrmMatrix(List<String> codes,
                                               Map<String, LocationEntity> locationMap) {
        // Build coordinate string: lng,lat;lng,lat;...
        StringBuilder coordsBuilder = new StringBuilder();
        for (int i = 0; i < codes.size(); i++) {
            LocationEntity loc = locationMap.get(codes.get(i));
            if (i > 0) coordsBuilder.append(';');
            coordsBuilder.append(loc.getLng()).append(',').append(loc.getLat());
        }

        String url = OSRM_TABLE_URL + coordsBuilder + "?annotations=duration,distance";
        log.info("Calling OSRM: {}", url);

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("OSRM returned status {} — falling back to dummy matrix", response.statusCode());
                return buildDummyMatrix(codes);
            }

            OsrmTableResponse osrm = new Gson().fromJson(response.body(), OsrmTableResponse.class);
            if (!"Ok".equals(osrm.code)) {
                log.warn("OSRM code='{}' — falling back to dummy matrix", osrm.code);
                return buildDummyMatrix(codes);
            }

            int n = codes.size();
            List<DistanceElement> elements = new ArrayList<>(n * n);
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    double dist = osrm.distances != null ? osrm.distances[i][j] : 0.0;
                    double dur  = osrm.durations != null ? osrm.durations[i][j] : 0.0;
                    elements.add(new DistanceElement(
                            codes.get(i), codes.get(j),
                            false, new int[0],
                            dist, dur, dist, dur));
                }
            }
            log.info("OSRM matrix built: {}×{} = {} elements", n, n, elements.size());
            return elements.toArray(DistanceElement[]::new);

        } catch (Exception e) {
            log.warn("OSRM call failed ({}), using dummy matrix", e.getMessage());
            return buildDummyMatrix(codes);
        }
    }

    /**
     * Fallback when OSRM is unavailable. Inserts 0 for same-location pairs
     * and a large constant (999 km / 3600 s) for all other pairs so the
     * algorithm can at least run without crashing.
     */
    private DistanceElement[] buildDummyMatrix(List<String> codes) {
        log.warn("Using dummy distance matrix for {} locations", codes.size());
        List<DistanceElement> elements = new ArrayList<>(codes.size() * codes.size());
        for (int i = 0; i < codes.size(); i++) {
            for (int j = 0; j < codes.size(); j++) {
                double dist = (i == j) ? 0.0 : 999_000.0;
                double dur  = (i == j) ? 0.0 : 3_600.0;
                elements.add(new DistanceElement(
                        codes.get(i), codes.get(j),
                        false, new int[0],
                        dist, dur, dist, dur));
            }
        }
        return elements.toArray(DistanceElement[]::new);
    }

    private String fmt(LocalDateTime dt, LocalDateTime fallback) {
        return (dt != null ? dt : fallback).format(DT_FMT);
    }

    /**
     * Converts a list of {@link ResourceReturnDepotDTO} entries into a map from
     * resource id → {@code String[]} of return depot codes.
     *
     * <p>Safe against {@code null} input and entries with null ids or null code lists.
     */
    private Map<Long, String[]> toReturnDepotMap(List<com.example.ttcrs.dto.request.resource.ResourceReturnDepotDTO> configs) {
        if (configs == null || configs.isEmpty()) return Map.of();
        return configs.stream()
                .filter(c -> c.getResourceId() != null && c.getReturnDepotCodes() != null)
                .collect(Collectors.toMap(
                        com.example.ttcrs.dto.request.resource.ResourceReturnDepotDTO::getResourceId,
                        c -> c.getReturnDepotCodes().toArray(new String[0]),
                        (a, b) -> a));
    }

    private void silentDelete(Path path) {
        if (path != null) {
            try { Files.deleteIfExists(path); } catch (Exception ignored) {}
        }
    }

    // ------------------------------------------------------------------ //
    // Inner class for OSRM JSON response deserialization
    // ------------------------------------------------------------------ //

    @SuppressWarnings("unused")
    private static class OsrmTableResponse {
        String     code;
        double[][] durations;
        double[][] distances;
    }
}
