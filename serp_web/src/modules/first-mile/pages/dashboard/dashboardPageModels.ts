/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard page models
 */

import type { LucideIcon } from 'lucide-react';
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  CircleDollarSign,
  Clock3,
  PackageCheck,
  Route,
  Truck,
} from 'lucide-react';

import type {
  Hub,
  PostOffice,
  TmsDashboardAlertSeverity,
  TmsDashboardBreakdownItem,
  TmsDashboardFilter,
  TmsDashboardGranularity,
  TmsDashboardLeg,
  TmsDashboardServiceType,
} from '@/modules/first-mile/types';

export type DashboardDatePreset =
  | 'TODAY'
  | 'LAST_7_DAYS'
  | 'LAST_30_DAYS'
  | 'CURRENT_MONTH'
  | 'CUSTOM';

export type DashboardSelectValue = 'ALL' | string;

export interface DashboardKpiConfig {
  key: string;
  title: string;
  value: string;
  detail: string;
  icon: LucideIcon;
  tone: 'blue' | 'green' | 'amber' | 'rose';
  href?: string;
  trend?: number;
}

export interface DashboardLegSummary {
  key: Exclude<TmsDashboardLeg, 'ALL'>;
  title: string;
  description: string;
  icon: LucideIcon;
}

export interface DashboardOption {
  value: string;
  label: string;
}

export const DASHBOARD_TIMEZONE = 'Asia/Saigon';

export const DATE_PRESET_OPTIONS: DashboardOption[] = [
  { value: 'TODAY', label: 'Hôm nay' },
  { value: 'LAST_7_DAYS', label: '7 ngày gần đây' },
  { value: 'LAST_30_DAYS', label: '30 ngày gần đây' },
  { value: 'CURRENT_MONTH', label: 'Tháng hiện tại' },
  { value: 'CUSTOM', label: 'Tùy chỉnh' },
];

export const GRANULARITY_OPTIONS: DashboardOption[] = [
  { value: 'DAY', label: 'Theo ngày' },
  { value: 'WEEK', label: 'Theo tuần' },
  { value: 'MONTH', label: 'Theo tháng' },
];

export const SERVICE_TYPE_OPTIONS: DashboardOption[] = [
  { value: 'ALL', label: 'Tất cả dịch vụ' },
  { value: 'STANDARD_ORDER', label: 'Đơn tiêu chuẩn' },
];

export const LEG_SUMMARIES: DashboardLegSummary[] = [
  {
    key: 'FIRST_MILE',
    title: 'Chặng lấy hàng',
    description: 'Lấy hàng, nhập bưu cục, lập túi và sẵn sàng bàn giao.',
    icon: Truck,
  },
  {
    key: 'MIDDLE_MILE',
    title: 'Chặng trung chuyển',
    description: 'Xử lý tại hub, luân chuyển túi và thực thi tuyến.',
    icon: Route,
  },
  {
    key: 'LAST_MILE',
    title: 'Chặng giao hàng',
    description: 'Nhập điểm đến, giao phát và kết quả giao hàng.',
    icon: PackageCheck,
  },
];

export const CHART_COLORS = [
  'var(--chart-1)',
  'var(--chart-2)',
  'var(--chart-3)',
  'var(--chart-4)',
  'var(--chart-5)',
];

export const KPI_ICONS = {
  orders: Activity,
  status: Clock3,
  success: CheckCircle2,
  finance: CircleDollarSign,
  alert: AlertTriangle,
};

export const buildPresetRange = (
  preset: DashboardDatePreset,
  now = new Date()
): Pick<TmsDashboardFilter, 'fromDate' | 'toDate'> => {
  const today = startOfDate(now);

  if (preset === 'TODAY') {
    return {
      fromDate: toDateInputValue(today),
      toDate: toDateInputValue(today),
    };
  }

  if (preset === 'LAST_30_DAYS') {
    return {
      fromDate: toDateInputValue(addDays(today, -29)),
      toDate: toDateInputValue(today),
    };
  }

  if (preset === 'CURRENT_MONTH') {
    return {
      fromDate: toDateInputValue(
        new Date(today.getFullYear(), today.getMonth(), 1)
      ),
      toDate: toDateInputValue(today),
    };
  }

  return {
    fromDate: toDateInputValue(addDays(today, -6)),
    toDate: toDateInputValue(today),
  };
};

export const buildDashboardFilter = ({
  fromDate,
  toDate,
  granularity,
  selectedHubId,
  selectedPostOfficeCode,
  selectedServiceType,
  postOfficesInScope,
}: {
  fromDate: string;
  toDate: string;
  granularity: TmsDashboardGranularity;
  selectedHubId: DashboardSelectValue;
  selectedPostOfficeCode: DashboardSelectValue;
  selectedServiceType: DashboardSelectValue;
  postOfficesInScope: PostOffice[];
}): TmsDashboardFilter => {
  const selectedPostOffice =
    selectedPostOfficeCode !== 'ALL' ? selectedPostOfficeCode : undefined;
  const postOfficeCodes =
    !selectedPostOffice && selectedHubId !== 'ALL'
      ? postOfficesInScope.map((item) => item.code).filter(Boolean)
      : undefined;
  const scopedPostOfficeCodes =
    selectedHubId !== 'ALL' && !selectedPostOffice && !postOfficeCodes?.length
      ? ['__EMPTY_HUB_SCOPE__']
      : postOfficeCodes;

  return {
    fromDate,
    toDate,
    timezone: DASHBOARD_TIMEZONE,
    granularity,
    ...(selectedHubId !== 'ALL' ? { hubId: Number(selectedHubId) } : {}),
    ...(selectedPostOffice ? { postOfficeCode: selectedPostOffice } : {}),
    ...(scopedPostOfficeCodes?.length
      ? { postOfficeCodes: scopedPostOfficeCodes }
      : {}),
    ...(selectedServiceType !== 'ALL'
      ? { serviceType: selectedServiceType as TmsDashboardServiceType }
      : {}),
  };
};

export const buildHubOptions = (hubs: Hub[]): DashboardOption[] => [
  { value: 'ALL', label: 'Tất cả hub' },
  ...hubs.map((hub) => ({
    value: String(hub.id),
    label: `${hub.code} - ${hub.name}`,
  })),
];

export const buildPostOfficeOptions = (
  postOffices: PostOffice[]
): DashboardOption[] => [
  { value: 'ALL', label: 'Tất cả bưu cục' },
  ...postOffices.map((postOffice) => ({
    value: postOffice.code,
    label: `${postOffice.code} - ${postOffice.name}`,
  })),
];

export const formatNumber = (value?: number | null): string =>
  new Intl.NumberFormat('vi-VN', { maximumFractionDigits: 0 }).format(
    value ?? 0
  );

export const formatPercent = (value?: number | null): string =>
  `${new Intl.NumberFormat('vi-VN', {
    maximumFractionDigits: 1,
  }).format(value ?? 0)}%`;

export const formatCurrency = (
  value?: number | null,
  currency = 'VND'
): string =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency,
    maximumFractionDigits: currency === 'VND' ? 0 : 2,
  }).format(value ?? 0);

export const formatMinutes = (value?: number | null): string => {
  if (value === null || value === undefined) {
    return 'Chưa có dữ liệu';
  }
  if (value < 60) {
    return `${formatNumber(value)} phút`;
  }
  return `${formatNumber(Math.floor(value / 60))} giờ ${formatNumber(
    value % 60
  )} phút`;
};

export const formatDateTime = (value?: string | null): string => {
  if (!value) {
    return 'Chưa có dữ liệu';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
};

export const isDashboardEmpty = (
  overviewTotal?: number,
  firstMileTotal?: number,
  middleMileTotal?: number,
  lastMileTotal?: number
): boolean =>
  (overviewTotal ?? 0) === 0 &&
  (firstMileTotal ?? 0) === 0 &&
  (middleMileTotal ?? 0) === 0 &&
  (lastMileTotal ?? 0) === 0;

export const getSeverityBadgeVariant = (
  severity: TmsDashboardAlertSeverity
): 'default' | 'secondary' | 'destructive' | 'outline' => {
  if (severity === 'HIGH' || severity === 'CRITICAL') {
    return 'destructive';
  }
  if (severity === 'MEDIUM') {
    return 'default';
  }
  return 'secondary';
};

export const toChartData = (items: TmsDashboardBreakdownItem[]) =>
  items.map((item) => ({
    name: item.name,
    value: item.count,
    percentage: item.percentage,
  }));

export const toDateInputValue = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const startOfDate = (date: Date): Date =>
  new Date(date.getFullYear(), date.getMonth(), date.getDate());

const addDays = (date: Date, days: number): Date => {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
};
