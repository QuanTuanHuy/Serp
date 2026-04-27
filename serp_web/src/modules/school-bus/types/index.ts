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
  pickupPointId?: number | null;
  pickupPointName?: string | null;
  fullName: string;
  studentCode?: string | null;
  grade?: string | null;
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
  status: string;
}

export interface SchoolBusAttendant extends SchoolBusBaseRecord {
  userId: number;
  fullName: string;
  phone?: string | null;
  status: string;
}

export interface SchoolBusPickupPoint extends SchoolBusBaseRecord {
  schoolId: number;
  schoolName: string;
  name: string;
  address: string;
  latitude?: number | null;
  longitude?: number | null;
  pickupWindowStart?: string | null;
  pickupWindowEnd?: string | null;
}

export interface SchoolBusDepot extends SchoolBusBaseRecord {
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
  shiftType: string;
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
}

export interface SchoolBusRouteAssignment extends SchoolBusBaseRecord {
  routeId: number;
  busId: number;
  busPlateNumber: string;
  driverId: number;
  driverName: string;
  attendantId?: number | null;
  attendantName?: string | null;
  assignedAt?: string | null;
}

export interface SchoolBusRouteDetail {
  route: SchoolBusRoute;
  stops: SchoolBusRouteStop[];
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
  simulationMode?: boolean;
  busId?: number | null;
  busPlateNumber?: string | null;
  driverId?: number | null;
  driverName?: string | null;
  attendantId?: number | null;
  attendantName?: string | null;
  stops: SchoolBusTripStopLog[];
  students: SchoolBusTripStudent[];
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
  pickupPointId?: number | null;
  fullName: string;
  grade?: string;
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
  isActive?: boolean;
}

export interface SchoolBusDriverUpsertRequest {
  userId: number;
  fullName: string;
  phone?: string;
  licenseNumber?: string;
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
  schoolId: number;
  name: string;
  address: string;
  latitude?: number | null;
  longitude?: number | null;
  pickupWindowStart?: string | null;
  pickupWindowEnd?: string | null;
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
  shiftType: string;
  planningNotes?: string;
  isActive?: boolean;
}

export interface SchoolBusRouteAssignmentRequest {
  busId: number;
  driverId: number;
  attendantId?: number | null;
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
