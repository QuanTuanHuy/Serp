/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher plan preview card
 */

import React from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  MapPin,
  PackageCheck,
  Route as RouteIcon,
  Timer,
  UserRound,
} from 'lucide-react';
import {
  Badge,
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

  return parsedDate.toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

const getCourierLabel = (routeItem: PickupOptimizationRoute): string => {
  const courierCode =
    routeItem.courierCode ||
    (routeItem.courierStaffId ? `Staff #${routeItem.courierStaffId}` : '--');

  return routeItem.courierName
    ? `${courierCode} - ${routeItem.courierName}`
    : courierCode;
};

const getOrderLabel = (stop: PickupOptimizationStop): string => {
  return stop.orderCode || stop.customerOrderCode || `Order #${stop.orderId}`;
};

const getUnassignedOrderLabel = (
  order: PickupOptimizationUnassignedOrder
): string => {
  return (
    order.orderCode || order.customerOrderCode || `Order #${order.orderId}`
  );
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
}) => {
  const assignedOrders = toSafeNumber(optimizationResult.assignedOrders);
  const totalOrders = toSafeNumber(optimizationResult.totalOrders);
  const unassignedOrders = toSafeNumber(optimizationResult.unassignedOrders);
  const routeItems = optimizationResult.routes ?? [];
  const unassignedDetails = optimizationResult.unassignedOrderDetails ?? [];
  const assignmentRate = totalOrders
    ? clampPercentage(Math.round((assignedOrders / totalOrders) * 100))
    : 0;
  const hasPlanRisks =
    unassignedOrders > 0 ||
    toSafeNumber(optimizationResult.totalLatenessMinutes) > 0;

  const summaryItems = [
    {
      label: 'Assigned orders',
      value: `${formatNumber(
        optimizationResult.assignedOrders
      )} / ${formatNumber(optimizationResult.totalOrders)}`,
      helper: `${assignmentRate}% of candidate orders`,
      icon: PackageCheck,
    },
    {
      label: 'Route usage',
      value: `${formatNumber(
        optimizationResult.usedRoutes
      )} / ${formatNumber(optimizationResult.totalRoutes)}`,
      helper: 'Couriers used by the optimizer',
      icon: RouteIcon,
    },
    {
      label: 'Total distance',
      value: formatMetric(
        optimizationResult.totalDistanceKm,
        'km',
        formatNumber
      ),
      helper: formatMetric(
        optimizationResult.totalTravelMinutes,
        'travel min',
        formatNumber
      ),
      icon: MapPin,
    },
    {
      label: 'Lateness',
      value: formatMetric(
        optimizationResult.totalLatenessMinutes,
        'min',
        formatNumber
      ),
      helper: `Objective score ${formatNumber(
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
            <CardTitle>Pickup Plan Preview</CardTitle>
            <CardDescription>
              Post office {optimizationResult.postOfficeCode || '--'} -{' '}
              {optimizationResult.postOfficeName || '--'}
            </CardDescription>
          </div>
          <div className='flex flex-wrap gap-2'>
            <Badge variant={hasPlanRisks ? 'destructive' : 'secondary'}>
              {hasPlanRisks ? 'Needs review' : 'Ready to assign'}
            </Badge>
            <Badge variant='outline'>
              {formatDateTime(optimizationResult.planningStartTime)} -{' '}
              {formatDateTime(optimizationResult.planningEndTime)}
            </Badge>
          </div>
        </div>
        <div className='space-y-2'>
          <div className='flex items-center justify-between gap-3 text-xs text-muted-foreground'>
            <span>Assignment coverage</span>
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
              Unassigned orders
            </p>
            <p className='mt-1 text-lg font-semibold'>
              {formatNumber(optimizationResult.unassignedOrders)}
            </p>
          </div>
          <div className='rounded-md border bg-muted/30 p-3'>
            <p className='text-xs font-medium text-muted-foreground'>
              Service time
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
              Candidate orders
            </p>
            <p className='mt-1 text-lg font-semibold'>
              {formatNumber(optimizationResult.totalOrders)}
            </p>
          </div>
        </div>

        {routeItems.length > 0 ? (
          <div className='space-y-3'>
            <div>
              <h3 className='font-semibold'>Courier routes</h3>
              <p className='text-xs text-muted-foreground'>
                {formatNumber(routeItems.length)} route(s) generated in this
                preview.
              </p>
            </div>
            <div className='grid gap-3'>
              {routeItems.map((routeItem, index) => {
                const stops = routeItem.stops ?? [];
                const routeHasLateness =
                  toSafeNumber(routeItem.totalLatenessMinutes) > 0 ||
                  stops.some((stop) => toSafeNumber(stop.latenessMinutes) > 0);

                return (
                  <div
                    key={`${routeItem.courierStaffId ?? 'route'}-${index}`}
                    className='rounded-md border'
                  >
                    <div className='border-b bg-muted/30 p-4'>
                      <div className='flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between'>
                        <div className='min-w-0 space-y-2'>
                          <div className='flex flex-wrap items-center gap-2'>
                            <Badge variant='outline'>Route {index + 1}</Badge>
                            <Badge
                              variant={
                                routeHasLateness ? 'destructive' : 'secondary'
                              }
                            >
                              {routeHasLateness ? 'Late stops' : 'On time'}
                            </Badge>
                          </div>
                          <div className='flex items-start gap-2'>
                            <UserRound className='mt-0.5 h-4 w-4 text-muted-foreground' />
                            <div className='min-w-0'>
                              <p className='truncate font-medium'>
                                {getCourierLabel(routeItem)}
                              </p>
                              <p className='text-xs text-muted-foreground'>
                                Staff ID {routeItem.courierStaffId ?? '--'}
                                {routeItem.vehicleLicensePlate
                                  ? ` | Vehicle ${routeItem.vehicleLicensePlate}`
                                  : ''}
                              </p>
                            </div>
                          </div>
                        </div>
                        <div className='grid gap-2 text-xs sm:grid-cols-2 lg:min-w-[24rem]'>
                          <RouteStat
                            label='Stops'
                            value={formatNumber(routeItem.totalStops)}
                          />
                          <RouteStat
                            label='Distance'
                            value={formatMetric(
                              routeItem.totalDistanceKm,
                              'km',
                              formatNumber
                            )}
                          />
                          <RouteStat
                            label='Travel'
                            value={formatMetric(
                              routeItem.totalTravelMinutes,
                              'min',
                              formatNumber
                            )}
                          />
                          <RouteStat
                            label='Service'
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
                          Window {formatDateTime(routeItem.startTime)} -{' '}
                          {formatDateTime(routeItem.endTime)}
                        </span>
                        <span>
                          Load {formatNumber(routeItem.totalWeight)} weight /{' '}
                          {formatNumber(routeItem.totalVolume)} volume
                        </span>
                        <span>
                          Lateness{' '}
                          {formatMetric(
                            routeItem.totalLatenessMinutes,
                            'min',
                            formatNumber
                          )}
                        </span>
                      </div>
                    </div>

                    {stops.length > 0 ? (
                      <div className='max-h-[30rem] space-y-3 overflow-y-auto p-4'>
                        {stops.map((stop) => (
                          <div
                            key={`${stop.sequence}-${stop.orderId}`}
                            className='grid gap-3 rounded-md border bg-background p-3 md:grid-cols-[auto_minmax(0,1fr)_minmax(11rem,0.8fr)]'
                          >
                            <div className='flex h-8 w-8 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground'>
                              {stop.sequence}
                            </div>
                            <div className='min-w-0 space-y-1'>
                              <div className='flex flex-wrap items-center gap-2'>
                                <p className='font-medium'>
                                  {getOrderLabel(stop)}
                                </p>
                                {stop.customerOrderCode ? (
                                  <Badge variant='outline'>
                                    {stop.customerOrderCode}
                                  </Badge>
                                ) : null}
                                {toSafeNumber(stop.latenessMinutes) > 0 ? (
                                  <Badge variant='destructive'>
                                    Late{' '}
                                    {formatMetric(
                                      stop.latenessMinutes,
                                      'min',
                                      formatNumber
                                    )}
                                  </Badge>
                                ) : (
                                  <Badge variant='secondary'>On time</Badge>
                                )}
                              </div>
                              <p className='text-xs text-muted-foreground'>
                                Sender {stop.senderName || '--'}
                                {stop.senderPhone
                                  ? ` | ${stop.senderPhone}`
                                  : ''}
                              </p>
                              <p className='text-xs text-muted-foreground'>
                                Pickup window{' '}
                                {formatStopTime(stop.pickupTimeStart)} -{' '}
                                {formatStopTime(stop.pickupTimeEnd)}
                              </p>
                            </div>
                            <div className='space-y-1 text-xs text-muted-foreground'>
                              <p className='flex items-center gap-1'>
                                <Clock3 className='h-3.5 w-3.5' />
                                Arrive {formatStopTime(stop.arrivalTime)}
                              </p>
                              <p>
                                Depart {formatStopTime(stop.departureTime)} |
                                Travel{' '}
                                {formatMetric(
                                  stop.travelMinutes,
                                  'min',
                                  formatNumber
                                )}
                              </p>
                              <p>
                                From previous{' '}
                                {formatMetric(
                                  stop.distanceFromPreviousKm,
                                  'km',
                                  formatNumber
                                )}
                              </p>
                            </div>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className='p-4 text-sm text-muted-foreground'>
                        No stops were placed on this route.
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        ) : (
          <div className='rounded-md border border-dashed p-6 text-center text-muted-foreground'>
            <RouteIcon className='mx-auto mb-2 h-5 w-5' />
            No courier routes in this preview.
          </div>
        )}

        {unassignedDetails.length > 0 ? (
          <div className='rounded-md border border-destructive/30 bg-destructive/5 p-4'>
            <div className='flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
              <div className='flex items-center gap-2 font-medium text-destructive'>
                <AlertTriangle className='h-4 w-4' />
                Orders needing review
              </div>
              <Badge variant='destructive'>
                {formatNumber(unassignedDetails.length)} unassigned
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
                    {order.reason || 'No optimizer reason provided.'}
                  </p>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className='flex items-center gap-2 rounded-md border border-primary/30 bg-primary/10 p-3 text-sm'>
            <CheckCircle2 className='h-4 w-4 text-primary' />
            All candidate orders are assigned in this preview.
          </div>
        )}
      </CardContent>
    </Card>
  );
};
