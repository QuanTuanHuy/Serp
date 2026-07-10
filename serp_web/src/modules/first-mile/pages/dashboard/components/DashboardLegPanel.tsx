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
            title='Đơn chặng lấy hàng'
            value={formatNumber(props.data.totalOrders)}
            icon={PackageCheck}
          />
          <MiniMetric
            title='Lấy hàng thành công'
            value={formatPercent(props.data.pickupSuccessRatePercent)}
            icon={Percent}
          />
          <MiniMetric
            title='Thời gian lấy TB'
            value={formatMinutes(props.data.avgPickupMinutes)}
            icon={Clock3}
          />
          <MiniMetric
            title='Quá SLA lấy hàng'
            value={formatNumber(props.data.slaBreachedOrders)}
            icon={AlertTriangle}
          />
        </div>
        <div className='grid gap-4 xl:grid-cols-[1fr_1fr]'>
          <TrendChart
            title='Sản lượng chặng lấy hàng'
            points={props.data.trend}
            emptyText='Chưa có dữ liệu xu hướng chặng lấy hàng'
          />
          <BreakdownChart
            title='Trạng thái chặng lấy hàng'
            items={props.data.statusBreakdown}
            emptyText='Chưa có dữ liệu trạng thái chặng lấy hàng'
          />
        </div>
        <TopEntityList
          title='Bưu cục nổi bật'
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
            title='Đơn chặng trung chuyển'
            value={formatNumber(props.data.totalOrders)}
            icon={PackageCheck}
          />
          <MiniMetric
            title='Túi hàng'
            value={formatNumber(props.data.totalBags)}
            icon={PackageCheck}
          />
          <MiniMetric
            title='Tuyến'
            value={formatNumber(props.data.totalRoutes)}
            icon={Clock3}
          />
          <MiniMetric
            title='Tuyến đúng giờ'
            value={formatPercent(props.data.onTimeRouteRatePercent)}
            icon={Percent}
          />
        </div>
        <div className='grid gap-4 xl:grid-cols-[1fr_1fr]'>
          <BreakdownChart
            title='Trạng thái túi và tuyến'
            items={props.data.statusBreakdown}
            emptyText='Chưa có dữ liệu trạng thái chặng trung chuyển'
          />
          <TopEntityList title='Hub nổi bật' items={props.data.topHubs} />
        </div>
      </div>
    );
  }

  return (
    <div className='space-y-4'>
      <div className='grid gap-3 md:grid-cols-4'>
        <MiniMetric
          title='Đơn chặng giao hàng'
          value={formatNumber(props.data.totalOrders)}
          icon={PackageCheck}
        />
        <MiniMetric
          title='Giao thành công'
          value={formatPercent(props.data.deliverySuccessRatePercent)}
          icon={Percent}
        />
        <MiniMetric
          title='Thời gian giao TB'
          value={formatMinutes(props.data.avgDeliveryMinutes)}
          icon={Clock3}
        />
        <MiniMetric
          title='Quá SLA giao hàng'
          value={formatNumber(props.data.slaBreachedOrders)}
          icon={AlertTriangle}
        />
      </div>
      <div className='grid gap-4 xl:grid-cols-[1fr_1fr]'>
        <TrendChart
          title='Sản lượng chặng giao hàng'
          points={props.data.trend}
          emptyText='Chưa có dữ liệu xu hướng chặng giao hàng'
        />
        <BreakdownChart
          title='Trạng thái chặng giao hàng'
          items={props.data.statusBreakdown}
          emptyText='Chưa có dữ liệu trạng thái chặng giao hàng'
        />
      </div>
      <BreakdownChart
        title='Lý do giao không thành công'
        items={props.data.failedReasons}
        emptyText='Chưa có dữ liệu giao không thành công'
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
            Chưa có dữ liệu xếp hạng
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
