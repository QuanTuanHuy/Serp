/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order tracking map
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletLayerGroup = import('leaflet').LayerGroup;

export interface TrackingMapPoint {
  id: string;
  label: string;
  description?: string;
  latitude: number;
  longitude: number;
  kind: 'sender' | 'receiver' | 'facility' | 'movement' | 'exception';
}

interface TrackingMapProps {
  points: TrackingMapPoint[];
  className?: string;
}

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};

const DEFAULT_ZOOM = 6;

const markerStyles: Record<
  TrackingMapPoint['kind'],
  { color: string; fillColor: string; radius: number }
> = {
  sender: { color: '#1d4ed8', fillColor: '#2563eb', radius: 7 },
  receiver: { color: '#b91c1c', fillColor: '#dc2626', radius: 7 },
  facility: { color: '#047857', fillColor: '#059669', radius: 8 },
  movement: { color: '#c2410c', fillColor: '#ea580c', radius: 8 },
  exception: { color: '#991b1b', fillColor: '#dc2626', radius: 9 },
};

const isValidCoordinate = (latitude: number, longitude: number): boolean => {
  return (
    Number.isFinite(latitude) &&
    Number.isFinite(longitude) &&
    latitude >= -90 &&
    latitude <= 90 &&
    longitude >= -180 &&
    longitude <= 180
  );
};

export const TrackingMap: React.FC<TrackingMapProps> = ({
  points,
  className,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const leafletRef = React.useRef<LeafletModule | null>(null);
  const mapRef = React.useRef<LeafletMap | null>(null);
  const layerGroupRef = React.useRef<LeafletLayerGroup | null>(null);

  const validPoints = React.useMemo(
    () =>
      points.filter((point) =>
        isValidCoordinate(point.latitude, point.longitude)
      ),
    [points]
  );

  const syncLayers = React.useCallback(() => {
    const leaflet = leafletRef.current;
    const map = mapRef.current;

    if (!leaflet || !map) {
      return;
    }

    if (layerGroupRef.current) {
      layerGroupRef.current.remove();
    }

    const layerGroup = leaflet.layerGroup().addTo(map);
    layerGroupRef.current = layerGroup;

    if (validPoints.length === 0) {
      map.setView(
        [DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude],
        DEFAULT_ZOOM
      );
      return;
    }

    validPoints.forEach((point) => {
      const style = markerStyles[point.kind];
      const marker = leaflet.circleMarker([point.latitude, point.longitude], {
        radius: style.radius,
        color: style.color,
        fillColor: style.fillColor,
        fillOpacity: 0.9,
        weight: 2,
      });

      const tooltipContent = document.createElement('div');
      const titleElement = document.createElement('strong');
      titleElement.textContent = point.label;
      tooltipContent.appendChild(titleElement);

      if (point.description) {
        tooltipContent.appendChild(document.createElement('br'));
        tooltipContent.appendChild(document.createTextNode(point.description));
      }

      marker.bindTooltip(tooltipContent, { direction: 'top', sticky: true });
      marker.addTo(layerGroup);
    });

    if (validPoints.length > 1) {
      leaflet
        .polyline(
          validPoints.map((point) => [point.latitude, point.longitude]),
          {
            color: '#334155',
            dashArray: '8 8',
            opacity: 0.8,
            weight: 3,
          }
        )
        .addTo(layerGroup);
    }

    const bounds = leaflet.latLngBounds(
      validPoints.map((point) => [point.latitude, point.longitude])
    );
    map.fitBounds(bounds.pad(0.2));
  }, [validPoints]);

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

      syncLayers();

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
  }, [syncLayers]);

  React.useEffect(() => {
    syncLayers();
  }, [syncLayers]);

  return (
    <div
      ref={containerRef}
      className={cn('h-[420px] w-full rounded-md border', className)}
    />
  );
};
