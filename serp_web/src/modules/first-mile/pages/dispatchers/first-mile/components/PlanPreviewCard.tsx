/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher plan preview card
 */

import React from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  ChevronDown,
  Clock3,
  MapPin,
  PackageCheck,
  Route as RouteIcon,
  Timer,
  UserRound,
} from 'lucide-react';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import type {
  PickupOptimizationRoute,
  PickupOptimizationStop,
  PickupOptimizationUnassignedOrder,
} from '../../../../types';
import {
  formatPickupBacklogDuration,
  getPickupBacklogMinutes,
  isPickupBacklogOrder,
} from '../dispatchOrderBacklog';
import { PlanPreviewRouteMap } from './PlanPreviewRouteMap';
import type { PlanPreviewCardProps } from './types';

const toSafeNumber = (value?: number): number => {
  return value ?? 0;
};

const clampPercentage = (value: number): number => {
  return Math.min(100, Math.max(0, value));
};

const formatMetric = (
  value: number | undefined,
  unit: string,
  formatNumber: (value?: number) => string
): string => {
  if (value === undefined || value === null) {
    return '--';
  }

  return `${formatNumber(value)} ${unit}`;
};

const formatStopTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

const getCourierLabel = (routeItem: PickupOptimizationRoute): string => {
  const courierCode =
    routeItem.courierCode ||
    (routeItem.courierStaffId
      ? `Nhân viên #${routeItem.courierStaffId}`
      : '--');

  return routeItem.courierName
    ? `${courierCode} - ${routeItem.courierName}`
    : courierCode;
};

const getRouteExpansionKey = (
  routeItem: PickupOptimizationRoute,
  index: number
): string => {
  return `${routeItem.courierStaffId ?? routeItem.courierCode ?? 'route'}-${index}`;
};

const getOrderLabel = (stop: PickupOptimizationStop): string => {
  return stop.orderCode || stop.customerOrderCode || `Đơn #${stop.orderId}`;
};

const getUnassignedOrderLabel = (
  order: PickupOptimizationUnassignedOrder
): string => {
  return order.orderCode || order.customerOrderCode || `Đơn #${order.orderId}`;
};

interface RouteStatProps {
  label: string;
  value: string;
}

const RouteStat: React.FC<RouteStatProps> = ({ label, value }) => {
  return (
    <div className='rounded-md border bg-background p-2'>
      <p className='text-muted-foreground'>{label}</p>
      <p className='font-medium text-foreground'>{value}</p>
    </div>
  );
};

export const PlanPreviewCard: React.FC<PlanPreviewCardProps> = ({
  optimizationResult,
  formatDateTime,
  formatNumber,
  title = 'Xem trước kế hoạch lấy hàng',
  stopContactLabel = 'Người gửi',
  stopTimeLabel = 'Khung giờ lấy hàng',
  successText = 'Tất cả đơn ứng viên đã được phân công trong bản xem trước này.',
}) => {
  const assignedOrders = toSafeNumber(optimizationResult.assignedOrders);
  const totalOrders = toSafeNumber(optimizationResult.totalOrders);
  const unassignedOrders = toSafeNumber(optimizationResult.unassignedOrders);
  const routeItems = React.useMemo(
    () => optimizationResult.routes ?? [],
    [optimizationResult.routes]
  );
  const unassignedDetails = optimizationResult.unassignedOrderDetails ?? [];
  const [expandedRouteKeys, setExpandedRouteKeys] = React.useState<Set<string>>(
    new Set()
  );
  const assignmentRate = totalOrders
    ? clampPercentage(Math.round((assignedOrders / totalOrders) * 100))
    : 0;
  const hasPlanRisks =
    unassignedOrders > 0 ||
    toSafeNumber(optimizationResult.totalLatenessMinutes) > 0;

  React.useEffect(() => {
    setExpandedRouteKeys(
      routeItems.length > 0
        ? new Set([getRouteExpansionKey(routeItems[0], 0)])
        : new Set()
    );
  }, [routeItems]);

  const handleToggleRoute = React.useCallback((routeKey: string) => {
    setExpandedRouteKeys((currentKeys) => {
      const nextKeys = new Set(currentKeys);

      if (nextKeys.has(routeKey)) {
        nextKeys.delete(routeKey);
      } else {
        nextKeys.add(routeKey);
      }

      return nextKeys;
    });
  }, []);

  const summaryItems = [
    {
      label: 'Đơn đã phân công',
      value: `${formatNumber(
        optimizationResult.assignedOrders
      )} / ${formatNumber(optimizationResult.totalOrders)}`,
      helper: `${assignmentRate}% đơn ứng viên`,
      icon: PackageCheck,
    },
    {
      label: 'Tuyến được dùng',
      value: `${formatNumber(
        optimizationResult.usedRoutes
      )} / ${formatNumber(optimizationResult.totalRoutes)}`,
      helper: 'Nhân viên được bộ tối ưu sử dụng',
      icon: RouteIcon,
    },
    {
      label: 'Tổng quãng đường',
      value: formatMetric(
        optimizationResult.totalDistanceKm,
        'km',
        formatNumber
      ),
      helper: formatMetric(
        optimizationResult.totalTravelMinutes,
        'phút di chuyển',
        formatNumber
      ),
      icon: MapPin,
    },
    {
      label: 'Trễ hạn',
      value: formatMetric(
        optimizationResult.totalLatenessMinutes,
        'min',
        formatNumber
      ),
      helper: `Điểm mục tiêu ${formatNumber(
        optimizationResult.objectiveScore
      )}`,
      icon: Timer,
    },
  ];

  return (
    <Card>
      <CardHeader className='space-y-4'>
        <div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
          <div className='space-y-1'>
            <CardTitle>{title}</CardTitle>
            <CardDescription>
              Bưu cục {optimizationResult.postOfficeCode || '--'} -{' '}
              {optimizationResult.postOfficeName || '--'}
            </CardDescription>
          </div>
          <div className='flex flex-wrap gap-2'>
            <Badge variant={hasPlanRisks ? 'destructive' : 'secondary'}>
              {hasPlanRisks ? 'Cần kiểm tra' : 'Sẵn sàng phân công'}
            </Badge>
            <Badge variant='outline'>
              {formatDateTime(optimizationResult.planningStartTime)} -{' '}
              {formatDateTime(optimizationResult.planningEndTime)}
            </Badge>
          </div>
        </div>
        <div className='space-y-2'>
          <div className='flex items-center justify-between gap-3 text-xs text-muted-foreground'>
            <span>Tỷ lệ phân công</span>
            <span>{assignmentRate}%</span>
          </div>
          <div className='h-2 overflow-hidden rounded-full bg-muted'>
            <div
              className='h-full rounded-full bg-primary transition-all'
              style={{ width: `${assignmentRate}%` }}
            />
          </div>
        </div>
      </CardHeader>
      <CardContent className='space-y-5 text-sm'>
        <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
          {summaryItems.map((item) => {
            const Icon = item.icon;

            return (
              <div key={item.label} className='rounded-md border p-3'>
                <div className='flex items-center gap-2 text-muted-foreground'>
                  <Icon className='h-4 w-4' />
                  <span className='text-xs font-medium uppercase'>
                    {item.label}
                  </span>
                </div>
                <p className='mt-2 text-lg font-semibold'>{item.value}</p>
                <p className='text-xs text-muted-foreground'>{item.helper}</p>
              </div>
            );
          })}
        </div>

        <div className='grid gap-3 md:grid-cols-3'>
          <div className='rounded-md border bg-muted/30 p-3'>
            <p className='text-xs font-medium text-muted-foreground'>
              Đơn chưa phân công
            </p>
            <p className='mt-1 text-lg font-semibold'>
              {formatNumber(optimizationResult.unassignedOrders)}
            </p>
          </div>
          <div className='rounded-md border bg-muted/30 p-3'>
            <p className='text-xs font-medium text-muted-foreground'>
              Thời gian phục vụ
            </p>
            <p className='mt-1 text-lg font-semibold'>
              {formatMetric(
                optimizationResult.totalServiceMinutes,
                'min',
                formatNumber
              )}
            </p>
          </div>
          <div className='rounded-md border bg-muted/30 p-3'>
            <p className='text-xs font-medium text-muted-foreground'>
              Đơn ứng viên
            </p>
            <p className='mt-1 text-lg font-semibold'>
              {formatNumber(optimizationResult.totalOrders)}
            </p>
          </div>
        </div>

        {routeItems.length > 0 ? (
          <div className='space-y-3'>
            <div>
              <h3 className='font-semibold'>Tuyến của nhân viên giao nhận</h3>
              <p className='text-xs text-muted-foreground'>
                {formatNumber(routeItems.length)} tuyến được tạo trong bản xem
                trước này.
              </p>
            </div>
            <div className='grid gap-4 xl:grid-cols-[minmax(0,1.05fr)_minmax(24rem,0.95fr)]'>
              <div className='xl:sticky xl:top-4 xl:self-start'>
                <PlanPreviewRouteMap
                  routes={routeItems}
                  className='h-[520px]'
                />
              </div>
              <div className='max-h-[560px] space-y-3 overflow-y-auto pr-1'>
                {routeItems.map((routeItem, index) => {
                  const stops = routeItem.stops ?? [];
                  const routeKey = getRouteExpansionKey(routeItem, index);
                  const isRouteExpanded = expandedRouteKeys.has(routeKey);
                  const routeHasLateness =
                    toSafeNumber(routeItem.totalLatenessMinutes) > 0 ||
                    stops.some(
                      (stop) => toSafeNumber(stop.latenessMinutes) > 0
                    );

                  return (
                    <div key={routeKey} className='rounded-md border'>
                      <div
                        className={`bg-muted/30 p-4 ${
                          isRouteExpanded ? 'border-b' : ''
                        }`}
                      >
                        <div className='flex flex-col gap-3 2xl:flex-row 2xl:items-start 2xl:justify-between'>
                          <div className='min-w-0 space-y-2'>
                            <div className='flex flex-wrap items-center gap-2'>
                              <Button
                                type='button'
                                variant='ghost'
                                size='sm'
                                className='h-7 w-7 p-0'
                                aria-expanded={isRouteExpanded}
                                aria-label={
                                  isRouteExpanded
                                    ? 'Thu gọn tuyến bưu tá'
                                    : 'Mở rộng tuyến bưu tá'
                                }
                                onClick={() => handleToggleRoute(routeKey)}
                              >
                                <ChevronDown
                                  className={`h-4 w-4 transition-transform ${
                                    isRouteExpanded ? '' : '-rotate-90'
                                  }`}
                                />
                              </Button>
                              <Badge variant='outline'>Tuyến {index + 1}</Badge>
                              <Badge
                                variant={
                                  routeHasLateness ? 'destructive' : 'secondary'
                                }
                              >
                                {routeHasLateness ? 'Có điểm trễ' : 'Đúng giờ'}
                              </Badge>
                            </div>
                            <div className='flex items-start gap-2'>
                              <UserRound className='mt-0.5 h-4 w-4 text-muted-foreground' />
                              <div className='min-w-0'>
                                <p className='truncate font-medium'>
                                  {getCourierLabel(routeItem)}
                                </p>
                                <p className='text-xs text-muted-foreground'>
                                  ID nhân viên{' '}
                                  {routeItem.courierStaffId ?? '--'}
                                  {routeItem.vehicleLicensePlate
                                    ? ` | Xe ${routeItem.vehicleLicensePlate}`
                                    : ''}
                                </p>
                              </div>
                            </div>
                          </div>
                          <div className='grid gap-2 text-xs sm:grid-cols-2 2xl:min-w-[24rem]'>
                            <RouteStat
                              label='Điểm dừng'
                              value={formatNumber(routeItem.totalStops)}
                            />
                            <RouteStat
                              label='Quãng đường'
                              value={formatMetric(
                                routeItem.totalDistanceKm,
                                'km',
                                formatNumber
                              )}
                            />
                            <RouteStat
                              label='Di chuyển'
                              value={formatMetric(
                                routeItem.totalTravelMinutes,
                                'min',
                                formatNumber
                              )}
                            />
                            <RouteStat
                              label='Phục vụ'
                              value={formatMetric(
                                routeItem.totalServiceMinutes,
                                'min',
                                formatNumber
                              )}
                            />
                          </div>
                        </div>
                        <div className='mt-3 grid gap-2 text-xs text-muted-foreground md:grid-cols-3'>
                          <span>
                            Khung giờ {formatDateTime(routeItem.startTime)} -{' '}
                            {formatDateTime(routeItem.endTime)}
                          </span>
                          <span>
                            Tải {formatNumber(routeItem.totalWeight)} khối lượng
                            / {formatNumber(routeItem.totalVolume)} thể tích
                          </span>
                          <span>
                            Trễ hạn{' '}
                            {formatMetric(
                              routeItem.totalLatenessMinutes,
                              'min',
                              formatNumber
                            )}
                          </span>
                        </div>
                      </div>

                      {isRouteExpanded && stops.length > 0 ? (
                        <div className='space-y-3 p-4'>
                          {stops.map((stop) => {
                            const isBacklog = isPickupBacklogOrder(stop);
                            const backlogDuration = formatPickupBacklogDuration(
                              getPickupBacklogMinutes(stop)
                            );

                            return (
                              <div
                                key={`${stop.sequence}-${stop.orderId}`}
                                className='grid gap-3 rounded-md border bg-background p-3 2xl:grid-cols-[auto_minmax(0,1fr)_minmax(11rem,0.8fr)]'
                              >
                                <div className='flex h-8 w-8 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground'>
                                  {stop.sequence}
                                </div>
                                <div className='min-w-0 space-y-1'>
                                  <div className='flex flex-wrap items-center gap-2'>
                                    <p className='font-medium'>
                                      {getOrderLabel(stop)}
                                    </p>
                                    {isBacklog ? (
                                      <Badge variant='destructive'>
                                        Quá hạn
                                      </Badge>
                                    ) : null}
                                    {stop.customerOrderCode ? (
                                      <Badge variant='outline'>
                                        {stop.customerOrderCode}
                                      </Badge>
                                    ) : null}
                                    {toSafeNumber(stop.latenessMinutes) > 0 ? (
                                      <Badge variant='destructive'>
                                        Trễ{' '}
                                        {formatMetric(
                                          stop.latenessMinutes,
                                          'min',
                                          formatNumber
                                        )}
                                      </Badge>
                                    ) : (
                                      <Badge variant='secondary'>
                                        Đúng giờ
                                      </Badge>
                                    )}
                                  </div>
                                  <p className='text-xs text-muted-foreground'>
                                    {stopContactLabel} {stop.senderName || '--'}
                                    {stop.senderPhone
                                      ? ` | ${stop.senderPhone}`
                                      : ''}
                                  </p>
                                  <p className='text-xs text-muted-foreground'>
                                    {stopTimeLabel}{' '}
                                    {formatStopTime(stop.pickupTimeStart)} -{' '}
                                    {formatStopTime(stop.pickupTimeEnd)}
                                    {backlogDuration
                                      ? ` | ${backlogDuration}`
                                      : ''}
                                  </p>
                                </div>
                                <div className='space-y-1 text-xs text-muted-foreground'>
                                  <p className='flex items-center gap-1'>
                                    <Clock3 className='h-3.5 w-3.5' />
                                    Đến {formatStopTime(stop.arrivalTime)}
                                  </p>
                                  <p>
                                    Rời {formatStopTime(stop.departureTime)} |
                                    Di chuyển{' '}
                                    {formatMetric(
                                      stop.travelMinutes,
                                      'min',
                                      formatNumber
                                    )}
                                  </p>
                                  <p>
                                    Từ điểm trước{' '}
                                    {formatMetric(
                                      stop.distanceFromPreviousKm,
                                      'km',
                                      formatNumber
                                    )}
                                  </p>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      ) : isRouteExpanded ? (
                        <div className='p-4 text-sm text-muted-foreground'>
                          Chưa có điểm dừng nào trên tuyến này.
                        </div>
                      ) : null}
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        ) : (
          <div className='rounded-md border border-dashed p-6 text-center text-muted-foreground'>
            <RouteIcon className='mx-auto mb-2 h-5 w-5' />
            Không có tuyến nhân viên nào trong bản xem trước này.
          </div>
        )}

        {unassignedDetails.length > 0 ? (
          <div className='rounded-md border border-destructive/30 bg-destructive/5 p-4'>
            <div className='flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
              <div className='flex items-center gap-2 font-medium text-destructive'>
                <AlertTriangle className='h-4 w-4' />
                Đơn cần kiểm tra
              </div>
              <Badge variant='destructive'>
                {formatNumber(unassignedDetails.length)} chưa phân công
              </Badge>
            </div>
            <div className='mt-3 grid gap-2 md:grid-cols-2'>
              {unassignedDetails.map((order) => (
                <div
                  key={order.orderId}
                  className='rounded-md border bg-background p-3'
                >
                  <p className='font-medium'>
                    {getUnassignedOrderLabel(order)}
                  </p>
                  <p className='text-xs text-muted-foreground'>
                    {order.reason || 'Bộ tối ưu chưa cung cấp lý do.'}
                  </p>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className='flex items-center gap-2 rounded-md border border-primary/30 bg-primary/10 p-3 text-sm'>
            <CheckCircle2 className='h-4 w-4 text-primary' />
            {successText}
          </div>
        )}
      </CardContent>
    </Card>
  );
};
