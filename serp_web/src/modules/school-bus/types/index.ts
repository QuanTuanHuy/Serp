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
}

export interface SchoolBusBus extends SchoolBusBaseRecord {
  plateNumber: string;
  busType?: string | null;
  capacity: number;
  status: string;
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
  approvedBy?: number | null;
  approvedAt?: string | null;
  rejectionReason?: string | null;
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
  routeCode: string;
  routeName: string;
  serviceDate: string;
  shiftType: string;
  status: string;
  plannedDistanceKm?: number | null;
  plannedDurationMin?: number | null;
  planningNotes?: string | null;
  startedAt?: string | null;
  completedAt?: string | null;
}

export interface SchoolBusRouteStop extends SchoolBusBaseRecord {
  routeId: number;
  pickupPointId: number;
  pickupPointName: string;
  pickupPointAddress?: string | null;
  pickupPointLatitude?: number | null;
  pickupPointLongitude?: number | null;
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
  studentId: number;
  studentName: string;
  attendanceType: string;
  status: string;
  recordedAt: string;
  recordedBy: number;
  notes?: string | null;
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
  students: SchoolBusTransportRequestStudentInput[];
  isActive?: boolean;
}

export interface SchoolBusRejectRequest {
  reason: string;
  isActive?: boolean;
}

export interface SchoolBusRouteUpsertRequest {
  schoolId: number;
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
