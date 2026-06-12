/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS dashboard leg panel
 */

import { AlertTriangle, Clock3, PackageCheck, Percent } from 'lucide-react';
import type React from 'react';

import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

import type {
  TmsDashboardFirstMile,
  TmsDashboardLastMile,
  TmsDashboardMiddleMile,
} from '@/modules/first-mile/types';
import {
  formatMinutes,
  formatNumber,
  formatPercent,
} from '../dashboardPageModels';
import { BreakdownChart, TrendChart } from './DashboardCharts';

type LegPanelProps =
  | { type: 'FIRST_MILE'; data: TmsDashboardFirstMile }
  | { type: 'MIDDLE_MILE'; data: TmsDashboardMiddleMile }
  | { type: 'LAST_MILE'; data: TmsDashboardLastMile };

export function DashboardLegPanel(props: LegPanelProps) {
  if (props.type === 'FIRST_MILE') {
    return (
      <div className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-4'>
          <MiniMetric
            title='First-mile orders'
            value={formatNumber(props.data.totalOrders)}
            icon={PackageCheck}
          />
          <MiniMetric
            title='Pickup success'
            value={formatPercent(props.data.pickupSuccessRatePercent)}
            icon={Percent}
          />
          <MiniMetric
            title='Avg pickup time'
            value={formatMinutes(props.data.avgPickupMinutes)}
            icon={Clock3}
          />
          <MiniMetric
            title='Pickup SLA breaches'
            value={formatNumber(props.data.slaBreachedOrders)}
            icon={AlertTriangle}
          />
        </div>
        <div className='grid gap-4 xl:grid-cols-[1fr_1fr]'>
          <TrendChart
            title='First-mile volume'
            points={props.data.trend}
            emptyText='No first-mile trend data'
          />
          <BreakdownChart
            title='First-mile status'
            items={props.data.statusBreakdown}
            emptyText='No first-mile status data'
          />
        </div>
        <TopEntityList
          title='Top post offices'
          items={props.data.topPostOffices}
        />
      </div>
    );
  }

  if (props.type === 'MIDDLE_MILE') {
    return (
      <div className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-4'>
          <MiniMetric
            title='Middle-mile orders'
            value={formatNumber(props.data.totalOrders)}
            icon={PackageCheck}
          />
          <MiniMetric
            title='Bags'
            value={formatNumber(props.data.totalBags)}
            icon={PackageCheck}
          />
          <MiniMetric
            title='Routes'
            value={formatNumber(props.data.totalRoutes)}
            icon={Clock3}
          />
          <MiniMetric
            title='On-time routes'
            value={formatPercent(props.data.onTimeRouteRatePercent)}
            icon={Percent}
          />
        </div>
        <div className='grid gap-4 xl:grid-cols-[1fr_1fr]'>
          <BreakdownChart
            title='Bag and route stage'
            items={props.data.statusBreakdown}
            emptyText='No middle-mile status data'
          />
          <TopEntityList title='Top hubs' items={props.data.topHubs} />
        </div>
      </div>
    );
  }

  return (
    <div className='space-y-4'>
      <div className='grid gap-3 md:grid-cols-4'>
        <MiniMetric
          title='Last-mile orders'
          value={formatNumber(props.data.totalOrders)}
          icon={PackageCheck}
        />
        <MiniMetric
          title='Delivery success'
          value={formatPercent(props.data.deliverySuccessRatePercent)}
          icon={Percent}
        />
        <MiniMetric
          title='Avg delivery time'
          value={formatMinutes(props.data.avgDeliveryMinutes)}
          icon={Clock3}
        />
        <MiniMetric
          title='Delivery SLA breaches'
          value={formatNumber(props.data.slaBreachedOrders)}
          icon={AlertTriangle}
        />
      </div>
      <div className='grid gap-4 xl:grid-cols-[1fr_1fr]'>
        <TrendChart
          title='Last-mile volume'
          points={props.data.trend}
          emptyText='No last-mile trend data'
        />
        <BreakdownChart
          title='Last-mile status'
          items={props.data.statusBreakdown}
          emptyText='No last-mile status data'
        />
      </div>
      <BreakdownChart
        title='Failed delivery reasons'
        items={props.data.failedReasons}
        emptyText='No failed delivery data'
      />
    </div>
  );
}

function MiniMetric({
  title,
  value,
  icon: Icon,
}: {
  title: string;
  value: string;
  icon: React.ComponentType<{ className?: string }>;
}) {
  return (
    <Card className='rounded-lg'>
      <CardContent className='flex items-center justify-between gap-3 p-4'>
        <div className='min-w-0'>
          <div className='truncate text-xs text-muted-foreground'>{title}</div>
          <div className='mt-1 truncate text-lg font-semibold'>{value}</div>
        </div>
        <div className='flex h-9 w-9 shrink-0 items-center justify-center rounded-md bg-muted'>
          <Icon className='h-4 w-4' />
        </div>
      </CardContent>
    </Card>
  );
}

function TopEntityList({
  title,
  items,
}: {
  title: string;
  items: { code: string; name: string; count: number; percentage: number }[];
}) {
  return (
    <Card className='rounded-lg'>
      <CardHeader className='pb-2'>
        <CardTitle className='text-base'>{title}</CardTitle>
      </CardHeader>
      <CardContent className='space-y-3'>
        {items.length === 0 ? (
          <div className='flex h-24 items-center justify-center rounded-md border border-dashed text-sm text-muted-foreground'>
            No ranked data
          </div>
        ) : (
          items.map((item) => (
            <div key={item.code} className='space-y-1.5'>
              <div className='flex items-center justify-between gap-3 text-sm'>
                <span className='min-w-0 truncate font-medium'>
                  {item.name}
                </span>
                <span className='shrink-0 text-muted-foreground'>
                  {formatNumber(item.count)}
                </span>
              </div>
              <div className='h-2 overflow-hidden rounded-full bg-muted'>
                <div
                  className='h-full rounded-full bg-primary'
                  style={{ width: `${Math.min(item.percentage, 100)}%` }}
                />
              </div>
            </div>
          ))
        )}
      </CardContent>
    </Card>
  );
}
