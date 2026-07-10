/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard page
 */

'use client';

import React from 'react';
import { Activity, CheckCircle2, CircleDollarSign, Clock3 } from 'lucide-react';

import {
  Badge,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Separator,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';

import type {
  TmsDashboardAlerts,
  TmsDashboardFilter,
  TmsDashboardGranularity,
  TmsDashboardLeg,
  TmsDashboardLegs,
  TmsDashboardOverview,
} from '@/modules/first-mile/types';

import {
  DashboardAlertsTable,
  DashboardFilters,
  DashboardKpiCard,
  DashboardLegPanel,
  BreakdownChart,
} from './components';
import type {
  DashboardDatePreset,
  DashboardKpiConfig,
  DashboardOption,
  DashboardSelectValue,
} from './dashboardPageModels';
import {
  buildPresetRange,
  formatCurrency,
  formatDateTime,
  formatNumber,
  formatPercent,
  LEG_SUMMARIES,
} from './dashboardPageModels';

const MOCK_HUB_OPTIONS: DashboardOption[] = [
  { value: 'ALL', label: 'Tất cả hub' },
  { value: '1', label: 'HCM-HUB - Hub TP. Hồ Chí Minh' },
  { value: '2', label: 'HAN-HUB - Hub Hà Nội' },
  { value: '3', label: 'DNG-HUB - Hub Đà Nẵng' },
];

const MOCK_POST_OFFICE_OPTIONS: DashboardOption[] = [
  { value: 'ALL', label: 'Tất cả bưu cục' },
  { value: 'BC-Q1', label: 'BC-Q1 - Bưu cục Quận 1' },
  { value: 'BC-TD', label: 'BC-TD - Bưu cục Thủ Đức' },
  { value: 'BC-CG', label: 'BC-CG - Bưu cục Cầu Giấy' },
  { value: 'BC-HC', label: 'BC-HC - Bưu cục Hải Châu' },
];

const MOCK_OVERVIEW: TmsDashboardOverview = {
  scope: {
    accessLevel: 'ADMIN',
    hubIds: [1, 2, 3],
    postOfficeCodes: ['BC-Q1', 'BC-TD', 'BC-CG', 'BC-HC'],
    roleCodes: ['TMS_ADMIN'],
  },
  period: {
    fromDate: '2026-07-03',
    toDate: '2026-07-09',
    timezone: 'Asia/Saigon',
    granularity: 'DAY',
  },
  orderVolume: {
    totalOrders: 12840,
    newOrders: 2140,
    inProgressOrders: 3860,
    completedOrders: 8340,
    cancelledOrders: 168,
    returnedOrders: 472,
    growthRatePercent: 12.6,
  },
  orderStatuses: [
    {
      statusCode: 'CREATED',
      statusName: 'Mới tạo',
      count: 1260,
      percentage: 9.8,
    },
    {
      statusCode: 'PICKED_UP',
      statusName: 'Đã lấy hàng',
      count: 2480,
      percentage: 19.3,
    },
    {
      statusCode: 'IN_TRANSIT',
      statusName: 'Đang trung chuyển',
      count: 3160,
      percentage: 24.6,
    },
    {
      statusCode: 'OUT_FOR_DELIVERY',
      statusName: 'Đang giao',
      count: 1600,
      percentage: 12.5,
    },
    {
      statusCode: 'DELIVERED',
      statusName: 'Giao thành công',
      count: 4340,
      percentage: 33.8,
    },
  ],
  deliverySuccess: {
    deliveredOrders: 8340,
    failedDeliveryOrders: 620,
    returnedOrders: 472,
    successRatePercent: 93.1,
    failedReasons: [
      {
        code: 'CUSTOMER_ABSENT',
        name: 'Khách vắng mặt',
        count: 228,
        percentage: 36.8,
      },
      {
        code: 'WRONG_ADDRESS',
        name: 'Sai địa chỉ',
        count: 154,
        percentage: 24.8,
      },
      {
        code: 'CUSTOMER_REJECTED',
        name: 'Khách từ chối nhận',
        count: 132,
        percentage: 21.3,
      },
      { code: 'OTHER', name: 'Lý do khác', count: 106, percentage: 17.1 },
    ],
  },
  finance: {
    grossRevenue: 1845000000,
    netRevenue: 1712000000,
    codAmount: 6420000000,
    codCollected: 5160000000,
    codReconciled: 4380000000,
    codPending: 1260000000,
    currency: 'VND',
    estimated: true,
    source: 'MOCK',
  },
  lastUpdatedAt: '2026-07-09T14:20:00+07:00',
};

const MOCK_LEGS: TmsDashboardLegs = {
  scope: MOCK_OVERVIEW.scope,
  period: MOCK_OVERVIEW.period,
  firstMile: {
    totalOrders: 9580,
    pickupSuccessRatePercent: 96.4,
    avgPickupMinutes: 42,
    slaBreachedOrders: 84,
    statusBreakdown: [
      {
        code: 'WAITING_PICKUP',
        name: 'Chờ lấy hàng',
        count: 760,
        percentage: 7.9,
      },
      { code: 'PICKED_UP', name: 'Đã lấy hàng', count: 3360, percentage: 35.1 },
      { code: 'BAGGED', name: 'Đã lập túi', count: 4020, percentage: 42.0 },
      {
        code: 'HANDED_OVER',
        name: 'Đã bàn giao',
        count: 1440,
        percentage: 15.0,
      },
    ],
    trend: [
      { label: '03/07', date: '2026-07-03', value: 1120 },
      { label: '04/07', date: '2026-07-04', value: 1260 },
      { label: '05/07', date: '2026-07-05', value: 1310 },
      { label: '06/07', date: '2026-07-06', value: 1180 },
      { label: '07/07', date: '2026-07-07', value: 1420 },
      { label: '08/07', date: '2026-07-08', value: 1510 },
      { label: '09/07', date: '2026-07-09', value: 1780 },
    ],
    topPostOffices: [
      { code: 'BC-Q1', name: 'Bưu cục Quận 1', count: 1860, percentage: 100 },
      { code: 'BC-TD', name: 'Bưu cục Thủ Đức', count: 1540, percentage: 82.8 },
      {
        code: 'BC-CG',
        name: 'Bưu cục Cầu Giấy',
        count: 1320,
        percentage: 71.0,
      },
      {
        code: 'BC-HC',
        name: 'Bưu cục Hải Châu',
        count: 1040,
        percentage: 55.9,
      },
    ],
  },
  middleMile: {
    totalOrders: 7040,
    totalBags: 438,
    totalRoutes: 76,
    onTimeRouteRatePercent: 91.7,
    avgHubDwellMinutes: 126,
    statusBreakdown: [
      {
        code: 'READY',
        name: 'Sẵn sàng điều phối',
        count: 86,
        percentage: 19.6,
      },
      {
        code: 'IN_TRANSIT',
        name: 'Đang vận chuyển',
        count: 214,
        percentage: 48.9,
      },
      {
        code: 'ARRIVED',
        name: 'Đã đến hub đích',
        count: 108,
        percentage: 24.7,
      },
      { code: 'DELAYED', name: 'Chậm xử lý', count: 30, percentage: 6.8 },
    ],
    topHubs: [
      {
        code: 'HCM-HUB',
        name: 'Hub TP. Hồ Chí Minh',
        count: 238,
        percentage: 100,
      },
      { code: 'HAN-HUB', name: 'Hub Hà Nội', count: 176, percentage: 73.9 },
      { code: 'DNG-HUB', name: 'Hub Đà Nẵng', count: 92, percentage: 38.7 },
    ],
  },
  lastMile: {
    totalOrders: 8920,
    deliverySuccessRatePercent: 93.1,
    avgDeliveryMinutes: 86,
    failedReasons: MOCK_OVERVIEW.deliverySuccess.failedReasons,
    slaBreachedOrders: 146,
    trend: [
      { label: '03/07', date: '2026-07-03', value: 980 },
      { label: '04/07', date: '2026-07-04', value: 1030 },
      { label: '05/07', date: '2026-07-05', value: 1190 },
      { label: '06/07', date: '2026-07-06', value: 1080 },
      { label: '07/07', date: '2026-07-07', value: 1320 },
      { label: '08/07', date: '2026-07-08', value: 1510 },
      { label: '09/07', date: '2026-07-09', value: 1810 },
    ],
    statusBreakdown: [
      {
        code: 'OUT_FOR_DELIVERY',
        name: 'Đang giao',
        count: 1600,
        percentage: 17.9,
      },
      {
        code: 'DELIVERED',
        name: 'Giao thành công',
        count: 8340,
        percentage: 93.1,
      },
      {
        code: 'FAILED',
        name: 'Giao không thành công',
        count: 620,
        percentage: 6.9,
      },
      { code: 'RETURNED', name: 'Hoàn hàng', count: 472, percentage: 5.3 },
    ],
  },
  lastUpdatedAt: MOCK_OVERVIEW.lastUpdatedAt,
};

const MOCK_ALERTS: TmsDashboardAlerts = {
  scope: MOCK_OVERVIEW.scope,
  period: MOCK_OVERVIEW.period,
  lastUpdatedAt: MOCK_OVERVIEW.lastUpdatedAt,
  items: [
    {
      id: 'mock-alert-1',
      type: 'SLA_RISK',
      severity: 'HIGH',
      title: 'Tuyến HCM-HUB -> HAN-HUB có nguy cơ trễ',
      description: '18 túi đang chờ xuất bến quá 90 phút so với kế hoạch.',
      entityType: 'ROUTE',
      entityCode: 'RT-HCM-HAN-02',
      leg: 'MIDDLE_MILE',
      occurredAt: '2026-07-09T13:45:00+07:00',
      actionHref: '/first-mile/dispatchers/second-mile',
    },
    {
      id: 'mock-alert-2',
      type: 'COD_PENDING',
      severity: 'MEDIUM',
      title: 'COD chờ đối soát tăng tại bưu cục Thủ Đức',
      description: 'COD tồn chờ đối soát đạt 246 triệu đồng trong ca chiều.',
      entityType: 'POST_OFFICE',
      entityCode: 'BC-TD',
      leg: 'LAST_MILE',
      occurredAt: '2026-07-09T12:30:00+07:00',
      actionHref: '/first-mile/settings/billing',
    },
    {
      id: 'mock-alert-3',
      type: 'PICKUP_CAPACITY',
      severity: 'LOW',
      title: 'Năng lực lấy hàng tại Quận 1 gần đầy',
      description:
        '3 nhân viên giao nhận đã dùng hơn 85% tải trong khung 15:00.',
      entityType: 'POST_OFFICE',
      entityCode: 'BC-Q1',
      leg: 'FIRST_MILE',
      occurredAt: '2026-07-09T11:10:00+07:00',
      actionHref: '/first-mile/dispatchers/first-mile',
    },
  ],
};

export const DashboardPage: React.FC = () => {
  const [datePreset, setDatePreset] =
    React.useState<DashboardDatePreset>('LAST_7_DAYS');
  const initialRange = React.useMemo(() => buildPresetRange('LAST_7_DAYS'), []);
  const [fromDate, setFromDate] = React.useState(initialRange.fromDate);
  const [toDate, setToDate] = React.useState(initialRange.toDate);
  const [granularity, setGranularity] =
    React.useState<TmsDashboardGranularity>('DAY');
  const [selectedHubId, setSelectedHubId] =
    React.useState<DashboardSelectValue>('ALL');
  const [selectedPostOfficeCode, setSelectedPostOfficeCode] =
    React.useState<DashboardSelectValue>('ALL');
  const [selectedServiceType, setSelectedServiceType] =
    React.useState<DashboardSelectValue>('ALL');
  const [selectedLeg, setSelectedLeg] =
    React.useState<Exclude<TmsDashboardLeg, 'ALL'>>('FIRST_MILE');
  const [lastUpdatedAt, setLastUpdatedAt] = React.useState(
    MOCK_OVERVIEW.lastUpdatedAt
  );

  React.useEffect(() => {
    setSelectedPostOfficeCode('ALL');
  }, [selectedHubId]);

  const filter = React.useMemo<TmsDashboardFilter>(
    () => ({
      fromDate,
      toDate,
      timezone: 'Asia/Saigon',
      granularity,
      ...(selectedHubId !== 'ALL' ? { hubId: Number(selectedHubId) } : {}),
      ...(selectedPostOfficeCode !== 'ALL'
        ? { postOfficeCode: selectedPostOfficeCode }
        : {}),
      ...(selectedServiceType !== 'ALL'
        ? { serviceType: 'STANDARD_ORDER' }
        : {}),
    }),
    [
      fromDate,
      granularity,
      selectedHubId,
      selectedPostOfficeCode,
      selectedServiceType,
      toDate,
    ]
  );

  const overview = React.useMemo<TmsDashboardOverview>(
    () => ({
      ...MOCK_OVERVIEW,
      period: {
        ...MOCK_OVERVIEW.period,
        fromDate,
        toDate,
        granularity,
      },
      lastUpdatedAt,
    }),
    [fromDate, granularity, lastUpdatedAt, toDate]
  );

  const legs = React.useMemo<TmsDashboardLegs>(
    () => ({
      ...MOCK_LEGS,
      period: overview.period,
      lastUpdatedAt,
    }),
    [lastUpdatedAt, overview.period]
  );

  const alerts = React.useMemo<TmsDashboardAlerts>(
    () => ({
      ...MOCK_ALERTS,
      period: overview.period,
      lastUpdatedAt,
    }),
    [lastUpdatedAt, overview.period]
  );

  const kpis = React.useMemo(
    () => buildKpis(overview, filter),
    [filter, overview]
  );
  const activeScopeLabel = resolveScopeLabel(
    selectedHubId,
    selectedPostOfficeCode
  );

  const handlePresetChange = (value: DashboardDatePreset) => {
    setDatePreset(value);
    if (value !== 'CUSTOM') {
      const nextRange = buildPresetRange(value);
      setFromDate(nextRange.fromDate);
      setToDate(nextRange.toDate);
    }
  };

  const handleRefresh = () => {
    setLastUpdatedAt(new Date().toISOString());
  };

  return (
    <div className='space-y-5 p-4 md:p-6'>
      <div className='flex flex-col gap-3 xl:flex-row xl:items-start xl:justify-between'>
        <div className='space-y-2'>
          <div className='flex flex-wrap items-center gap-2'>
            <h1 className='text-2xl font-semibold tracking-normal'>
              Tổng quan vận hành TMS
            </h1>
            <Badge variant='outline'>{activeScopeLabel}</Badge>
            <Badge variant='secondary'>Dữ liệu mô phỏng</Badge>
          </div>
          <div className='text-sm text-muted-foreground'>
            Cập nhật lần cuối {formatDateTime(lastUpdatedAt)}
          </div>
        </div>

        <div className='w-full xl:max-w-6xl'>
          <DashboardFilters
            datePreset={datePreset}
            fromDate={fromDate}
            toDate={toDate}
            granularity={granularity}
            selectedHubId={selectedHubId}
            selectedPostOfficeCode={selectedPostOfficeCode}
            selectedServiceType={selectedServiceType}
            hubOptions={MOCK_HUB_OPTIONS}
            postOfficeOptions={MOCK_POST_OFFICE_OPTIONS}
            canUseHubFilter
            canUsePostOfficeFilter
            isRefreshing={false}
            onDatePresetChange={handlePresetChange}
            onFromDateChange={(value) => {
              setDatePreset('CUSTOM');
              setFromDate(value);
            }}
            onToDateChange={(value) => {
              setDatePreset('CUSTOM');
              setToDate(value);
            }}
            onGranularityChange={setGranularity}
            onHubChange={setSelectedHubId}
            onPostOfficeChange={setSelectedPostOfficeCode}
            onServiceTypeChange={setSelectedServiceType}
            onRefresh={handleRefresh}
          />
        </div>
      </div>

      <KpiGrid kpis={kpis} />

      <div className='grid gap-4 xl:grid-cols-[1.15fr_0.85fr]'>
        <BreakdownChart
          title='Trạng thái đơn hàng'
          items={overview.orderStatuses.map((item) => ({
            code: item.statusCode,
            name: item.statusName,
            count: item.count,
            percentage: item.percentage,
          }))}
          emptyText='Chưa có dữ liệu trạng thái đơn hàng'
        />

        <Card className='rounded-lg'>
          <CardHeader className='pb-2'>
            <CardTitle className='text-base'>Giao hàng và COD</CardTitle>
            <CardDescription>
              Doanh thu, tiền thu hộ đã thu và khoản COD còn chờ đối soát.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <MetricRow
              label='Đã giao thành công'
              value={formatNumber(overview.deliverySuccess.deliveredOrders)}
            />
            <MetricRow
              label='Giao lỗi hoặc hoàn'
              value={formatNumber(
                overview.deliverySuccess.failedDeliveryOrders +
                  overview.deliverySuccess.returnedOrders
              )}
            />
            <MetricRow
              label='COD đã thu'
              value={formatCurrency(
                overview.finance.codCollected,
                overview.finance.currency
              )}
            />
            <MetricRow
              label='COD chờ đối soát'
              value={formatCurrency(
                overview.finance.codPending,
                overview.finance.currency
              )}
            />
            <Separator />
            <div className='flex items-center justify-between gap-3'>
              <span className='text-sm text-muted-foreground'>
                Tỷ lệ giao thành công
              </span>
              <span className='text-lg font-semibold'>
                {formatPercent(overview.deliverySuccess.successRatePercent)}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card className='rounded-lg'>
        <CardHeader className='pb-2'>
          <CardTitle className='text-base'>Hiệu suất theo ba chặng</CardTitle>
          <CardDescription>
            Số liệu mô phỏng cho lấy hàng, trung chuyển và giao hàng.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs
            value={selectedLeg}
            onValueChange={(value) =>
              setSelectedLeg(value as Exclude<TmsDashboardLeg, 'ALL'>)
            }
          >
            <TabsList className='mb-4 grid h-auto w-full grid-cols-1 md:grid-cols-3'>
              {LEG_SUMMARIES.map((item) => {
                const Icon = item.icon;
                return (
                  <TabsTrigger
                    key={item.key}
                    value={item.key}
                    className='h-auto justify-start gap-2 py-2'
                  >
                    <Icon className='h-4 w-4' />
                    <span className='truncate'>{item.title}</span>
                  </TabsTrigger>
                );
              })}
            </TabsList>

            <TabsContent value='FIRST_MILE' className='mt-0'>
              <DashboardLegPanel type='FIRST_MILE' data={legs.firstMile} />
            </TabsContent>
            <TabsContent value='MIDDLE_MILE' className='mt-0'>
              <DashboardLegPanel type='MIDDLE_MILE' data={legs.middleMile} />
            </TabsContent>
            <TabsContent value='LAST_MILE' className='mt-0'>
              <DashboardLegPanel type='LAST_MILE' data={legs.lastMile} />
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      <DashboardAlertsTable alerts={alerts.items} />
    </div>
  );
};

function KpiGrid({ kpis }: { kpis: DashboardKpiConfig[] }) {
  return (
    <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
      {kpis.map((item) => (
        <DashboardKpiCard key={item.key} item={item} />
      ))}
    </div>
  );
}

function MetricRow({ label, value }: { label: string; value: string }) {
  return (
    <div className='flex items-center justify-between gap-3 text-sm'>
      <span className='text-muted-foreground'>{label}</span>
      <span className='font-medium'>{value}</span>
    </div>
  );
}

function buildKpis(
  overview: TmsDashboardOverview,
  filter: TmsDashboardFilter
): DashboardKpiConfig[] {
  const fromTo = `${filter.fromDate} - ${filter.toDate}`;
  return [
    {
      key: 'orders',
      title: 'Tổng đơn hàng',
      value: formatNumber(overview.orderVolume.totalOrders),
      detail: fromTo,
      icon: Activity,
      tone: 'blue',
      trend: overview.orderVolume.growthRatePercent,
      href: `/first-mile/orders/list?fromDate=${filter.fromDate}&toDate=${filter.toDate}`,
    },
    {
      key: 'status',
      title: 'Đang xử lý',
      value: formatNumber(overview.orderVolume.inProgressOrders),
      detail: `${formatNumber(overview.orderVolume.completedOrders)} đã hoàn tất`,
      icon: Clock3,
      tone: 'amber',
      href: '/first-mile/orders/list?status=IN_PROGRESS',
    },
    {
      key: 'success',
      title: 'Tỷ lệ giao thành công',
      value: formatPercent(overview.deliverySuccess.successRatePercent),
      detail: `${formatNumber(overview.deliverySuccess.deliveredOrders)} đơn đã giao`,
      icon: CheckCircle2,
      tone: 'green',
      href: '/first-mile/orders/list?status=DELIVERED',
    },
    {
      key: 'finance',
      title: 'Doanh thu và COD',
      value: formatCurrency(
        overview.finance.grossRevenue,
        overview.finance.currency
      ),
      detail: `${formatCurrency(
        overview.finance.codPending,
        overview.finance.currency
      )} COD chờ đối soát`,
      icon: CircleDollarSign,
      tone: 'rose',
      href: '/first-mile/settings/billing',
    },
  ];
}

function resolveScopeLabel(
  selectedHubId: DashboardSelectValue,
  selectedPostOfficeCode: DashboardSelectValue
) {
  if (selectedPostOfficeCode !== 'ALL') {
    const postOffice = MOCK_POST_OFFICE_OPTIONS.find(
      (item) => item.value === selectedPostOfficeCode
    );
    return postOffice?.label ?? `Bưu cục ${selectedPostOfficeCode}`;
  }

  if (selectedHubId !== 'ALL') {
    const hub = MOCK_HUB_OPTIONS.find((item) => item.value === selectedHubId);
    return hub?.label ?? `Hub ${selectedHubId}`;
  }

  return 'Toàn hệ thống';
}
