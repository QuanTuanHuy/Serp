/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS entity location map
 */

'use client';

import React from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';

export interface TmsMapBounds {
  minLatitude: number;
  maxLatitude: number;
  minLongitude: number;
  maxLongitude: number;
}

export interface TmsLocationMapPoint {
  id: number;
  code: string;
  name: string;
  latitude: number;
  longitude: number;
  address?: string;
  status?: string;
}

interface TmsEntityLocationMapProps {
  title: string;
  description: string;
  points: TmsLocationMapPoint[];
  markerColor: string;
  markerFillColor: string;
  loading?: boolean;
  totalItems?: number;
  emptyText: string;
  className?: string;
  onBoundsChange: (bounds: TmsMapBounds) => void;
  onPointClick?: (point: TmsLocationMapPoint) => void;
}

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};
const DEFAULT_ZOOM = 6;

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletLayerGroup = import('leaflet').LayerGroup;

function escapeHtml(value: string): string {
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
}

function formatCoordinate(value: number): string {
  return Number(value.toFixed(6)).toString();
}

function buildPopupRow(label: string, value: string): string {
  return `<div style="display:grid;grid-template-columns:72px minmax(0,1fr);gap:8px;align-items:start"><span style="color:#64748b">${label}</span><span style="color:#0f172a;word-break:break-word">${escapeHtml(value)}</span></div>`;
}

function buildPopup(point: TmsLocationMapPoint): string {
  const coordinates = `${formatCoordinate(point.latitude)}, ${formatCoordinate(
    point.longitude
  )}`;
  const rows = [
    buildPopupRow('Code', point.code),
    buildPopupRow('Name', point.name),
    buildPopupRow('Address', point.address || '--'),
    buildPopupRow('Coordinates', coordinates),
  ];

  if (point.status) {
    rows.push(buildPopupRow('Status', point.status));
  }

  return `<div style="min-width:240px;max-width:320px;font-size:12px;line-height:1.45"><strong style="display:block;margin-bottom:8px;font-size:13px;color:#0f172a">${escapeHtml(point.name)}</strong><div style="display:grid;gap:5px">${rows.join('')}</div></div>`;
}

function readMapBounds(map: LeafletMap): TmsMapBounds {
  const bounds = map.getBounds();
  const southWest = bounds.getSouthWest();
  const northEast = bounds.getNorthEast();

  return {
    minLatitude: Number(southWest.lat.toFixed(6)),
    maxLatitude: Number(northEast.lat.toFixed(6)),
    minLongitude: Number(southWest.lng.toFixed(6)),
    maxLongitude: Number(northEast.lng.toFixed(6)),
  };
}

export const TmsEntityLocationMap: React.FC<TmsEntityLocationMapProps> = ({
  title,
  description,
  points,
  markerColor,
  markerFillColor,
  loading = false,
  totalItems,
  emptyText,
  className,
  onBoundsChange,
  onPointClick,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const leafletRef = React.useRef<LeafletModule | null>(null);
  const mapRef = React.useRef<LeafletMap | null>(null);
  const layerGroupRef = React.useRef<LeafletLayerGroup | null>(null);
  const onBoundsChangeRef = React.useRef(onBoundsChange);

  React.useEffect(() => {
    onBoundsChangeRef.current = onBoundsChange;
  }, [onBoundsChange]);

  React.useEffect(() => {
    let isCancelled = false;
    let boundsTimer: number | null = null;

    const emitBounds = () => {
      const map = mapRef.current;
      if (!map) {
        return;
      }

      if (boundsTimer) {
        window.clearTimeout(boundsTimer);
      }

      boundsTimer = window.setTimeout(() => {
        const nextBounds = readMapBounds(map);
        onBoundsChangeRef.current(nextBounds);
      }, 200);
    };

    const initMap = async () => {
      if (!containerRef.current || mapRef.current) {
        return;
      }

      const leaflet = await import('leaflet');
      if (!containerRef.current || isCancelled) {
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
      layerGroupRef.current = leaflet.layerGroup().addTo(map);
      map.on('moveend zoomend', emitBounds);

      window.setTimeout(() => {
        map.invalidateSize();
        emitBounds();
      }, 0);
    };

    void initMap();

    return () => {
      isCancelled = true;

      if (boundsTimer) {
        window.clearTimeout(boundsTimer);
      }

      if (mapRef.current) {
        mapRef.current.remove();
      }

      mapRef.current = null;
      leafletRef.current = null;
      layerGroupRef.current = null;
    };
  }, []);

  React.useEffect(() => {
    const leaflet = leafletRef.current;
    const layerGroup = layerGroupRef.current;
    if (!leaflet || !layerGroup) {
      return;
    }

    layerGroup.clearLayers();

    points.forEach((point) => {
      const marker = leaflet
        .circleMarker([point.latitude, point.longitude], {
          radius: 7,
          color: markerColor,
          weight: 2,
          fillColor: markerFillColor,
          fillOpacity: 0.85,
        })
        .bindPopup(buildPopup(point));

      marker.on('click', () => {
        marker.openPopup();
        onPointClick?.(point);
      });

      marker.addTo(layerGroup);
    });
  }, [markerColor, markerFillColor, onPointClick, points]);

  const markerCountText =
    totalItems !== undefined && totalItems > points.length
      ? `${points.length} of ${totalItems}`
      : String(points.length);

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        <div className='relative'>
          <div
            ref={containerRef}
            className={cn('h-[420px] w-full overflow-hidden rounded-md border')}
          />

          <div className='pointer-events-none absolute left-3 top-3 rounded-md border bg-background/95 px-3 py-2 text-xs text-muted-foreground shadow-sm'>
            {loading
              ? 'Loading current map area...'
              : `${markerCountText} markers in view`}
          </div>

          {!loading && points.length === 0 ? (
            <div className='pointer-events-none absolute inset-x-3 bottom-3 rounded-md border bg-background/95 px-3 py-2 text-sm text-muted-foreground shadow-sm'>
              {emptyText}
            </div>
          ) : null}
        </div>
      </CardContent>
    </Card>
  );
};
