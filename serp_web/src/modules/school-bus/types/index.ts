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

export interface SchoolBusSchool extends SchoolBusBaseRecord {
  name: string;
  code?: string | null;
  address?: string | null;
  contactPhone?: string | null;
  contactEmail?: string | null;
  latitude?: number | null;
  longitude?: number | null;
}

export interface SchoolBusParent extends SchoolBusBaseRecord {
  userId: number;
  fullName: string;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
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
  emergencyContactName?: string | null;
  emergencyContactPhone?: string | null;
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
  fullName: string;
  phone?: string | null;
  licenseNumber?: string | null;
  licenseClass?: string | null;
  licenseExpiryDate?: string | null;
  status: string;
}

export interface SchoolBusAttendant extends SchoolBusBaseRecord {
  userId: number;
  fullName: string;
  phone?: string | null;
  status: string;
}

export interface SchoolBusPickupPoint extends SchoolBusBaseRecord {
  name: string;
  address: string;
  latitude?: number | null;
  longitude?: number | null;
  code?: string | null;
  zoneCode?: string | null;
  usageType?: string | null;
  pickupInstruction?: string | null;
  /** @deprecated Legacy field, always null */
  schoolId?: number | null;
  /** @deprecated Legacy field, always null */
  schoolName?: string | null;
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

export interface SchoolBusRequestStudent extends SchoolBusBaseRecord {
  requestId: number;
  studentId: number;
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
  schoolScheduleId?: number | null;
  schoolScheduleName?: string | null;
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

export interface SchoolBusTransportRequestHistory extends SchoolBusBaseRecord {
  requestId: number;
  oldStatus?: string | null;
  newStatus: string;
  changedBy?: number | null;
  changedAt: string;
  reason?: string | null;
  notes?: string | null;
}

export interface SchoolBusTransportRequestDetail {
  request: SchoolBusTransportRequest;
  students: SchoolBusRequestStudent[];
}

export interface SchoolBusSubscriptionHistory extends SchoolBusBaseRecord {
  subscriptionId?: number | null;
  sourceRequestId?: number | null;
  requestStudentId?: number | null;
  changeType: string;
  oldStatus?: string | null;
  newStatus?: string | null;
  oldPickupPointId?: number | null;
  newPickupPointId?: number | null;
  oldDropoffPointId?: number | null;
  newDropoffPointId?: number | null;
  oldSchoolScheduleId?: number | null;
  newSchoolScheduleId?: number | null;
  oldTripOption?: string | null;
  newTripOption?: string | null;
  oldEffectiveFrom?: string | null;
  newEffectiveFrom?: string | null;
  oldEffectiveTo?: string | null;
  newEffectiveTo?: string | null;
  changedBy?: number | null;
  changedAt: string;
  reason?: string | null;
  notes?: string | null;
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
  schoolScheduleId: number;
  schoolScheduleName: string;
  status: string;
  plannedDistanceKm?: number | null;
  plannedDurationMin?: number | null;
  plannedStudentCount?: number | null;
  assignedBusCapacity?: number | null;
  routeGenerationMethod?: string | null;
  estimatedCost?: number | null;
  versionNo?: number | null;
  planningNotes?: string | null;
  geometryPath?: string | null;
  geometrySource?: 'ROAD_NETWORK' | 'STRAIGHT_LINE_ESTIMATE' | 'NONE' | null;
  fallbackUsed?: boolean | null;
  startedAt?: string | null;
  completedAt?: string | null;
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
  geometrySource?: 'ROAD_NETWORK' | 'STRAIGHT_LINE_ESTIMATE' | 'NONE' | null;
  distanceKm?: number | null;
  durationMin?: number | null;
  warning?: string | null;
  coordinates: SchoolBusRoutePathCoordinate[];
}

export interface SchoolBusRouteStop extends SchoolBusBaseRecord {
  routeId: number;
  pickupPointId: number;
  pickupPointName: string;
  pickupPointAddress?: string | null;
  pickupPointLatitude?: number | null;
  pickupPointLongitude?: number | null;
  stopType?: 'PICKUP' | 'DROPOFF';
  stopOrder: number;
  estimatedStudentCount?: number | null;
  plannedArrivalTime?: string | null;
  plannedDepartureTime?: string | null;
  distanceFromPreviousKm?: number | null;
  estimatedTravelTimeFromPrevious?: number | null;
}

export interface SchoolBusAssignmentHistory extends SchoolBusBaseRecord {
  routeId: number;
  oldBusId?: number | null;
  newBusId?: number | null;
  oldDriverId?: number | null;
  newDriverId?: number | null;
  oldAttendantId?: number | null;
  newAttendantId?: number | null;
  changedBy?: number | null;
  changedAt?: string | null;
  reason?: string | null;
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
  routeStopId?: number | null;
  studentId: number;
  studentName: string;
  subscriptionId: number;
  serviceAction: 'BOARD' | 'DROPOFF';
  stopName?: string | null;
  plannedTime?: string | null;
}

export interface SchoolBusRouteDetail {
  route: SchoolBusRoute;
  stops: SchoolBusRouteStop[];
  students: SchoolBusRoutePlanStudent[];
  assignment: SchoolBusRouteAssignment | null;
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
  schoolId: number;
  schoolName: string;
  pickupPointId?: number | null;
  pickupPointName?: string | null;
  dropoffPointId?: number | null;
  dropoffPointName?: string | null;
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
  schoolScheduleId?: number | null;
  schoolScheduleName?: string | null;
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
  shiftType: string;
  status: string;
  plannedStartAt?: string | null;
  plannedEndAt?: string | null;
  startedAt?: string | null;
  completedAt?: string | null;
  plannedDistanceKm?: number | null;
  plannedDurationMin?: number | null;
  actualDistanceKm?: number | null;
  actualDurationMin?: number | null;
  completionNote?: string | null;
  cancelledAt?: string | null;
  cancelledBy?: number | null;
  cancellationReason?: string | null;
  simulationMode?: boolean;
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

export interface SchoolBusDemoEvent extends SchoolBusBaseRecord {
  demoSessionId: number;
  eventType: string;
  eventTime: string;
  payloadJson?: string | null;
}

export interface SchoolBusDemoSession extends SchoolBusBaseRecord {
  demoCode: string;
  tripId: number;
  tripCode: string;
  status: string;
  speedMultiplier: number;
  currentStopOrder?: number | null;
  currentLatitude?: number | null;
  currentLongitude?: number | null;
  progressPercent: number;
  startedAt?: string | null;
  pausedAt?: string | null;
  completedAt?: string | null;
  events: SchoolBusDemoEvent[];
}

export interface SchoolBusCapacityUtilization {
  tripId: number;
  tripCode: string;
  routeCode: string;
  plannedStudents: number;
  busCapacity: number;
  utilizationPercent: number;
}

export interface SchoolBusAttendanceManifestStudent {
  studentId: number;
  studentName: string;
  pickupPointId?: number | null;
  pickupPointName?: string | null;
  latestAttendanceType?: string | null;
  latestAttendanceStatus?: string | null;
  latestRecordedAt?: string | null;
}

export interface SchoolBusAttendanceManifest {
  route: SchoolBusRoute;
  assignment: SchoolBusRouteAssignment | null;
  students: SchoolBusAttendanceManifestStudent[];
}

export interface SchoolBusTripHistory extends SchoolBusBaseRecord {
  routeId: number;
  routeCode: string;
  serviceDate: string;
  status: string;
  startedAt?: string | null;
  completedAt?: string | null;
  busId?: number | null;
  busPlateNumber?: string | null;
  driverId?: number | null;
  driverName?: string | null;
  attendantId?: number | null;
  attendantName?: string | null;
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
  userId: number;
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
  emergencyContactName?: string;
  emergencyContactPhone?: string;
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
  userId: number;
  fullName: string;
  phone?: string;
  licenseNumber: string;
  licenseClass: string;
  licenseExpiryDate: string;
  status: string;
  isActive?: boolean;
}

export interface SchoolBusAttendantUpsertRequest {
  userId: number;
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
  zoneCode?: string | null;
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
  schoolScheduleId?: number | null;
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
  schoolScheduleId?: number | null;
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
  schoolScheduleId: number;
  planningNotes?: string;
  isActive?: boolean;
}

export interface SchoolBusRouteAssignmentRequest {
  busId: number;
  driverId: number;
  attendantId?: number | null;
  assignmentNote?: string | null;
  reason?: string | null;
  isActive?: boolean;
}

export interface SchoolBusManualDispatchRequest extends SchoolBusRouteAssignmentRequest {
  orderedStopIds?: number[];
  notes?: string;
}

export interface SchoolBusTripAttendanceActionRequest {
  routeStopId: number;
  studentId: number;
  notes?: string;
  isActive?: boolean;
}

export interface SchoolBusDemoSpeedRequest {
  speedMultiplier: number;
  isActive?: boolean;
}

export interface SchoolBusAttendanceActionRequest {
  routeId: number;
  studentId: number;
  notes?: string;
  isActive?: boolean;
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

// --- School Schedule ---

export interface SchoolBusSchedule extends SchoolBusBaseRecord {
  schoolId: number;
  schoolName: string;
  scheduleCode?: string | null;
  scheduleName: string;
  educationLevel?: string | null;
  grade?: string | null;
  shiftType: string;
  daysOfWeek?: string[];
  arrivalDeadline?: string | null;
  departureTime?: string | null;
  effectiveFrom: string;
  effectiveTo?: string | null;
  isDefault?: boolean;
}

export interface SchoolBusScheduleUpsertRequest {
  scheduleName: string;
  educationLevel?: string | null;
  grade?: string | null;
  shiftType: string;
  daysOfWeek?: string[];
  arrivalDeadline?: string | null;
  departureTime?: string | null;
  effectiveFrom: string;
  effectiveTo?: string | null;
  isDefault?: boolean;
  isActive?: boolean;
}

// --- School ↔ Pickup Point link ---

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

// ===== School Pickup Point Windows =====
export type PickupPointWindowDirection = 'PICKUP_TO_SCHOOL' | 'DROPOFF_FROM_SCHOOL';

export interface SchoolBusSchoolPickupPointWindow extends SchoolBusBaseRecord {
  schoolPickupPointId: number;
  schoolScheduleId: number;
  scheduleName: string;
  direction: PickupPointWindowDirection;
  windowStart: string;
  windowEnd: string;
  estimatedDistanceToSchoolKm?: number | null;
  estimatedDurationToSchoolMin?: number | null;
}

export interface SchoolPickupPointWindowUpsertRequest {
  schoolPickupPointId: number;
  schoolScheduleId: number;
  direction: PickupPointWindowDirection;
  windowStart: string;
  windowEnd: string;
  estimatedDistanceToSchoolKm?: number | null;
  estimatedDurationToSchoolMin?: number | null;
}

// ── Planning Session ──────────────────────────────────────────────────────

export type PlanningSessionStatus = 'DRAFT' | 'GENERATED' | 'REVIEWING' | 'PUBLISHED' | 'CANCELLED';
export type PlanningMethod = 'MANUAL' | 'GREEDY';
export type PlanningIssueSeverity = 'INFO' | 'WARNING' | 'BLOCKING';

export interface SchoolBusPlanningSession extends SchoolBusBaseRecord {
  schoolId: number;
  schoolName: string;
  schoolScheduleId: number;
  schoolScheduleName: string;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
  planningMethod: PlanningMethod;
  status: PlanningSessionStatus;
  totalEligibleStudents: number;
  totalPlannedStudents: number;
  totalUnassignedStudents: number;
  totalRoutes: number;
  totalStops: number;
  totalDistanceKm?: number | null;
  totalDurationMin?: number | null;
  generatedAt?: string | null;
  publishedAt?: string | null;
  planningNotes?: string | null;
}

export interface SchoolBusPlanningIssue {
  id?: number;
  planningSessionId?: number;
  routeId?: number;
  routeStopId?: number;
  studentId?: number;
  studentName?: string;
  subscriptionId?: number;
  issueType: string;
  severity: PlanningIssueSeverity;
  message: string;
  isResolved?: boolean;
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
  windowStart?: string;
  windowEnd?: string;
  specialNote?: string;
}

export interface SchoolBusPlanningPickupPoint {
  pickupPointId: number;
  pickupPointName: string;
  latitude?: number;
  longitude?: number;
  studentCount: number;
  hasWindow?: boolean;
}

export interface SchoolBusPlanningPreview {
  schoolId: number;
  schoolName: string;
  schoolScheduleId: number;
  schoolScheduleName?: string;
  serviceDate: string;
  routeDirection: string;
  totalEligibleStudents: number;
  totalEligiblePickupPoints: number;
  eligibleStudents: SchoolBusEligibleStudent[];
  eligiblePickupPoints: SchoolBusPlanningPickupPoint[];
  issues: SchoolBusPlanningIssue[];
}

export interface SchoolBusRouteQuality {
  routeId: number;
  routeCode: string;
  routeName: string;
  status: string;
  studentCount: number;
  stopCount: number;
  totalDistanceKm?: number;
  totalDurationMin?: number;
  requiredCapacity?: number;
  capacityUtilizationPercent?: number;
  qualityScore?: number;
  blockingIssueCount: number;
  warningIssueCount: number;
  infoIssueCount: number;
  arrivalDeadlineStatus?: string;
  departureTimeStatus?: string;
  issues: SchoolBusPlanningIssue[];
}

export interface SchoolBusGreedyGenerateResult {
  session: SchoolBusPlanningSession;
  routes: SchoolBusRouteQuality[];
  totalUnassignedStudents: number;
  unassignedStudents: SchoolBusEligibleStudent[];
  sessionIssues: SchoolBusPlanningIssue[];
}

// ── Requests ─────────────────────────────────────────────────────────────

export interface PlanningSessionCreateRequest {
  schoolId: number;
  schoolScheduleId: number;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
  planningMethod: PlanningMethod;
  planningNotes?: string;
}

export interface PlanningSessionPreviewRequest {
  schoolId: number;
  schoolScheduleId: number;
  serviceDate: string;
  routeDirection: 'OUTBOUND' | 'RETURN';
}

export interface GreedyGenerateRequest {
  defaultBusCapacity?: number;
  depotId?: number;
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
  routeName: string;
  serviceDate: string;
  schoolScheduleId: number;
  planningNotes?: string;
}

export interface AddStudentToStopRequest {
  studentId: number;
  subscriptionId: number;
}
