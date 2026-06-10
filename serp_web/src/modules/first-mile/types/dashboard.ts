/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard types
 */

export type TmsDashboardGranularity = 'DAY' | 'WEEK' | 'MONTH';

export type TmsDashboardServiceType = 'STANDARD_ORDER';

export type TmsDashboardLeg =
  | 'ALL'
  | 'FIRST_MILE'
  | 'MIDDLE_MILE'
  | 'LAST_MILE';

export type TmsDashboardAccessLevel =
  | 'ADMIN'
  | 'HUB_MANAGER'
  | 'POST_OFFICE_MANAGER'
  | 'VIEWER';

export interface TmsDashboardFilter {
  fromDate: string;
  toDate: string;
  timezone: string;
  granularity: TmsDashboardGranularity;
  hubId?: number;
  postOfficeId?: number;
  postOfficeCode?: string;
  postOfficeCodes?: string[];
  serviceType?: TmsDashboardServiceType;
}

export interface TmsDashboardScope {
  accessLevel: TmsDashboardAccessLevel;
  hubIds: number[];
  postOfficeCodes: string[];
  roleCodes: string[];
}

export interface TmsDashboardPeriod {
  fromDate: string;
  toDate: string;
  timezone: string;
  granularity: TmsDashboardGranularity;
}

export interface TmsDashboardTrendPoint {
  label: string;
  date: string;
  value: number;
}

export interface TmsDashboardBreakdownItem {
  code: string;
  name: string;
  count: number;
  percentage: number;
}

export interface TmsDashboardTopEntity {
  code: string;
  name: string;
  count: number;
  percentage: number;
}

export interface TmsDashboardOrderVolume {
  totalOrders: number;
  newOrders: number;
  inProgressOrders: number;
  completedOrders: number;
  cancelledOrders: number;
  returnedOrders: number;
  growthRatePercent: number;
}

export interface TmsDashboardStatus {
  statusCode: string;
  statusName: string;
  count: number;
  percentage: number;
  lastUpdatedAt?: string;
}

export interface TmsDashboardDeliverySuccess {
  deliveredOrders: number;
  failedDeliveryOrders: number;
  returnedOrders: number;
  successRatePercent: number;
  failedReasons: TmsDashboardBreakdownItem[];
}

export interface TmsDashboardFinance {
  grossRevenue: number;
  netRevenue: number;
  codAmount: number;
  codCollected: number;
  codReconciled: number;
  codPending: number;
  currency: string;
  estimated: boolean;
  source: string;
}

export interface TmsDashboardOverview {
  scope: TmsDashboardScope;
  period: TmsDashboardPeriod;
  orderVolume: TmsDashboardOrderVolume;
  orderStatuses: TmsDashboardStatus[];
  deliverySuccess: TmsDashboardDeliverySuccess;
  finance: TmsDashboardFinance;
  lastUpdatedAt: string;
}

export interface TmsDashboardFirstMile {
  totalOrders: number;
  pickupSuccessRatePercent: number;
  avgPickupMinutes?: number | null;
  slaBreachedOrders: number;
  statusBreakdown: TmsDashboardBreakdownItem[];
  trend: TmsDashboardTrendPoint[];
  topPostOffices: TmsDashboardTopEntity[];
}

export interface TmsDashboardMiddleMile {
  totalOrders: number;
  totalBags: number;
  totalRoutes: number;
  onTimeRouteRatePercent: number;
  avgHubDwellMinutes?: number | null;
  statusBreakdown: TmsDashboardBreakdownItem[];
  topHubs: TmsDashboardTopEntity[];
}

export interface TmsDashboardLastMile {
  totalOrders: number;
  deliverySuccessRatePercent: number;
  avgDeliveryMinutes?: number | null;
  failedReasons: TmsDashboardBreakdownItem[];
  slaBreachedOrders: number;
  trend: TmsDashboardTrendPoint[];
  statusBreakdown: TmsDashboardBreakdownItem[];
}

export interface TmsDashboardLegs {
  scope: TmsDashboardScope;
  period: TmsDashboardPeriod;
  firstMile: TmsDashboardFirstMile;
  middleMile: TmsDashboardMiddleMile;
  lastMile: TmsDashboardLastMile;
  lastUpdatedAt: string;
}

export type TmsDashboardAlertSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface TmsDashboardAlert {
  id: string;
  type: string;
  severity: TmsDashboardAlertSeverity;
  title: string;
  description: string;
  entityType: string;
  entityId?: number | null;
  entityCode?: string | null;
  statusCode?: string | null;
  leg: TmsDashboardLeg;
  occurredAt?: string | null;
  dueAt?: string | null;
  actionHref?: string | null;
}

export interface TmsDashboardAlerts {
  scope: TmsDashboardScope;
  period: TmsDashboardPeriod;
  items: TmsDashboardAlert[];
  lastUpdatedAt: string;
}
