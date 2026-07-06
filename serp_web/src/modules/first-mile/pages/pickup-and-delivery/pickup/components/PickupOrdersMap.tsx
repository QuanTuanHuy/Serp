/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Pickup assigned orders map
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';
import type { PickupTrackingOrder } from '../../../../types';

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletLayerGroup = import('leaflet').LayerGroup;

interface PickupOrdersMapProps {
  orders: PickupTrackingOrder[];
  selectedOrderId?: number;
  onSelectOrder?: (orderId: number) => void;
  className?: string;
}

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};

const DEFAULT_ZOOM = 6;

const ORDER_STATUS_LABELS: Record<string, string> = {
  ASSIGNED_TO_PICKUP: 'Đã phân công lấy',
  PICKING_UP: 'Đang lấy hàng',
  PICKED_UP: 'Đã lấy hàng',
  PENDING_ORIGIN_POST_OFFICE_INBOUND: 'Chờ nhập bưu cục gốc',
};

const formatOrderStatus = (status?: string): string =>
  status ? (ORDER_STATUS_LABELS[status] ?? status) : '--';

const buildPopupHtml = (order: PickupTrackingOrder): string => {
  const orderCode = order.orderCode || `#${order.orderId}`;
  const courierText = order.courierName || order.courierCode || '--';
  const statusText = formatOrderStatus(order.orderStatus);
  const checkinText = order.checkedIn ? 'Đã check-in' : 'Chưa check-in';

  return [
    `<div style="min-width: 180px;">`,
    `<div style="font-weight: 600; margin-bottom: 4px;">${orderCode}</div>`,
    `<div style="font-size: 12px;">Nhân viên: ${courierText}</div>`,
    `<div style="font-size: 12px;">Trạng thái: ${statusText}</div>`,
    `<div style="font-size: 12px;">${checkinText}</div>`,
    `</div>`,
  ].join('');
};

const resolveMarkerStyle = (
  order: PickupTrackingOrder,
  isSelected: boolean
): {
  radius: number;
  color: string;
  fillColor: string;
  fillOpacity: number;
  weight: number;
} => {
  if (isSelected) {
    return {
      radius: 9,
      color: '#0f172a',
      fillColor: '#0ea5e9',
      fillOpacity: 0.95,
      weight: 3,
    };
  }

  if (order.checkedIn) {
    return {
      radius: 7,
      color: '#166534',
      fillColor: '#22c55e',
      fillOpacity: 0.9,
      weight: 2,
    };
  }

  return {
    radius: 7,
    color: '#1d4ed8',
    fillColor: '#3b82f6',
    fillOpacity: 0.85,
    weight: 2,
  };
};

export const PickupOrdersMap: React.FC<PickupOrdersMapProps> = ({
  orders,
  selectedOrderId,
  onSelectOrder,
  className,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const leafletRef = React.useRef<LeafletModule | null>(null);
  const mapRef = React.useRef<LeafletMap | null>(null);
  const markerLayerRef = React.useRef<LeafletLayerGroup | null>(null);

  const syncMarkers = React.useCallback(() => {
    const leaflet = leafletRef.current;
    const map = mapRef.current;

    if (!leaflet || !map) {
      return;
    }

    if (markerLayerRef.current) {
      markerLayerRef.current.removeFrom(map);
    }

    const markerLayer = leaflet.layerGroup();
    markerLayerRef.current = markerLayer;

    const points: [number, number][] = [];

    for (const order of orders) {
      if (
        order.senderLatitude === undefined ||
        order.senderLongitude === undefined
      ) {
        continue;
      }

      const point: [number, number] = [
        order.senderLatitude,
        order.senderLongitude,
      ];
      points.push(point);

      const marker = leaflet.circleMarker(
        point,
        resolveMarkerStyle(order, selectedOrderId === order.orderId)
      );

      marker.bindPopup(buildPopupHtml(order));
      marker.on('click', () => onSelectOrder?.(order.orderId));
      marker.addTo(markerLayer);
    }

    markerLayer.addTo(map);

    if (points.length === 0) {
      map.setView(
        [DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude],
        DEFAULT_ZOOM
      );
      return;
    }

    if (points.length === 1) {
      map.setView(points[0], 14);
      return;
    }

    const bounds = leaflet.latLngBounds(points);
    map.fitBounds(bounds.pad(0.25));
  }, [onSelectOrder, orders, selectedOrderId]);

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

      map.setView(
        [DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude],
        DEFAULT_ZOOM
      );

      syncMarkers();

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
      markerLayerRef.current = null;
      leafletRef.current = null;
    };
  }, [syncMarkers]);

  React.useEffect(() => {
    if (!mapRef.current) {
      return;
    }

    syncMarkers();
  }, [syncMarkers]);

  return (
    <div
      ref={containerRef}
      className={cn(
        'relative isolate z-0 h-[420px] w-full rounded-md border',
        className
      )}
    />
  );
};
