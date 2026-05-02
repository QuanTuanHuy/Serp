'use client';

import { useMemo, useState } from 'react';
import {
  MapContainer,
  Marker,
  Polyline,
  Popup,
  TileLayer,
} from 'react-leaflet';
import L from 'leaflet';
import {
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
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

type RouteMapGroupedSegment = {
  key: string;
  location: [number, number];
  group: RouteMapSegment[];
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

const getStopStatusConfig = (status: RouteStop['status']) =>
  ROUTE_STOP_CONFIG[status as keyof typeof ROUTE_STOP_CONFIG] ||
  ROUTE_STOP_CONFIG.WAITING;

const getStopIcon = (status: RouteStop['status']) =>
  ICONS[status as keyof typeof ICONS] || ICONS.WAITING;

const getPolylineColor = (segment: RouteMapSegment) => {
  if (segment.isNextStop) {
    return '#3b82f6';
  }

  if (segment.stop.status === 'ARRIVED') {
    return '#10b981';
  }

  if (segment.stop.status === 'FAILED') {
    return '#f43f5e';
  }

  return '#94a3b8';
};

const RouteStopMarkerGroup = ({
  group,
  canShowStopActions,
  routeStopAction,
  onCompleteRouteStop,
  onAbortRouteStop,
}: {
  group: RouteMapSegment[];
  canShowStopActions: boolean;
  routeStopAction: RouteStopActionState | null;
  onCompleteRouteStop?: (id: string) => Promise<void> | void;
  onAbortRouteStop?: (id: string) => Promise<void> | void;
}) => {
  const [activeIndex, setActiveIndex] = useState(0);
  const isMultiple = group.length > 1;
  const segment = group[Math.min(activeIndex, group.length - 1)] ?? group[0];
  const stopStatusConfig = getStopStatusConfig(segment.stop.status);

  const isCompletingStop =
    routeStopAction?.id === segment.stop.id &&
    routeStopAction.type === 'complete';
  const isAbortingStop =
    routeStopAction?.id === segment.stop.id && routeStopAction.type === 'abort';
  const isSubmittingAnotherStop =
    Boolean(routeStopAction) && routeStopAction?.id !== segment.stop.id;

  const canHandleStop =
    canShowStopActions &&
    segment.stop.status === 'WAITING' &&
    segment.isNextStop &&
    !isSubmittingAnotherStop;

  const stopZIndexOffset = segment.isNextStop
    ? 1000
    : segment.stop.status === 'WAITING'
      ? 500
      : 100;

  return (
    <Marker
      position={segment.location}
      icon={getStopIcon(segment.stop.status)}
      zIndexOffset={stopZIndexOffset}
    >
      <Popup
        closeButton={false}
        autoClose={false}
        closeOnClick={false}
        className='route-stop-tooltip'
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
            {segment.stop.deliverySlip?.customerAddress?.fullAddress ||
              'Chưa có địa chỉ giao hàng'}
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
                {formatNumber(segment.stop.deliverySlip?.totalWeightKg, 'kg')}
              </span>
            </p>
            <p>
              Thể tích:{' '}
              <span className='font-medium text-slate-900'>
                {formatNumber(segment.stop.deliverySlip?.totalVolumeCbm, 'cbm')}
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

          {isMultiple && (
            <div className='mt-3 flex items-center justify-between rounded-lg p-1.5'>
              <Button
                size='sm'
                variant='outline'
                className='flex h-7 w-7 items-center justify-center rounded-full p-0 border-blue-400 text-blue-600 transition-all hover:border-blue-600 hover:bg-blue-50 hover:text-blue-700 hover:shadow-sm disabled:border-slate-200 disabled:bg-transparent disabled:text-slate-300 disabled:shadow-none'
                disabled={activeIndex === 0}
                onMouseDown={(event) => event.stopPropagation()}
                onClick={(event) => {
                  event.preventDefault();
                  event.stopPropagation();
                  setActiveIndex((prev) => Math.max(0, prev - 1));
                }}
              >
                <ChevronLeft className='h-4 w-4' />
              </Button>

              <span className='text-[11px] font-medium text-slate-600'>
                Điểm {activeIndex + 1} / {group.length}
              </span>

              <Button
                size='sm'
                variant='outline'
                className='flex h-7 w-7 items-center justify-center rounded-full p-0 border-blue-400 text-blue-600 transition-all hover:border-blue-600 hover:bg-blue-50 hover:text-blue-700 hover:shadow-sm disabled:border-slate-200 disabled:bg-transparent disabled:text-slate-300 disabled:shadow-none'
                disabled={activeIndex === group.length - 1}
                onMouseDown={(event) => event.stopPropagation()}
                onClick={(event) => {
                  event.preventDefault();
                  event.stopPropagation();
                  setActiveIndex((prev) =>
                    Math.min(group.length - 1, prev + 1)
                  );
                }}
              >
                <ChevronRight className='h-4 w-4' />
              </Button>
            </div>
          )}

          {canShowStopActions && (
            <div className='mt-3 space-y-2 border-t border-slate-100 pt-3'>
              {segment.stop.status === 'WAITING' && !segment.isNextStop && (
                <p className='rounded-md bg-amber-50 px-2 py-1 text-[11px] text-amber-700'>
                  Điểm này chưa phải điểm giao kế tiếp.
                </p>
              )}

              <div className='grid grid-cols-2 gap-2'>
                <Button
                  size='sm'
                  className='h-8 bg-emerald-600 text-white hover:bg-emerald-700'
                  disabled={
                    !canHandleStop || isCompletingStop || isAbortingStop
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
                    !canHandleStop || isCompletingStop || isAbortingStop
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
      </Popup>
    </Marker>
  );
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
    const groupedSegmentsMap = new Map<string, RouteMapSegment[]>();

    stops.forEach((stop) => {
      const path = stop.encodedPolyline
        ? decodePolyline(stop.encodedPolyline)
        : [];

      if (path.length === 0) {
        return;
      }

      allPoints = [...allPoints, ...path];

      if (!warehouseLocation) {
        warehouseLocation = path[0];
      }

      const location = path[path.length - 1];
      const isPassed = stop.status === 'ARRIVED';

      const segment: RouteMapSegment = {
        stop,
        path,
        location,
        isPassed,
        isNextStop: stop.id === nextWaitingStopId,
      };

      segments.push(segment);

      const locationKey = `${location[0]},${location[1]}`;
      if (!groupedSegmentsMap.has(locationKey)) {
        groupedSegmentsMap.set(locationKey, []);
      }
      groupedSegmentsMap.get(locationKey)!.push(segment);

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

    let currentTruckLocation: [number, number] | null = warehouseLocation;
    if (route.status === 'IN_PROGRESS') {
      if (latestArrivedIndex >= 0 && segments[latestArrivedIndex]) {
        currentTruckLocation = segments[latestArrivedIndex].location;
      }
    } else {
      currentTruckLocation = null;
    }

    const groupedSegments: RouteMapGroupedSegment[] = Array.from(
      groupedSegmentsMap.entries()
    ).map(([key, group]) => ({
      key,
      location: group[0].location,
      group,
    }));

    return {
      segments,
      groupedSegments,
      warehouseLocation,
      currentTruckLocation,
      allPoints,
    };
  }, [route]);

  if (!mapData || !mapData.warehouseLocation) {
    return (
      <div className='flex h-[500px] w-full items-center justify-center rounded-xl border border-dashed border-slate-300 bg-slate-50 text-sm text-slate-500'>
        Không có dữ liệu bản đồ cho lộ trình này
      </div>
    );
  }

  const bounds = L.latLngBounds(mapData.allPoints);

  return (
    <div className='relative z-0 h-[500px] w-full overflow-hidden rounded-xl border border-slate-200'>
      <MapContainer bounds={bounds} className='h-full w-full'>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
          url='https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png'
        />

        <Marker position={mapData.warehouseLocation} icon={ICONS.WAREHOUSE}>
          <Popup>
            <div className='p-1'>
              <p className='text-[11px] font-semibold uppercase tracking-[0.14em] text-sky-700'>
                Kho xuất phát
              </p>
              <p className='text-sm font-semibold text-slate-900'>
                Điểm bắt đầu lộ trình
              </p>
            </div>
          </Popup>
        </Marker>

        {mapData.segments.map((segment) => (
          <Polyline
            key={segment.stop.id}
            positions={segment.path}
            color={getPolylineColor(segment)}
            weight={segment.isNextStop ? 6 : 4}
            opacity={0.85}
            dashArray={segment.stop.status === 'WAITING' ? '6, 10' : undefined}
          />
        ))}

        {mapData.groupedSegments.map((groupedSegment) => (
          <RouteStopMarkerGroup
            key={groupedSegment.key}
            group={groupedSegment.group}
            canShowStopActions={canShowStopActions}
            routeStopAction={routeStopAction}
            onCompleteRouteStop={onCompleteRouteStop}
            onAbortRouteStop={onAbortRouteStop}
          />
        ))}

        {mapData.currentTruckLocation && (
          <Marker
            position={mapData.currentTruckLocation}
            icon={ICONS.TRUCK}
            zIndexOffset={900}
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
