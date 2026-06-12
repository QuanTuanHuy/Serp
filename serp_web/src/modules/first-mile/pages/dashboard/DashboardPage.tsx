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
import { useAppSelector } from '@/lib/store';
import {
  useGetHubsQuery,
  useGetPostOfficesQuery,
} from '@/modules/first-mile/api/firstMileApi';
import {
  useGetTmsDashboardAlertsQuery,
  useGetTmsDashboardLegsQuery,
  useGetTmsDashboardOverviewQuery,
} from '@/modules/first-mile/api/dashboardApi';
import type {
  TmsDashboardFilter,
  TmsDashboardGranularity,
  TmsDashboardLeg,
  TmsDashboardOverview,
} from '@/modules/first-mile/types';

import {
  DashboardAlertsTable,
  DashboardEmptyState,
  DashboardErrorState,
  DashboardFilters,
  DashboardKpiCard,
  DashboardLegPanel,
  DashboardLoadingState,
  BreakdownChart,
} from './components';
import type {
  DashboardDatePreset,
  DashboardKpiConfig,
  DashboardSelectValue,
} from './dashboardPageModels';
import {
  buildDashboardFilter,
  buildHubOptions,
  buildPostOfficeOptions,
  buildPresetRange,
  formatCurrency,
  formatDateTime,
  formatNumber,
  formatPercent,
  isDashboardEmpty,
  LEG_SUMMARIES,
} from './dashboardPageModels';

const DASHBOARD_POLLING_INTERVAL_MS = 45_000;

export const DashboardPage: React.FC = () => {
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const isAdmin = roles.includes('TMS_ADMIN');
  const isHubManager = roles.includes('TMS_HUB_MANAGER');
  const isPostOfficeManager = roles.includes('TMS_POSTOFFICER_MANAGER');

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

  const canUseHubFilter = isAdmin || isHubManager;
  const canUsePostOfficeFilter = isAdmin || isHubManager || isPostOfficeManager;

  const { data: hubsData } = useGetHubsQuery({
    page: 0,
    size: 200,
    status: 'ACTIVE',
  });
  const { data: postOfficesData } = useGetPostOfficesQuery({
    page: 0,
    size: 500,
    status: 'ACTIVE',
    ...(selectedHubId !== 'ALL' ? { hubId: Number(selectedHubId) } : {}),
  });

  const hubs = React.useMemo(() => hubsData?.items ?? [], [hubsData?.items]);
  const postOffices = React.useMemo(
    () => postOfficesData?.items ?? [],
    [postOfficesData?.items]
  );
  const hubOptions = React.useMemo(() => buildHubOptions(hubs), [hubs]);
  const postOfficeOptions = React.useMemo(
    () => buildPostOfficeOptions(postOffices),
    [postOffices]
  );

  React.useEffect(() => {
    setSelectedPostOfficeCode('ALL');
  }, [selectedHubId]);

  const filter = React.useMemo<TmsDashboardFilter>(
    () =>
      buildDashboardFilter({
        fromDate,
        toDate,
        granularity,
        selectedHubId,
        selectedPostOfficeCode,
        selectedServiceType,
        postOfficesInScope: postOffices,
      }),
    [
      fromDate,
      granularity,
      postOffices,
      selectedHubId,
      selectedPostOfficeCode,
      selectedServiceType,
      toDate,
    ]
  );

  const {
    data: overview,
    isLoading: isLoadingOverview,
    isFetching: isFetchingOverview,
    isError: isOverviewError,
    refetch: refetchOverview,
  } = useGetTmsDashboardOverviewQuery(filter, {
    pollingInterval: DASHBOARD_POLLING_INTERVAL_MS,
  });
  const {
    data: legs,
    isLoading: isLoadingLegs,
    isFetching: isFetchingLegs,
    isError: isLegsError,
    refetch: refetchLegs,
  } = useGetTmsDashboardLegsQuery(filter, {
    pollingInterval: DASHBOARD_POLLING_INTERVAL_MS,
  });
  const {
    data: alerts,
    isLoading: isLoadingAlerts,
    isFetching: isFetchingAlerts,
    isError: isAlertsError,
    refetch: refetchAlerts,
  } = useGetTmsDashboardAlertsQuery(
    { ...filter, size: 12 },
    {
      pollingInterval: DASHBOARD_POLLING_INTERVAL_MS,
    }
  );

  const isInitialLoading =
    isLoadingOverview || isLoadingLegs || isLoadingAlerts;
  const isRefreshing = isFetchingOverview || isFetchingLegs || isFetchingAlerts;
  const hasError = isOverviewError || isLegsError || isAlertsError;
  const activeScopeLabel = resolveScopeLabel({
    selectedHubId,
    selectedPostOfficeCode,
    hubs,
    postOffices,
    overview,
  });
  const kpis = React.useMemo(
    () => buildKpis(overview, filter),
    [filter, overview]
  );
  const empty = isDashboardEmpty(
    overview?.orderVolume.totalOrders,
    legs?.firstMile.totalOrders,
    legs?.middleMile.totalOrders,
    legs?.lastMile.totalOrders
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
    refetchOverview();
    refetchLegs();
    refetchAlerts();
  };

  return (
    <div className='space-y-5 p-4 md:p-6'>
      <div className='flex flex-col gap-3 xl:flex-row xl:items-start xl:justify-between'>
        <div className='space-y-2'>
          <div className='flex flex-wrap items-center gap-2'>
            <h1 className='text-2xl font-semibold tracking-normal'>
              TMS Dashboard
            </h1>
            <Badge variant='outline'>{activeScopeLabel}</Badge>
            {overview?.finance.estimated && (
              <Badge variant='secondary'>Estimated finance</Badge>
            )}
          </div>
          <div className='text-sm text-muted-foreground'>
            Last updated {formatDateTime(overview?.lastUpdatedAt)}
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
            hubOptions={hubOptions}
            postOfficeOptions={postOfficeOptions}
            canUseHubFilter={canUseHubFilter}
            canUsePostOfficeFilter={canUsePostOfficeFilter}
            isRefreshing={isRefreshing}
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

      {isInitialLoading ? (
        <DashboardLoadingState />
      ) : hasError ? (
        <DashboardErrorState onRetry={handleRefresh} />
      ) : empty ? (
        <>
          <KpiGrid kpis={kpis} />
          <DashboardEmptyState />
        </>
      ) : (
        <>
          <KpiGrid kpis={kpis} />

          <div className='grid gap-4 xl:grid-cols-[1.15fr_0.85fr]'>
            <BreakdownChart
              title='Real-time order status'
              items={(overview?.orderStatuses ?? []).map((item) => ({
                code: item.statusCode,
                name: item.statusName,
                count: item.count,
                percentage: item.percentage,
              }))}
              emptyText='No order status data'
            />

            <Card className='rounded-lg'>
              <CardHeader className='pb-2'>
                <CardTitle className='text-base'>
                  Delivery and COD position
                </CardTitle>
                <CardDescription>
                  Order-based revenue, collection, and pending COD.
                </CardDescription>
              </CardHeader>
              <CardContent className='space-y-4'>
                <MetricRow
                  label='Delivered'
                  value={formatNumber(
                    overview?.deliverySuccess.deliveredOrders
                  )}
                />
                <MetricRow
                  label='Failed or returned'
                  value={formatNumber(
                    (overview?.deliverySuccess.failedDeliveryOrders ?? 0) +
                      (overview?.deliverySuccess.returnedOrders ?? 0)
                  )}
                />
                <MetricRow
                  label='COD collected'
                  value={formatCurrency(
                    overview?.finance.codCollected,
                    overview?.finance.currency
                  )}
                />
                <MetricRow
                  label='COD pending'
                  value={formatCurrency(
                    overview?.finance.codPending,
                    overview?.finance.currency
                  )}
                />
                <Separator />
                <div className='flex items-center justify-between gap-3'>
                  <span className='text-sm text-muted-foreground'>
                    Success rate
                  </span>
                  <span className='text-lg font-semibold'>
                    {formatPercent(
                      overview?.deliverySuccess.successRatePercent
                    )}
                  </span>
                </div>
              </CardContent>
            </Card>
          </div>

          {legs && (
            <Card className='rounded-lg'>
              <CardHeader className='pb-2'>
                <CardTitle className='text-base'>
                  Three-leg performance
                </CardTitle>
                <CardDescription>
                  Scoped operational performance by leg.
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
                    <DashboardLegPanel
                      type='FIRST_MILE'
                      data={legs.firstMile}
                    />
                  </TabsContent>
                  <TabsContent value='MIDDLE_MILE' className='mt-0'>
                    <DashboardLegPanel
                      type='MIDDLE_MILE'
                      data={legs.middleMile}
                    />
                  </TabsContent>
                  <TabsContent value='LAST_MILE' className='mt-0'>
                    <DashboardLegPanel type='LAST_MILE' data={legs.lastMile} />
                  </TabsContent>
                </Tabs>
              </CardContent>
            </Card>
          )}

          <DashboardAlertsTable alerts={alerts?.items ?? []} />
        </>
      )}
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
  overview: TmsDashboardOverview | undefined,
  filter: TmsDashboardFilter
): DashboardKpiConfig[] {
  const fromTo = `from ${filter.fromDate} to ${filter.toDate}`;
  return [
    {
      key: 'orders',
      title: 'Total orders',
      value: formatNumber(overview?.orderVolume.totalOrders),
      detail: fromTo,
      icon: Activity,
      tone: 'blue',
      trend: overview?.orderVolume.growthRatePercent,
      href: `/first-mile/orders?fromDate=${filter.fromDate}&toDate=${filter.toDate}`,
    },
    {
      key: 'status',
      title: 'In progress',
      value: formatNumber(overview?.orderVolume.inProgressOrders),
      detail: `${formatNumber(overview?.orderVolume.completedOrders)} completed`,
      icon: Clock3,
      tone: 'amber',
      href: '/first-mile/orders?status=IN_PROGRESS',
    },
    {
      key: 'success',
      title: 'Delivery success',
      value: formatPercent(overview?.deliverySuccess.successRatePercent),
      detail: `${formatNumber(
        overview?.deliverySuccess.deliveredOrders
      )} delivered`,
      icon: CheckCircle2,
      tone: 'green',
      href: '/first-mile/orders?status=DELIVERED',
    },
    {
      key: 'finance',
      title: 'Revenue and COD',
      value: formatCurrency(
        overview?.finance.grossRevenue,
        overview?.finance.currency
      ),
      detail: `${formatCurrency(
        overview?.finance.codPending,
        overview?.finance.currency
      )} COD pending`,
      icon: CircleDollarSign,
      tone: 'rose',
      href: '/first-mile/billing',
    },
  ];
}

function resolveScopeLabel({
  selectedHubId,
  selectedPostOfficeCode,
  hubs,
  postOffices,
  overview,
}: {
  selectedHubId: DashboardSelectValue;
  selectedPostOfficeCode: DashboardSelectValue;
  hubs: { id: number; code: string; name: string }[];
  postOffices: { code: string; name: string }[];
  overview?: TmsDashboardOverview;
}): string {
  if (selectedPostOfficeCode !== 'ALL') {
    const postOffice = postOffices.find(
      (item) => item.code === selectedPostOfficeCode
    );
    return postOffice
      ? `Post office: ${postOffice.code}`
      : `Post office: ${selectedPostOfficeCode}`;
  }

  if (selectedHubId !== 'ALL') {
    const hub = hubs.find((item) => String(item.id) === selectedHubId);
    return hub ? `Hub: ${hub.code}` : `Hub: ${selectedHubId}`;
  }

  const accessLevel = overview?.scope.accessLevel;
  if (accessLevel === 'HUB_MANAGER') {
    return 'Hub manager scope';
  }
  if (accessLevel === 'POST_OFFICE_MANAGER') {
    return 'Post office manager scope';
  }
  if (accessLevel === 'ADMIN') {
    return 'All TMS scope';
  }
  return 'Current scope';
}
