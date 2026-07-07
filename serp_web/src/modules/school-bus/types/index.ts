export interface ApiResponse<T> {
  code: number;
  status: string;
  message: string;
  data: T;
}

export interface PagedResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface SchoolBusListParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
  keyword?: string;
  depotId?: number;
  homeDepotId?: number;
  schoolId?: number;
  parentProfileId?: number;
}

export interface SchoolBusBaseRecord {
  id: number;
  tenantId?: number;
  isActive?: boolean;
  isDeleted?: boolean;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
}

export interface LinkedPickupPointSummary {
  id: number;
  linkId?: number;
  code?: string | null;
  name?: string | null;
  address?: string | null;
  usageType?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  hasCoordinates?: boolean;
  isDefault?: boolean;
}

export interface SchoolBusSchool extends SchoolBusBaseRecord {
  name: string;
  code?: string | null;
  address?: string | null;
  contactPhone?: string | null;
  contactEmail?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  pickupPoints?: LinkedPickupPointSummary[];
  pickupPointCount?: number;
  hasCoordinates?: boolean;
  anyLinkedPointMissingCoordinates?: boolean;
}

export interface SchoolBusParent extends SchoolBusBaseRecord {
  userId: number;
  schoolBusUserId?: number | null;
  accountUserId?: number | null;
  fullName: string;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  studentCount?: number | null;
  user?: SchoolBusAccountUser | null;
}

export interface SchoolBusAccountUser {
  id: number;
  keycloakId?: string | null;
  email: string;
  firstName?: string | null;
  lastName?: string | null;
  phoneNumber?: string | null;
  organizationId?: number | null;
  organizationName?: string | null;
  userType?: string | null;
  status?: string | null;
  roles?: string[];
}

export interface SchoolBusStudent extends SchoolBusBaseRecord {
  schoolId: number;
  schoolName: string;
  parentProfileId: number;
  parentProfileName: string;
  /** Default pickup point (legacy: pickupPointId) */
  pickupPointId?: number | null;
  pickupPointName?: string | null;
  defaultDropoffPointId?: number | null;
  defaultDropoffPointName?: string | null;
  fullName: string;
  studentCode?: string | null;
  grade?: string | null;
  className?: string | null;
  homeAddress?: string | null;
  dateOfBirth?: string | null;
  gender?: string | null;
  specialNote?: string | null;
}

export interface SchoolBusBus extends SchoolBusBaseRecord {
  plateNumber: string;
  busType?: string | null;
  capacity: number;
  status: string;
  homeDepotId?: number | null;
  homeDepotName?: string | null;
}

export interface SchoolBusBusType {
  code: string;
  value: number;
  description: string;
}

export interface SchoolBusDriver extends SchoolBusBaseRecord {
  userId: number;
  schoolBusUserId?: number | null;
  accountUserId?: number | null;
  fullName: string;
  phone?: string | null;
  licenseNumber?: string | null;
  licenseClass?: string | null;
  licenseExpiryDate?: string | null;
  status: string;
  user?: SchoolBusAccountUser | null;
}

export interface SchoolBusAttendant extends SchoolBusBaseRecord {
  userId: number;
  schoolBusUserId?: number | null;
  accountUserId?: number | null;
  fullName: string;
  phone?: string | null;
  status: string;
  user?: SchoolBusAccountUser | null;
}

export interface SchoolBusPickupPoint extends SchoolBusBaseRecord {
  name: string;
  address: string;
  latitude?: number | null;
  longitude?: number | null;
  code?: string | null;
  usageType?: string | null;
  pickupInstruction?: string | null;
  schools?: PickupPointLinkedSchool[];
}

export interface PickupPointLinkedSchool {
  id: number;
  code: string;
  name: string;
}

export interface SchoolBusDepot extends SchoolBusBaseRecord {
  code?: string | null;
  name: string;
  address?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  contactPhone?: string | null;
  description?: string | null;
}

export interface SchoolBusFleetSummary {
  totalBuses: number;
  availableBuses: number;
  totalDrivers: number;
  availableDrivers: number;
  unavailableDrivers: number;
  totalAttendants: number;
  availableAttendants: number;
  unavailableAttendants: number;
  totalDepots: number;
  depotsWithCoordinates: number;
}

export interface SchoolBusSchoolRegistrySummary {
  totalSchools: number;
  totalPickupPoints: number;
  linkedPickupPoints: number;
  missingCoordinates: number;
}

export interface SchoolBusStudentSummary {
  totalStudents: number;
  linkedSchools: number;
  linkedParents: number;
  activeStudents: number;
}

export interface SchoolBusParentSummary {
  totalParents: number;
  withEmail: number;
  withPhone: number;
  activeParents: number;
}

export interface SchoolBusTransportRequestSummary {
  totalRequests: number;
  submittedRequests: number;
  approvedRequests: number;
  rejectedRequests: number;
}

export interface SchoolBusSubscriptionSummary {
  totalSubscriptions: number;
  activeSubscriptions: number;
  inactiveSubscriptions: number;
}

export interface SchoolBusRouteDispatchSummary {
  totalRoutes: number;
  plannedRoutes: number;
  tripCreatedRoutes: number;
}

export interface SchoolBusTripListSummary {
  totalTrips: number;
  inProgressTrips: number;
  completedTrips: number;
}

export interface SchoolBusRequestStudent extends SchoolBusBaseRecord {
  requestId: number;
  studentId: number;
  studentCode?: string | null;
  studentName: string;
  pickupPointId?: number | null;
  pickupPointName?: string | null;
  pickupPointAddress?: string | null;
  pickupPointLatitude?: number | null;
  pickupPointLongitude?: number | null;
  dropoffPointId?: number | null;
  dropoffPointName?: string | null;
  dropoffPointAddress?: string | null;
  dropoffPointLatitude?: number | null;
  dropoffPointLongitude?: number | null;
  tripOption?: string | null;
  monday?: boolean;
  tuesday?: boolean;
  wednesday?: boolean;
  thursday?: boolean;
  friday?: boolean;
  saturday?: boolean;
  sunday?: boolean;
  subscriptionId?: number | null;
  subscriptionCode?: string | null;
  targetSubscriptionId?: number | null;
  targetSubscriptionCode?: string | null;
  studentNote?: string | null;
}

export interface SchoolBusTransportRequest extends SchoolBusBaseRecord {
  requestCode?: string | null;
  parentProfileId: number;
  parentProfileName: string;
  schoolId: number;
  schoolName: string;
  schoolLatitude?: number | null;
  schoolLongitude?: number | null;
  requestType: string;
  status: string;
  effectiveFrom: string;
  effectiveTo?: string | null;
  notes?: string | null;
  requestedAt?: string | null;
  requestSource?: string | null;
  changeReason?: string | null;
  approvedBy?: number | null;
  approvedAt?: string | null;
  rejectionReason?: string | null;
}

export interface SchoolBusTransportRequestDetail {
  request: SchoolBusTransportRequest;
  students: SchoolBusRequestStudent[];
}

export interface SchoolBusSubscriptionPausePeriod extends SchoolBusBaseRecord {
  subscriptionId: number;
  sourceRequestId?: number | null;
  requestStudentId?: number | null;
  pauseFrom: string;
  pauseTo?: string | null;
  status: string;
  reason?: string | null;
}

export interface SchoolBusRoute extends SchoolBusBaseRecord {
  schoolId: number;
  schoolName: string;
  schoolLatitude?: number | null;
  schoolLongitude?: number | null;
  routeDirection: 'OUTBOUND' | 'RETURN';
  startLocationType: 'SCHOOL' | 'DEPOT';
  startLocationId: number;
  startLocationName: string;
  startLocationAddress?: string | null;
  startLocationLatitude?: number | null;
  startLocationLongitude?: number | null;
  endLocationType: 'SCHOOL' | 'DEPOT';
  endLocationId: number;
  endLocationName: string;
  endLocationAddress?: string | null;
  endLocationLatitude?: number | null;
  endLocationLongitude?: number | null;
  routeCode: string;
  routeName: string;
  serviceDate: string;
  status: string;
  plannedDistanceKm?: number | null;
  plannedDurationMin?: number | null;
  plannedStudentCount?: number | null;
  busId?: number | null;
  busPlateNumber?: string | null;
  busName?: string | null;
  busCapacity?: number | null;
  busStatus?: string | null;
  stopsCount?: number | null;
  driverId?: number | null;
  driverName?: string | null;
  attendantId?: number | null;
  attendantName?: string | null;
  startDepotName?: string | null;
  versionNo?: number | null;
  planningNotes?: string | null;
  geometryPath?: string | null;
  geometrySource?: 'OSRM' | 'HAVERSINE_FALLBACK' | 'MANUAL' | 'UNKNOWN' | null;
  fallbackUsed?: boolean | null;
  startedAt?: string | null;
  completedAt?: string | null;
  planningSessionId?: number | null;
}

export interface SchoolBusRoutePathCoordinate {
  latitude: number;
  longitude: number;
}

export interface SchoolBusRoutePath {
  routeId: number;
  provider?: string | null;
  estimated?: boolean | null;
  fallbackUsed?: boolean | null;
  geometrySource?: 'OSRM' | 'HAVERSINE_FALLBACK' | 'MANUAL' | 'UNKNOWN' | null;
  distanceKm?: number | null;
  durationMin?: number | null;
  warning?: string | null;
  coordinates: SchoolBusRoutePathCoordinate[];
}

export interface SchoolBusRouteStop extends SchoolBusBaseRecord {
  routeId: number;
  locationType?: string | null; // DEPOT | SCHOOL | PICKUP_POINT
  stopPurpose?: string | null; // START_TERMINAL | PICKUP | DROPOFF | END_TERMINAL
  displayName?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  pickupPointId?: number | null;
  pickupPointName?: string | null;
  pickupPointAddress?: string | null;
  pickupPointLatitude?: number | null;
  pickupPointLongitude?: number | null;
  schoolId?: number | null;
  schoolName?: string | null;
  depotId?: number | null;
  depotName?: string | null;
  stopType?: 'PICKUP' | 'DROPOFF'; // legacy compat
  stopOrder: number;
  estimatedStudentCount?: number | null;
  distanceFromPreviousKm?: number | null;
  estimatedTravelTimeFromPrevious?: number | null;
}

export interface SchoolBusRouteAssignment extends SchoolBusBaseRecord {
  routeId: number;
  routeStatus?: string | null;
  busId: number;
  busPlateNumber: string;
  busCapacity?: number | null;
  driverId: number;
  driverName: string;
  driverLicenseClass?: string | null;
  driverLicenseExpiryDate?: string | null;
  attendantId?: number | null;
  attendantName?: string | null;
  status?: string | null;
  assignedBy?: number | null;
  assignmentNote?: string | null;
  assignedAt?: string | null;
  confirmedAt?: string | null;
  capacityWarning?: string | null;
  licenseWarning?: string | null;
}

export interface SchoolBusRoutePlanStudent {
  id: number;
  routeId: number;
  studentId: number;
  studentName: string;
  subscriptionId?: number | null;
  pickupStopId?: number | null;
  pickupPointName?: string | null;
  dropoffStopId?: number | null;
  dropoffPointName?: string | null;
}

export interface SchoolBusRouteDetail {
  route: SchoolBusRoute;
  stops: SchoolBusRouteStop[];
  students: SchoolBusRoutePlanStudent[];
  assignment: SchoolBusRouteAssignment | null;
}

export interface SchoolBusRouteMapDetail {
  route: SchoolBusRoute;
  stops: SchoolBusRouteStop[];
  assignment: SchoolBusRouteAssignment | null;
  path: SchoolBusRoutePath | null;
}

export interface SchoolBusAttendance extends SchoolBusBaseRecord {
  routeId: number;
  routeCode: string;
  tripId?: number | null;
  routeStopId?: number | null;
  studentId: number;
  studentName: string;
  attendanceType: string;
  eventType?: string | null;
  eventSource?: string | null;
  status: string;
  recordedAt: string;
  recordedBy: number;
  notes?: string | null;
}

export interface SchoolBusSubscription extends SchoolBusBaseRecord {
  subscriptionCode: string;
  studentId: number;
  studentName: string;
  studentCode?: string | null;
  parentName?: string | null;
  schoolId: number;
  schoolName: string;
  schoolCode?: string | null;
  pickupPointId?: number | null;
  pickupPointName?: string | null;
  pickupPointCode?: string | null;
  dropoffPointId?: number | null;
  dropoffPointName?: string | null;
  dropoffPointCode?: string | null;
  tripOption: 'MORNING' | 'AFTERNOON' | 'ROUND_TRIP';
  status: string;
  monday: boolean;
  tuesday: boolean;
  wednesday: boolean;
  thursday: boolean;
  friday: boolean;
  saturday: boolean;
  sunday: boolean;
  effectiveFrom: string;
  effectiveTo?: string | null;
  sourceRequestId?: number | null;
  sourceRequestCode?: string | null;
}

export interface SchoolBusTripStopLog extends SchoolBusBaseRecord {
  tripId: number;
  routeStopId: number;
  stopName: string;
  stopOrder: number;
  status: string;
  actualArrivalTime?: string | null;
  actualDepartureTime?: string | null;
  delayMinutes?: number | null;
  actualBoardedCount: number;
  actualDroppedCount: number;
  note?: string | null;
}

export interface SchoolBusTripStudent extends SchoolBusBaseRecord {
  tripId: number;
  studentId: number;
  studentName: string;
  pickupStopId?: number | null;
  dropoffStopId?: number | null;
  subscriptionId?: number | null;
  status: string;
  note?: string | null;
}

  export interface SchoolBusTripExecution extends SchoolBusBaseRecord {
  tripCode: string;
  routeId: number;
  routeCode: string;
  routeName: string;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
  status: string;
  plannedStartAt?: string | null;
  plannedEndAt?: string | null;
  startedAt?: string | null;
  completedAt?: string | null;
  completionNote?: string | null;
  cancelledAt?: string | null;
  cancelledBy?: number | null;
  cancellationReason?: string | null;
  busId?: number | null;
  busPlateNumber?: string | null;
  driverId?: number | null;
  driverName?: string | null;
  attendantId?: number | null;
  attendantName?: string | null;
  stops: SchoolBusTripStopLog[];
  students: SchoolBusTripStudent[];
  startLocationType?: string | null;
  startLocationName?: string | null;
  endLocationType?: string | null;
    endLocationName?: string | null;
  }

  export interface SchoolBusTripExecutionListItem extends SchoolBusBaseRecord {
    tripCode: string;
    routeId: number;
    routeCode: string;
    routeName: string;
    serviceDate: string;
    routeDirection: 'OUTBOUND' | 'RETURN';
    status: string;
    plannedStartAt?: string | null;
    plannedEndAt?: string | null;
    startedAt?: string | null;
    completedAt?: string | null;
    cancelledAt?: string | null;
    cancellationReason?: string | null;
    busId?: number | null;
    busPlateNumber?: string | null;
    driverId?: number | null;
    driverName?: string | null;
    attendantId?: number | null;
    attendantName?: string | null;
    startLocationType?: string | null;
    startLocationName?: string | null;
    endLocationType?: string | null;
    endLocationName?: string | null;
    totalStops: number;
    completedStops: number;
    nextRouteStopId?: number | null;
    nextStopName?: string | null;
    nextStopStatus?: string | null;
  }

export interface SchoolBusTripOperationAction {
  tripId: number;
  tripStatus?: string | null;
  routeStopId?: number | null;
  stopStatus?: string | null;
  actualArrivalTime?: string | null;
  actualDepartureTime?: string | null;
  updatedAt?: string | null;
  message?: string | null;
}
  
  export interface SchoolBusCapacityUtilization {
  tripId: number;
  tripCode: string;
  routeCode: string;
  plannedStudents: number;
  busCapacity: number;
  utilizationPercent: number;
}

export interface TripAttendanceStopItem {
  routeStopId: number;
  stopOrder: number;
  locationType?: string | null; // DEPOT | SCHOOL | PICKUP_POINT
  stopPurpose?: string | null; // START_TERMINAL | PICKUP | DROPOFF | END_TERMINAL
  displayName?: string | null;
  stopName?: string | null; // legacy compat
  stopStatus: string;
  plannedBoardingCount: number;
  plannedDropoffCount: number;
  actualBoardedCount: number;
  actualDroppedCount: number;
  latitude?: number | null;
  longitude?: number | null;
  plannedArrivalTime?: string | null;
  plannedDepartureTime?: string | null;
  actualArrivalTime?: string | null;
  actualDepartureTime?: string | null;
  studentCount?: number | null;
}

export interface TripAttendanceStudentItem {
  tripStudentId: number;
  studentId: number;
  studentName?: string | null;
  studentCode?: string | null;
  status: string;
  pickupStopId?: number | null;
  dropoffStopId?: number | null;
  subscriptionId?: number | null;
  note?: string | null;
}

export interface SchoolBusTripAttendanceSummary {
  totalStudents: number;
  planned: number;
  boarded: number;
  droppedOff: number;
  absent: number;
  noShow: number;
  notServed: number;
}

export interface SchoolBusTripAttendanceManifest {
  tripId: number;
  tripCode?: string | null;
  routeId?: number | null;
  routeCode?: string | null;
  routeName?: string | null;
  routeDirection?: string | null;
  tripStatus: string;
  serviceDate?: string | null;
  routeGeometry?: string | null;
  distanceKm?: number | null;
  durationMin?: number | null;
  summary: SchoolBusTripAttendanceSummary;
  stops: TripAttendanceStopItem[];
  students: TripAttendanceStudentItem[];
}

export interface SchoolBusTripOperationOverview {
  tripId: number;
  tripCode?: string | null;
  routeId?: number | null;
  routeCode?: string | null;
  routeName?: string | null;
  routeDirection?: string | null;
  tripStatus: string;
  serviceDate?: string | null;
  busPlateNumber?: string | null;
  driverName?: string | null;
  attendantName?: string | null;
  routeGeometry?: string | null;
  distanceKm?: number | null;
  durationMin?: number | null;
  cancellationReason?: string | null;
  summary: SchoolBusTripAttendanceSummary;
  stops: TripAttendanceStopItem[];
}

export interface DashboardSummary {
  schoolCount: number;
  parentCount: number;
  studentCount: number;
  busCount: number;
  pendingRequestCount: number;
  assignedRouteCount: number;
  inProgressRouteCount: number;
  completedTripCount: number;
}

export interface OperationalReport {
  totalRequests: number;
  approvedRequests: number;
  rejectedRequests: number;
  activeRoutes: number;
  completedRoutes: number;
  attendanceEvents: number;
  auditEvents: number;
}

export interface SchoolBusReportOverview {
  totalRequests: number;
  approvedRequests: number;
  completedTrips: number;
  attendanceEvents: number;
  tripCount: number;
  attendanceCount: number;
  capacityCount: number;
}

export interface SchoolBusSchoolUpsertRequest {
  name: string;
  address?: string;
  contactPhone?: string;
  contactEmail?: string;
  latitude?: number | null;
  longitude?: number | null;
  isActive?: boolean;
}

export interface SchoolBusParentUpsertRequest {
  accountUserId: number;
  fullName: string;
  phone?: string;
  email?: string;
  address?: string;
  isActive?: boolean;
}

export interface SchoolBusStudentUpsertRequest {
  schoolId: number;
  parentProfileId: number;
  /** Default pickup point (legacy field name) */
  pickupPointId?: number | null;
  defaultDropoffPointId?: number | null;
  fullName: string;
  grade?: string;
  className?: string;
  homeAddress?: string;
  dateOfBirth?: string | null;
  gender?: string | null;
  specialNote?: string;
  isActive?: boolean;
}

export interface SchoolBusBusUpsertRequest {
  plateNumber: string;
  busType?: string;
  capacity: number;
  status: string;
  homeDepotId?: number | null;
  isActive?: boolean;
}

export interface SchoolBusDriverUpsertRequest {
  accountUserId: number;
  fullName: string;
  phone?: string;
  licenseNumber: string;
  licenseClass: string;
  licenseExpiryDate: string;
  status: string;
  isActive?: boolean;
}

export interface SchoolBusAttendantUpsertRequest {
  accountUserId: number;
  fullName: string;
  phone?: string;
  status: string;
  isActive?: boolean;
}

export interface SchoolBusPickupPointUpsertRequest {
  name: string;
  address: string;
  latitude?: number | null;
  longitude?: number | null;
  code?: string | null;
  usageType?: string | null;
  pickupInstruction?: string | null;
  isActive?: boolean;
}

export interface SchoolBusDepotUpsertRequest {
  name: string;
  address?: string;
  latitude?: number | null;
  longitude?: number | null;
  contactPhone?: string;
  description?: string;
  isActive?: boolean;
}

export interface SchoolBusTransportRequestStudentInput {
  studentId: number;
  pickupPointId?: number | null;
  dropoffPointId?: number | null;
  tripOption?: string | null;
  monday?: boolean;
  tuesday?: boolean;
  wednesday?: boolean;
  thursday?: boolean;
  friday?: boolean;
  saturday?: boolean;
  sunday?: boolean;
  targetSubscriptionId?: number | null;
  studentNote?: string | null;
  isActive?: boolean;
}

export interface SchoolBusTransportRequestUpsertRequest {
  parentProfileId: number;
  schoolId: number;
  requestType: string;
  effectiveFrom: string;
  effectiveTo?: string | null;
  notes?: string;
  changeReason?: string;
  students: SchoolBusTransportRequestStudentInput[];
  isActive?: boolean;
}

export interface SchoolBusSubscriptionUpsertRequest {
  studentId: number;
  schoolId: number;
  pickupPointId?: number | null;
  dropoffPointId?: number | null;
  tripOption: 'MORNING' | 'AFTERNOON' | 'ROUND_TRIP';
  effectiveFrom: string;
  effectiveTo?: string | null;
  status?: string;
  monday?: boolean;
  tuesday?: boolean;
  wednesday?: boolean;
  thursday?: boolean;
  friday?: boolean;
  saturday?: boolean;
  sunday?: boolean;
  isActive?: boolean;
}

export interface SchoolBusRejectRequest {
  reason: string;
  isActive?: boolean;
}

export interface SchoolBusRouteUpsertRequest {
  schoolId: number;
  routeDirection: 'OUTBOUND' | 'RETURN';
  startLocationType: 'SCHOOL' | 'DEPOT';
  startSchoolId?: number | null;
  startDepotId?: number | null;
  endLocationType: 'SCHOOL' | 'DEPOT';
  endSchoolId?: number | null;
  endDepotId?: number | null;
  routeName: string;
  serviceDate: string;
  planningNotes?: string;
  isActive?: boolean;
}

export interface SchoolBusRouteAssignmentRequest {
  busId?: number | null;
  driverId: number;
  attendantId?: number | null;
  assignmentNote?: string | null;
  reason?: string | null;
  isActive?: boolean;
}

export interface SchoolBusManualDispatchRequest
  extends SchoolBusRouteAssignmentRequest {
  orderedStopIds?: number[];
  notes?: string;
}

export interface SchoolBusTripAttendanceActionRequest {
  routeStopId: number;
  studentId: number;
  notes?: string;
  isActive?: boolean;
}


export interface SchoolBusBatchAttendanceRequest {
  action: 'MARK_BOARDED' | 'MARK_ABSENT' | 'MARK_NO_SHOW';
  studentIds: number[];
  note?: string;
}

export interface SchoolBusBatchAttendanceUpdatedStudent {
  studentId: number;
  status: string;
}

export interface SchoolBusBatchAttendanceResponse {
  updatedCount: number;
  skippedCount: number;
  updatedStudents: SchoolBusBatchAttendanceUpdatedStudent[];
}

export interface SchoolBusMapAddressParts {
  [key: string]: string;
}

export interface SchoolBusMapLocation {
  displayName?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  addressParts?: SchoolBusMapAddressParts;
}

// --- School <-> Pickup Point link ---

export interface SchoolBusSchoolPickupPoint extends SchoolBusBaseRecord {
  schoolId: number;
  schoolName: string;
  pickupPointId: number;
  pickupPointName: string;
  pickupPointAddress?: string | null;
  pickupPointLatitude?: number | null;
  pickupPointLongitude?: number | null;
  pickupPointUsageType?: string | null;
  isDefault?: boolean;
}

export interface SchoolBusSchoolPickupPointUpsertRequest {
  pickupPointId: number;
  isDefault?: boolean;
  isActive?: boolean;
}

// -- Planning Session ------------------------------------------------------

export type PlanningSessionStatus =
  | 'DRAFT'
  | 'GENERATED'
  | 'REVIEWING'
  | 'PUBLISHED'
  | 'CANCELLED';

export interface SchoolBusPlanningSession extends SchoolBusBaseRecord {
  schoolId: number;
  schoolName: string;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
  status: PlanningSessionStatus;
  totalEligibleStudents: number;
  totalPlannedStudents: number;
  totalUnassignedStudents: number;
  totalRoutes: number;
  totalStops: number;
  totalDistanceKm?: number | null;
  totalDurationMin?: number | null;
  publishedAt?: string | null;
  planningNotes?: string | null;
}

export interface SchoolBusEligibleStudent {
  studentId: number;
  studentName: string;
  studentCode?: string;
  subscriptionId: number;
  subscriptionCode?: string;
  tripOption: string;
  pickupPointId?: number;
  pickupPointName?: string;
  pickupPointLatitude?: number;
  pickupPointLongitude?: number;
  dropoffPointId?: number;
  dropoffPointName?: string;
  dropoffPointLatitude?: number;
  dropoffPointLongitude?: number;
  relevantPointId?: number;
  relevantPointName?: string;
  relevantPointLatitude?: number;
  relevantPointLongitude?: number;
  specialNote?: string;
  assigned?: boolean;
  assignedRouteId?: number;
}

export interface SchoolBusPlanningPickupPoint {
  pickupPointId: number;
  pickupPointName: string;
  latitude?: number;
  longitude?: number;
  studentCount: number;
}

export interface PlanningReadinessSummary {
  totalSubscriptions: number;
  eligibleStudents: number;
  pointCount: number;
  pickupPointCount: number;
  dropoffPointCount: number;
}

export interface PlanningDemandResponse {
  subscriptionId: number;
  subscriptionCode: string;
  studentId: number;
  studentCode: string;
  studentName: string;
  schoolId: number;
  schoolName: string;
  tripOption: string;
  tripOptionLabel?: string;
  pointId?: number;
  pointCode?: string;
  pointName?: string;
  latitude?: number;
  longitude?: number;
}

export interface PlanningPointResponse {
  pointId: number;
  pointCode: string;
  pointName: string;
  latitude?: number;
  longitude?: number;
  pointRole: 'PICKUP' | 'DROPOFF';
  studentCount: number;
}

export interface SchoolBusPlanningPreview {
  schoolId: number;
  schoolName: string;
  serviceDate: string;
  routeDirection: string;
  totalEligibleStudents: number;
  totalEligiblePickupPoints: number;
  eligibleStudents: SchoolBusEligibleStudent[];
  eligiblePickupPoints: SchoolBusPlanningPickupPoint[];

  schoolCode?: string;
  schoolAddress?: string;
  activeDays?: string[];
  serviceDayOfWeek?: string;
  direction?: string;
  summary?: PlanningReadinessSummary;
  eligibleDemands?: PlanningDemandResponse[];
  points?: PlanningPointResponse[];
  existingSessionId?: number | null;
  existingSessionStatus?: string | null;
  canCreate?: boolean;
  createDisabledReason?: string | null;
}

// -- Requests -------------------------------------------------------------

export interface PlanningSessionCreateRequest {
  schoolId: number;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
  planningNotes?: string;
}

export interface PlanningSessionPreviewRequest {
  schoolId: number;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
}

export interface CreateRouteInSessionRequest {
  schoolId: number;
  routeDirection: 'OUTBOUND' | 'RETURN';
  startLocationType: 'SCHOOL' | 'DEPOT';
  startSchoolId?: number;
  startDepotId?: number;
  endLocationType: 'SCHOOL' | 'DEPOT';
  endSchoolId?: number;
  endDepotId?: number;
  busId: number;
  routeName: string;
  serviceDate: string;
  planningNotes?: string;
}

export interface AddStudentToStopRequest {
  studentId: number;
  subscriptionId: number;
}

export interface GreedyFillRouteRequest {
  preserveExistingAssignments?: boolean;
  maxStops?: number | null;
}

export interface GreedyFillRouteResponse {
  routeId: number;
  sessionId: number;
  addedStudents: number;
  addedStops: number;
  totalAssignedStudents: number;
  remainingCapacity: number;
  plannedDistanceKm?: number | null;
  plannedDurationMin?: number | null;
  unassignedCandidates: number;
  skippedAssignedElsewhere?: number;
  skippedMissingCoordinates?: number;
  skippedInvalidPoint?: number;
  message: string;
}

export interface ChartItemDto {
  name: string;
  count: number;
  label?: string;
}

export interface DashboardOperationsResponse {
  summary: DashboardSummary;
  tripStatusChart: ChartItemDto[];
  attendanceChart: ChartItemDto[];
  routeReadinessChart: ChartItemDto[];
  requestStatusChart: ChartItemDto[];
  tripsByDate: ChartItemDto[];
  directionSplit: ChartItemDto[];
  activeRoutes: SchoolBusRoute[];
  pendingApprovalQueue: SchoolBusTransportRequest[];
  recentAttendanceActivity: SchoolBusAttendance[];
}

export interface SchoolBusDropdownOption {
  id: number;
  label: string;
  code?: string;
  description?: string;
  metadata?: Record<string, any>;
}

export interface SchoolBusNamedDropdownOption {
  id: number;
  name: string;
}

