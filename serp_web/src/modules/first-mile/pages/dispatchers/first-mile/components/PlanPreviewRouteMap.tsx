/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Dispatcher plan route map
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import type {
  PickupOptimizationRoute,
  PickupOptimizationStop,
} from '../../../../types';

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletLayerGroup = import('leaflet').LayerGroup;

interface PlanPreviewRouteMapProps {
  routes: PickupOptimizationRoute[];
  className?: string;
}

interface RoutePoint {
  routeIndex: number;
  routeLabel: string;
  color: string;
  stop: PickupOptimizationStop;
  point: [number, number];
}

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};

const DEFAULT_ZOOM = 6;

const ROUTE_COLORS = [
  '#2563eb',
  '#dc2626',
  '#16a34a',
  '#d97706',
  '#7c3aed',
  '#0891b2',
  '#be123c',
  '#4f46e5',
];

const hasCoordinate = (
  stop: PickupOptimizationStop
): stop is PickupOptimizationStop & {
  latitude: number;
  longitude: number;
} => {
  return (
    typeof stop.latitude === 'number' &&
    Number.isFinite(stop.latitude) &&
    typeof stop.longitude === 'number' &&
    Number.isFinite(stop.longitude)
  );
};

const escapeHtml = (value: string): string => {
  return value.replace(/[&<>"']/g, (char) => {
    const entities: Record<string, string> = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;',
    };
    return entities[char] ?? char;
  });
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

const getRouteLabel = (
  route: PickupOptimizationRoute,
  routeIndex: number
): string => {
  const courierText =
    route.courierName ||
    route.courierCode ||
    (route.courierStaffId ? `Nhân viên #${route.courierStaffId}` : '');

  return courierText
    ? `Tuyến ${routeIndex + 1} - ${courierText}`
    : `Tuyến ${routeIndex + 1}`;
};

const getStopLabel = (stop: PickupOptimizationStop): string => {
  return stop.orderCode || stop.customerOrderCode || `Đơn #${stop.orderId}`;
};

const buildRoutePoints = (routes: PickupOptimizationRoute[]): RoutePoint[] => {
  return routes.flatMap((route, routeIndex) => {
    const color = ROUTE_COLORS[routeIndex % ROUTE_COLORS.length];
    const routeLabel = getRouteLabel(route, routeIndex);

    return (route.stops ?? [])
      .filter(hasCoordinate)
      .sort((left, right) => left.sequence - right.sequence)
      .map((stop) => ({
        routeIndex,
        routeLabel,
        color,
        stop,
        point: [stop.latitude, stop.longitude] as [number, number],
      }));
  });
};

const buildPopupHtml = (routePoint: RoutePoint): string => {
  const { routeLabel, stop } = routePoint;
  const senderText = stop.senderName || '--';

  return [
    '<div style="min-width: 190px; font-size: 12px; line-height: 1.45;">',
    `<div style="font-weight: 600; margin-bottom: 4px;">#${stop.sequence} ${escapeHtml(getStopLabel(stop))}</div>`,
    `<div>${escapeHtml(routeLabel)}</div>`,
    `<div>Người gửi: ${escapeHtml(senderText)}</div>`,
    `<div>Đến: ${escapeHtml(formatStopTime(stop.arrivalTime))}</div>`,
    `<div>Rời: ${escapeHtml(formatStopTime(stop.departureTime))}</div>`,
    '</div>',
  ].join('');
};

const buildMarkerHtml = (routePoint: RoutePoint): string => {
  return [
    'display:flex;',
    'align-items:center;',
    'justify-content:center;',
    'width:28px;',
    'height:28px;',
    'border-radius:9999px;',
    'border:2px solid #ffffff;',
    `background:${routePoint.color};`,
    'color:#ffffff;',
    'font-size:12px;',
    'font-weight:700;',
    'box-shadow:0 8px 18px rgba(15,23,42,0.28);',
  ].join('');
};

export const PlanPreviewRouteMap: React.FC<PlanPreviewRouteMapProps> = ({
  routes,
  className,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const leafletRef = React.useRef<LeafletModule | null>(null);
  const mapRef = React.useRef<LeafletMap | null>(null);
  const layerGroupRef = React.useRef<LeafletLayerGroup | null>(null);
  const routePoints = React.useMemo(() => buildRoutePoints(routes), [routes]);

  const syncRouteLayers = React.useCallback(() => {
    const leaflet = leafletRef.current;
    const map = mapRef.current;
    const layerGroup = layerGroupRef.current;

    if (!leaflet || !map || !layerGroup) {
      return;
    }

    layerGroup.clearLayers();

    if (routePoints.length === 0) {
      map.setView(
        [DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude],
        DEFAULT_ZOOM
      );
      return;
    }

    const boundsPoints: [number, number][] = [];
    const pointsByRoute = new Map<number, RoutePoint[]>();

    for (const routePoint of routePoints) {
      boundsPoints.push(routePoint.point);
      const currentPoints = pointsByRoute.get(routePoint.routeIndex) ?? [];
      currentPoints.push(routePoint);
      pointsByRoute.set(routePoint.routeIndex, currentPoints);
    }

    for (const points of pointsByRoute.values()) {
      if (points.length > 1) {
        leaflet
          .polyline(
            points.map((routePoint) => routePoint.point),
            {
              color: points[0]?.color ?? '#2563eb',
              weight: 4,
              opacity: 0.86,
            }
          )
          .addTo(layerGroup);
      }
    }

    for (const routePoint of routePoints) {
      const marker = leaflet.marker(routePoint.point, {
        icon: leaflet.divIcon({
          className: 'pickup-plan-stop-marker',
          html: `<span style="${buildMarkerHtml(routePoint)}">${routePoint.stop.sequence}</span>`,
          iconSize: [28, 28],
          iconAnchor: [14, 14],
        }),
      });

      marker.bindPopup(buildPopupHtml(routePoint));
      marker.addTo(layerGroup);
    }

    if (boundsPoints.length === 1) {
      map.setView(boundsPoints[0], 14);
      return;
    }

    map.fitBounds(leaflet.latLngBounds(boundsPoints).pad(0.25));
  }, [routePoints]);

  React.useEffect(() => {
    let isCancelled = false;

    const initMap = async () => {
      if (!containerRef.current || mapRef.current) {
        return;
      }

      const leaflet = await import('leaflet');

      if (isCancelled || !containerRef.current) {
        return;
      }

      leafletRef.current = leaflet;

      const map = leaflet.map(containerRef.current, {
        zoomControl: true,
      });
      mapRef.current = map;

      leaflet
        .tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '&copy; OpenStreetMap contributors',
        })
        .addTo(map);

      layerGroupRef.current = leaflet.layerGroup().addTo(map);
      map.setView(
        [DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude],
        DEFAULT_ZOOM
      );

      syncRouteLayers();

      window.setTimeout(() => {
        map.invalidateSize();
      }, 0);
    };

    void initMap();

    return () => {
      isCancelled = true;

      if (mapRef.current) {
        mapRef.current.remove();
      }

      mapRef.current = null;
      layerGroupRef.current = null;
      leafletRef.current = null;
    };
  }, [syncRouteLayers]);

  React.useEffect(() => {
    syncRouteLayers();
  }, [syncRouteLayers]);

  return (
    <div className='space-y-2'>
      <div
        ref={containerRef}
        className={cn(
          'relative isolate z-0 h-[420px] w-full rounded-md border',
          className
        )}
      />
      {routePoints.length === 0 ? (
        <p className='text-xs text-muted-foreground'>
          Bản xem trước chưa có tọa độ điểm lấy hàng để hiển thị hành trình trên
          bản đồ.
        </p>
      ) : (
        <div className='flex flex-wrap gap-3 text-xs text-muted-foreground'>
          {routes.map((route, index) => {
            const routePointCount = routePoints.filter(
              (routePoint) => routePoint.routeIndex === index
            ).length;

            if (routePointCount === 0) {
              return null;
            }

            return (
              <span key={`${route.courierStaffId ?? 'route'}-${index}`}>
                <span
                  className='mr-1 inline-block h-2.5 w-2.5 rounded-full'
                  style={{
                    backgroundColor: ROUTE_COLORS[index % ROUTE_COLORS.length],
                  }}
                />
                {getRouteLabel(route, index)} ({routePointCount} điểm)
              </span>
            );
          })}
        </div>
      )}
    </div>
  );
};
