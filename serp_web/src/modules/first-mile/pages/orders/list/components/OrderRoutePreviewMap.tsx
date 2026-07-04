/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - Order route preview map
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletCircleMarker = import('leaflet').CircleMarker;
type LeafletPolyline = import('leaflet').Polyline;

interface OrderRoutePreviewMapProps {
  senderLatitude: number;
  senderLongitude: number;
  receiverLatitude: number;
  receiverLongitude: number;
  className?: string;
}

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};

const DEFAULT_ZOOM = 6;

export const OrderRoutePreviewMap: React.FC<OrderRoutePreviewMapProps> = ({
  senderLatitude,
  senderLongitude,
  receiverLatitude,
  receiverLongitude,
  className,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const leafletRef = React.useRef<LeafletModule | null>(null);
  const mapRef = React.useRef<LeafletMap | null>(null);
  const senderMarkerRef = React.useRef<LeafletCircleMarker | null>(null);
  const receiverMarkerRef = React.useRef<LeafletCircleMarker | null>(null);
  const routeLineRef = React.useRef<LeafletPolyline | null>(null);

  const syncRouteLayers = React.useCallback(
    (senderPoint: [number, number], receiverPoint: [number, number]) => {
      const leaflet = leafletRef.current;
      const map = mapRef.current;

      if (!leaflet || !map) {
        return;
      }

      if (!senderMarkerRef.current) {
        senderMarkerRef.current = leaflet
          .circleMarker(senderPoint, {
            radius: 7,
            color: '#1d4ed8',
            fillColor: '#2563eb',
            fillOpacity: 0.9,
            weight: 2,
          })
          .addTo(map);
      } else {
        senderMarkerRef.current.setLatLng(senderPoint);
      }

      if (!receiverMarkerRef.current) {
        receiverMarkerRef.current = leaflet
          .circleMarker(receiverPoint, {
            radius: 7,
            color: '#b91c1c',
            fillColor: '#dc2626',
            fillOpacity: 0.9,
            weight: 2,
          })
          .addTo(map);
      } else {
        receiverMarkerRef.current.setLatLng(receiverPoint);
      }

      if (!routeLineRef.current) {
        routeLineRef.current = leaflet
          .polyline([senderPoint, receiverPoint], {
            color: '#334155',
            weight: 3,
            opacity: 0.9,
          })
          .addTo(map);
      } else {
        routeLineRef.current.setLatLngs([senderPoint, receiverPoint]);
      }

      const bounds = leaflet.latLngBounds([senderPoint, receiverPoint]);
      map.fitBounds(bounds.pad(0.25));
    },
    []
  );

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

      syncRouteLayers(
        [senderLatitude, senderLongitude],
        [receiverLatitude, receiverLongitude]
      );

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
      senderMarkerRef.current = null;
      receiverMarkerRef.current = null;
      routeLineRef.current = null;
      leafletRef.current = null;
    };
  }, [
    receiverLatitude,
    receiverLongitude,
    senderLatitude,
    senderLongitude,
    syncRouteLayers,
  ]);

  React.useEffect(() => {
    if (!mapRef.current) {
      return;
    }

    syncRouteLayers(
      [senderLatitude, senderLongitude],
      [receiverLatitude, receiverLongitude]
    );
  }, [
    receiverLatitude,
    receiverLongitude,
    senderLatitude,
    senderLongitude,
    syncRouteLayers,
  ]);

  return (
    <div
      ref={containerRef}
      className={cn('h-64 w-full rounded-md border', className)}
    />
  );
};
