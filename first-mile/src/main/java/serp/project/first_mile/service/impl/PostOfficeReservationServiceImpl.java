/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.dto.request.PostOfficeSuggestionRequest;
import serp.project.first_mile.dto.request.ReserveOriginPostOfficeRequest;
import serp.project.first_mile.dto.response.OrderDropOffPostOfficeSuggestionResponse;
import serp.project.first_mile.dto.response.OriginPostOfficeReservationResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.FirstMileAccessUtils;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.service.PostOfficeReservationService;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostOfficeReservationServiceImpl implements PostOfficeReservationService {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);
    private static final double EARTH_RADIUS_METERS = 6_371_000.0;
    private static final int DEFAULT_DROP_OFF_SUGGESTION_LIMIT = 5;
    private static final int MAX_DROP_OFF_SUGGESTION_LIMIT = 20;

    private final FirstMileAccessUtils firstMileAccessUtils;
    private final PostOfficeRepository postOfficeRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OriginPostOfficeReservationResponse reserveBestOriginPostOffice(ReserveOriginPostOfficeRequest request) {
        Point senderLocation = toSenderPoint(request);
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();

        PostOffice postOffice = postOfficeRepository.findBestAssignablePostOfficeForSenderForUpdate(
                        tenantId,
                        senderLocation,
                        LocalDate.now()
                )
                .orElseThrow(() -> new AppException(ErrorCode.NO_SUITABLE_ORIGIN_POST_OFFICE));

        postOffice.addLoad(1);
        return toResponse(postOfficeRepository.save(postOffice));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OriginPostOfficeReservationResponse reserveDropOffOriginPostOffice(
            Long postOfficeId,
            ReserveOriginPostOfficeRequest request
    ) {
        if (postOfficeId == null || postOfficeId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Point senderLocation = toSenderPoint(request);
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        firstMileAccessUtils.ensureCurrentManagerAssignedToPostOfficeOrThrow(postOfficeId, tenantId);

        PostOffice postOffice = postOfficeRepository.findByIdAndTenantIdForUpdate(postOfficeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));

        if (!isPostOfficeSuitableForSender(postOffice, LocalDate.now(), senderLocation)) {
            throw new AppException(ErrorCode.NO_SUITABLE_ORIGIN_POST_OFFICE);
        }

        postOffice.addLoad(1);
        return toResponse(postOfficeRepository.save(postOffice));
    }

    @Override
    public OriginPostOfficeReservationResponse validateManagedPostOffice(Long postOfficeId) {
        if (postOfficeId == null || postOfficeId <= 0) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        firstMileAccessUtils.ensureCurrentManagerAssignedToPostOfficeOrThrow(postOfficeId, tenantId);

        PostOffice postOffice = postOfficeRepository.findByIdAndTenantId(postOfficeId, tenantId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));

        return toResponse(postOffice);
    }

    @Override
    public List<OrderDropOffPostOfficeSuggestionResponse> suggestDropOffPostOffices(
            PostOfficeSuggestionRequest request
    ) {
        Point senderLocation = toSenderPoint(request);
        Long tenantId = firstMileAccessUtils.getCurrentTenantIdOrThrow();
        LocalDate operationalDate = LocalDate.now();
        double senderLatitude = senderLocation.getY();
        double senderLongitude = senderLocation.getX();

        return postOfficeRepository.findAllByTenantId(tenantId)
                .stream()
                .map(postOffice -> buildSuggestionCandidate(
                        postOffice,
                        operationalDate,
                        senderLatitude,
                        senderLongitude
                ))
                .flatMap(Optional::stream)
                .sorted(
                        Comparator.comparingInt(
                                        (DropOffSuggestionCandidate candidate) ->
                                                safePriority(candidate.postOffice().getPriority())
                                )
                                .thenComparingDouble(DropOffSuggestionCandidate::distanceMeters)
                                .thenComparingInt(candidate -> safeInt(candidate.postOffice().getCurrentLoad()))
                                .thenComparingLong(candidate -> candidate.postOffice().getId() == null
                                        ? Long.MAX_VALUE
                                        : candidate.postOffice().getId())
                )
                .limit(normalizeDropOffSuggestionLimit(request == null ? null : request.getLimit()))
                .map(this::toDropOffSuggestionResponse)
                .toList();
    }

    private Point toSenderPoint(ReserveOriginPostOfficeRequest request) {
        if (request == null
                || request.getSenderLatitude() == null
                || request.getSenderLongitude() == null
                || !isValidCoordinate(request.getSenderLatitude(), request.getSenderLongitude())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return GEOMETRY_FACTORY.createPoint(new Coordinate(
                request.getSenderLongitude(),
                request.getSenderLatitude()
        ));
    }

    private Point toSenderPoint(PostOfficeSuggestionRequest request) {
        if (request == null
                || request.getSenderLatitude() == null
                || request.getSenderLongitude() == null
                || !isValidCoordinate(request.getSenderLatitude(), request.getSenderLongitude())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        return GEOMETRY_FACTORY.createPoint(new Coordinate(
                request.getSenderLongitude(),
                request.getSenderLatitude()
        ));
    }

    private Optional<DropOffSuggestionCandidate> buildSuggestionCandidate(
            PostOffice postOffice,
            LocalDate operationalDate,
            double senderLatitude,
            double senderLongitude
    ) {
        if (postOffice == null || postOffice.getLocation() == null) {
            return Optional.empty();
        }

        Point postOfficeLocation = postOffice.getLocation();
        if (!isValidCoordinate(postOfficeLocation.getY(), postOfficeLocation.getX())) {
            return Optional.empty();
        }

        if (!isPostOfficeSuitableForSender(
                postOffice,
                operationalDate,
                toPoint(senderLatitude, senderLongitude)
        )) {
            return Optional.empty();
        }

        double distanceMeters = calculateDistanceMeters(
                senderLatitude,
                senderLongitude,
                postOfficeLocation.getY(),
                postOfficeLocation.getX()
        );

        return Optional.of(new DropOffSuggestionCandidate(postOffice, distanceMeters));
    }

    private OrderDropOffPostOfficeSuggestionResponse toDropOffSuggestionResponse(
            DropOffSuggestionCandidate candidate
    ) {
        PostOffice postOffice = candidate.postOffice();
        Point location = postOffice.getLocation();

        int currentLoad = safeInt(postOffice.getCurrentLoad());
        int dailyCapacity = safeInt(postOffice.getDailyCapacity());
        int remainingCapacity = Math.max(dailyCapacity - currentLoad, 0);

        return new OrderDropOffPostOfficeSuggestionResponse(
                postOffice.getId(),
                postOffice.getCode(),
                postOffice.getName(),
                postOffice.getProvinceCode(),
                postOffice.getWardCode(),
                postOffice.getAddressDetail(),
                postOffice.getPriority(),
                postOffice.getCurrentLoad(),
                postOffice.getDailyCapacity(),
                remainingCapacity,
                location == null ? null : round3(location.getY()),
                location == null ? null : round3(location.getX()),
                round3(candidate.distanceMeters())
        );
    }

    private Point toPoint(double latitude, double longitude) {
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    private boolean isPostOfficeSuitableForSender(PostOffice postOffice, LocalDate operationalDate, Point senderLocation) {
        if (postOffice == null
                || senderLocation == null
                || postOffice.getLocation() == null
                || postOffice.getDailyCapacity() == null
                || postOffice.getCurrentLoad() == null
                || !postOffice.isActive()
                || !isPostOfficeOperationalOnDate(postOffice, operationalDate)
                || !postOffice.canAccept(1)) {
            return false;
        }

        Integer serviceRadiusMeters = postOffice.getServiceRadiusM();
        if (serviceRadiusMeters == null || serviceRadiusMeters <= 0) {
            return false;
        }

        double distanceMeters = calculateDistanceMeters(
                senderLocation.getY(),
                senderLocation.getX(),
                postOffice.getLocation().getY(),
                postOffice.getLocation().getX()
        );

        return distanceMeters <= serviceRadiusMeters;
    }

    private boolean isPostOfficeOperationalOnDate(PostOffice postOffice, LocalDate operationalDate) {
        LocalDate startDate = postOffice.getOperationalStartDate();
        if (startDate != null && startDate.isAfter(operationalDate)) {
            return false;
        }

        LocalDate endDate = postOffice.getOperationalEndDate();
        return endDate == null || !endDate.isBefore(operationalDate);
    }

    private double calculateDistanceMeters(
            double fromLatitude,
            double fromLongitude,
            double toLatitude,
            double toLongitude
    ) {
        double latitudeDistance = Math.toRadians(toLatitude - fromLatitude);
        double longitudeDistance = Math.toRadians(toLongitude - fromLongitude);
        double fromLatitudeRadians = Math.toRadians(fromLatitude);
        double toLatitudeRadians = Math.toRadians(toLatitude);

        double a = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(fromLatitudeRadians)
                * Math.cos(toLatitudeRadians)
                * Math.sin(longitudeDistance / 2)
                * Math.sin(longitudeDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private boolean isValidCoordinate(double latitude, double longitude) {
        return !Double.isNaN(latitude)
                && !Double.isNaN(longitude)
                && !Double.isInfinite(latitude)
                && !Double.isInfinite(longitude)
                && latitude >= -90.0
                && latitude <= 90.0
                && longitude >= -180.0
                && longitude <= 180.0;
    }

    private OriginPostOfficeReservationResponse toResponse(PostOffice postOffice) {
        return new OriginPostOfficeReservationResponse(
                postOffice.getId(),
                postOffice.getCode(),
                postOffice.getName(),
                postOffice.getCurrentLoad(),
                postOffice.getDailyCapacity()
        );
    }

    private int normalizeDropOffSuggestionLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_DROP_OFF_SUGGESTION_LIMIT;
        }
        return Math.min(limit, MAX_DROP_OFF_SUGGESTION_LIMIT);
    }

    private int safePriority(Integer value) {
        return value == null ? Integer.MAX_VALUE : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }

    private record DropOffSuggestionCandidate(PostOffice postOffice, double distanceMeters) {
    }
}
