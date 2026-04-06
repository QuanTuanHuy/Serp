export interface ApiResponse<T> {
  code: number;
  status: string;
  message: string;
  data: T;
}

export interface SchoolBusSchool {
  id: number;
  name: string;
  code?: string;
  address?: string;
  contactPhone?: string;
  contactEmail?: string;
  active?: boolean;
}

export interface SchoolBusParent {
  id: number;
  userId: number;
  fullName: string;
  phone?: string;
  email?: string;
  address?: string;
  active?: boolean;
}

export interface SchoolBusStudent {
  id: number;
  fullName: string;
  studentCode?: string;
  grade?: string;
  homeAddress?: string;
  active?: boolean;
  school?: SchoolBusSchool;
  parentProfile?: SchoolBusParent;
}

export interface SchoolBusBus {
  id: number;
  plateNumber: string;
  busType?: string;
  capacity: number;
  status: string;
}

export interface SchoolBusDriver {
  id: number;
  fullName: string;
  phone?: string;
  licenseNumber?: string;
  status: string;
  active?: boolean;
}

export interface SchoolBusAttendant {
  id: number;
  fullName: string;
  phone?: string;
  status: string;
  active?: boolean;
}

export interface SchoolBusPickupPoint {
  id: number;
  name: string;
  address: string;
}

export interface SchoolBusTransportRequest {
  id: number;
  requestType: string;
  status: string;
  effectiveFrom: string;
  effectiveTo?: string;
  notes?: string;
  school?: SchoolBusSchool;
  parentProfile?: SchoolBusParent;
}

export interface SchoolBusRoute {
  id: number;
  routeCode: string;
  routeName: string;
  serviceDate: string;
  shiftType: string;
  status: string;
  plannedDistanceKm?: number;
  plannedDurationMin?: number;
  school?: SchoolBusSchool;
}

export interface SchoolBusAttendance {
  id: number;
  attendanceType: string;
  status: string;
  recordedAt: string;
  notes?: string;
  student?: SchoolBusStudent;
}

export interface SchoolBusTripHistory {
  id: number;
  routeCode: string;
  serviceDate: string;
  status: string;
  startedAt?: string;
  completedAt?: string;
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
