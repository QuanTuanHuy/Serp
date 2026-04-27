'use client';

import { Fragment, useMemo } from 'react';
import {
  MapContainer,
  Marker,
  Polyline,
  Popup,
  TileLayer,
  Tooltip as LeafletTooltip,
} from 'react-leaflet';
import L from 'leaflet';
import {
  CheckCircle2,
  MapPin,
  Phone,
  SkipForward,
  Truck,
  Warehouse,
  XCircle,
} from 'lucide-react';
import { renderToString } from 'react-dom/server';
import { Button } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { decodePolyline } from '@/shared/utils/polyline';
import type { Route, RouteStop } from '../../types';

interface RouteStopActionState {
  id: string;
  type: 'complete' | 'abort';
}

interface RouteMapProps {
  route: Route;
  canOperateStops?: boolean;
  onCompleteRouteStop?: (routeStopId: string) => Promise<void> | void;
  onAbortRouteStop?: (routeStopId: string) => Promise<void> | void;
  routeStopAction?: RouteStopActionState | null;
}

type RouteMapSegment = {
  stop: RouteStop;
  path: [number, number][];
  location: [number, number];
  isPassed: boolean;
  isNextStop: boolean;
};

const ROUTE_STOP_CONFIG = {
  WAITING: {
    label: 'Đang chờ giao',
    badgeClass: 'border-amber-200 bg-amber-50 text-amber-700',
  },
  ARRIVED: {
    label: 'Đã giao',
    badgeClass: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  },
  FAILED: {
    label: 'Đã bỏ qua',
    badgeClass: 'border-rose-200 bg-rose-50 text-rose-700',
  },
} as const;

const formatNumber = (value?: number | null, suffix?: string) => {
  const safeValue = value ?? 0;

  return `${new Intl.NumberFormat('vi-VN').format(safeValue)}${
    suffix ? ` ${suffix}` : ''
  }`;
};

// Helper tạo custom icon từ Lucide
const createCustomIcon = (
  IconComponent: any,
  colorClass: string,
  bgClass: string
) => {
  const iconHtml = renderToString(
    <div
      className={`flex h-8 w-8 items-center justify-center rounded-full border-2 border-white shadow-md ${bgClass} ${colorClass}`}
    >
      <IconComponent size={16} />
    </div>
  );

  return L.divIcon({
    html: iconHtml,
    className: 'custom-leaflet-icon',
    iconSize: [32, 32],
    iconAnchor: [16, 16],
    popupAnchor: [0, -16],
  });
};

const ICONS = {
  WAREHOUSE: createCustomIcon(Warehouse, 'text-slate-700', 'bg-slate-200'),
  WAITING: createCustomIcon(MapPin, 'text-amber-700', 'bg-amber-100'),
  ARRIVED: createCustomIcon(MapPin, 'text-emerald-700', 'bg-emerald-100'),
  FAILED: createCustomIcon(XCircle, 'text-rose-700', 'bg-rose-100'),
  TRUCK: createCustomIcon(Truck, 'text-blue-700', 'bg-blue-100'),
};

export default function RouteMap({
  route,
  canOperateStops = false,
  onCompleteRouteStop,
  onAbortRouteStop,
  routeStopAction = null,
}: RouteMapProps) {
  const canShowStopActions = Boolean(
    canOperateStops && onCompleteRouteStop && onAbortRouteStop
  );

  const mapData = useMemo(() => {
    if (!route?.routeStops?.length) {
      return null;
    }

    const stops = [...route.routeStops].sort((a, b) => a.sequence - b.sequence);
    const nextWaitingStopId =
      stops.find((stop) => stop.status === 'WAITING')?.id ?? null;

    let allPoints: [number, number][] = [];
    let warehouseLocation: [number, number] | null = null;
    let latestArrivedIndex = -1;
    const segments: RouteMapSegment[] = [];

    stops.forEach((stop) => {
      const path = stop.encodedPolyline
        ? decodePolyline(stop.encodedPolyline)
        : [];

      if (path.length === 0) {
        return;
      }

      allPoints = [...allPoints, ...path];

      // Điểm bắt đầu của polyline đầu tiên chính là Kho (Warehouse)
      if (!warehouseLocation) {
        warehouseLocation = path[0];
      }

      const location = path[path.length - 1];
      const isPassed = stop.status === 'ARRIVED';

      segments.push({
        stop,
        path,
        // Điểm cuối của polyline là vị trí điểm giao hàng
        location,
        isPassed,
        isNextStop: stop.id === nextWaitingStopId,
      });

      if (isPassed) {
        latestArrivedIndex = segments.length - 1;
      }
    });

    if (!warehouseLocation) {
      return null;
    }

    if (allPoints.length === 0) {
      allPoints = [warehouseLocation];
    }

    // Xác định vị trí xe tải (Truck)
    let currentTruckLocation: [number, number] | null = warehouseLocation;
    if (route.status === 'IN_PROGRESS') {
      if (latestArrivedIndex >= 0 && segments[latestArrivedIndex]) {
        currentTruckLocation = segments[latestArrivedIndex].location;
      }
    } else {
      currentTruckLocation = null; // Không hiện xe tải nếu chưa chạy hoặc đã xong
    }

    return { segments, warehouseLocation, currentTruckLocation, allPoints };
  }, [route]);

  if (!mapData || !mapData.warehouseLocation) {
    return (
      <div className='flex h-[500px] w-full items-center justify-center rounded-xl border border-dashed border-slate-300 bg-slate-50 text-sm text-slate-500'>
        Không có dữ liệu bản đồ cho lộ trình này
      </div>
    );
  }

  // Tính toán bounds để tự động zoom vừa vặn tất cả các điểm
  const bounds = L.latLngBounds(mapData.allPoints);

  return (
    <div className='relative z-0 h-[500px] w-full overflow-hidden rounded-xl border border-slate-200'>
      <MapContainer bounds={bounds} className='h-full w-full'>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url='https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
        />

        {/* Marker Kho (Warehouse) */}
        <Marker position={mapData.warehouseLocation} icon={ICONS.WAREHOUSE}>
          <Popup>
            <div className='p-1'>
              <p className='font-semibold text-sm'>Kho xuất phát</p>
              <p className='text-xs text-muted-foreground'>
                Điểm bắt đầu lộ trình
              </p>
            </div>
          </Popup>
        </Marker>

        {/* Vẽ Polylines và Markers cho từng điểm giao */}
        {mapData.segments.map((segment) => {
          const stopStatusConfig =
            ROUTE_STOP_CONFIG[
              segment.stop.status as keyof typeof ROUTE_STOP_CONFIG
            ] || ROUTE_STOP_CONFIG.WAITING;
          const isCompletingStop =
            routeStopAction?.id === segment.stop.id &&
            routeStopAction.type === 'complete';
          const isAbortingStop =
            routeStopAction?.id === segment.stop.id &&
            routeStopAction.type === 'abort';
          const isSubmittingAnotherStop =
            Boolean(routeStopAction) && routeStopAction?.id !== segment.stop.id;
          const canHandleStop =
            canShowStopActions &&
            segment.stop.status === 'WAITING' &&
            segment.isNextStop &&
            !isSubmittingAnotherStop;

          return (
            <Fragment key={segment.stop.id}>
              <Polyline
                positions={segment.path}
                color={
                  segment.stop.status === 'ARRIVED'
                    ? '#10b981'
                    : segment.stop.status === 'FAILED'
                      ? '#f43f5e'
                      : '#94a3b8'
                }
                weight={segment.isNextStop ? 5 : 4}
                opacity={0.85}
                dashArray={
                  segment.stop.status === 'WAITING' ? '6, 10' : undefined
                }
              />

              <Marker
                position={segment.location}
                icon={
                  ICONS[segment.stop.status as keyof typeof ICONS] ||
                  ICONS.WAITING
                }
              >
                <LeafletTooltip
                  direction='top'
                  offset={[0, -20]}
                  opacity={1}
                  sticky
                  interactive
                  className='route-stop-tooltip !rounded-2xl !border-none !bg-transparent !p-0 !shadow-none'
                >
                  <div className='w-[280px] rounded-2xl border border-slate-200 bg-white/95 p-3 shadow-2xl backdrop-blur'>
                    <div className='mb-2 flex items-center justify-between gap-2'>
                      <p className='text-[11px] font-semibold uppercase tracking-[0.14em] text-sky-700'>
                        Điểm giao #{segment.stop.sequence}
                      </p>
                      <span
                        className={cn(
                          'rounded-full border px-2 py-0.5 text-[11px] font-medium',
                          stopStatusConfig.badgeClass
                        )}
                      >
                        {stopStatusConfig.label}
                      </span>
                    </div>

                    <p className='text-sm font-semibold text-slate-900'>
                      {segment.stop.deliverySlip?.customerName ||
                        'Khách hàng chưa xác định'}
                    </p>

                    <p className='mt-1 line-clamp-2 text-xs leading-5 text-slate-600'>
                      {segment.stop.deliverySlip?.customerAddress
                        ?.fullAddress || 'Chưa có địa chỉ giao hàng'}
                    </p>

                    <div className='mt-2 space-y-1 text-xs text-slate-600'>
                      <p>
                        Mã phiếu:{' '}
                        <span className='font-medium text-slate-900'>
                          {segment.stop.deliverySlip?.code || '--'}
                        </span>
                      </p>
                      <p>
                        Khối lượng:{' '}
                        <span className='font-medium text-slate-900'>
                          {formatNumber(
                            segment.stop.deliverySlip?.totalWeightKg,
                            'kg'
                          )}
                        </span>
                      </p>
                      <p>
                        Thể tích:{' '}
                        <span className='font-medium text-slate-900'>
                          {formatNumber(
                            segment.stop.deliverySlip?.totalVolumeCbm,
                            'cbm'
                          )}
                        </span>
                      </p>
                      <p className='flex items-center gap-1'>
                        <Phone className='h-3.5 w-3.5 text-slate-400' />
                        <span className='font-medium text-slate-700'>
                          {segment.stop.deliverySlip?.customerPhone ||
                            'Chưa có số điện thoại'}
                        </span>
                      </p>
                    </div>

                    {canShowStopActions && (
                      <div className='mt-3 space-y-2 border-t border-slate-100 pt-3'>
                        {segment.stop.status === 'WAITING' &&
                          !segment.isNextStop && (
                            <p className='rounded-md bg-amber-50 px-2 py-1 text-[11px] text-amber-700'>
                              Điểm này chưa phải điểm giao kế tiếp.
                            </p>
                          )}

                        <div className='grid grid-cols-2 gap-2'>
                          <Button
                            size='sm'
                            className='h-8 bg-emerald-600 text-white hover:bg-emerald-700'
                            disabled={
                              !canHandleStop ||
                              isCompletingStop ||
                              isAbortingStop
                            }
                            onMouseDown={(event) => event.stopPropagation()}
                            onClick={(event) => {
                              event.preventDefault();
                              event.stopPropagation();
                              void onCompleteRouteStop?.(segment.stop.id);
                            }}
                          >
                            <CheckCircle2 className='h-3.5 w-3.5' />
                            {isCompletingStop ? 'Đang xử lý' : 'Đã giao'}
                          </Button>

                          <Button
                            size='sm'
                            variant='outline'
                            className='h-8 border-rose-300 text-rose-700 hover:bg-rose-50'
                            disabled={
                              !canHandleStop ||
                              isCompletingStop ||
                              isAbortingStop
                            }
                            onMouseDown={(event) => event.stopPropagation()}
                            onClick={(event) => {
                              event.preventDefault();
                              event.stopPropagation();
                              void onAbortRouteStop?.(segment.stop.id);
                            }}
                          >
                            <SkipForward className='h-3.5 w-3.5' />
                            {isAbortingStop ? 'Đang xử lý' : 'Bỏ qua'}
                          </Button>
                        </div>
                      </div>
                    )}
                  </div>
                </LeafletTooltip>
              </Marker>
            </Fragment>
          );
        })}

        {/* Marker Xe tải (Nếu lộ trình đang chạy) */}
        {mapData.currentTruckLocation && (
          <Marker
            position={mapData.currentTruckLocation}
            icon={ICONS.TRUCK}
            zIndexOffset={1000}
          >
            <Popup>
              <p className='font-semibold text-sm'>Vị trí hiện tại</p>
            </Popup>
          </Marker>
        )}
      </MapContainer>
    </div>
  );
}
