/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Map for pickup vs check-in locations
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletLayerGroup = import('leaflet').LayerGroup;

interface PickupCheckinLocationMapProps {
  pickupLatitude?: number;
  pickupLongitude?: number;
  checkinLatitude?: number;
  checkinLongitude?: number;
  className?: string;
}

const hasValidCoordinate = (latitude?: number, longitude?: number): boolean =>
  latitude !== undefined &&
  longitude !== undefined &&
  Number.isFinite(latitude) &&
  Number.isFinite(longitude);

export const PickupCheckinLocationMap: React.FC<
  PickupCheckinLocationMapProps
> = ({
  pickupLatitude,
  pickupLongitude,
  checkinLatitude,
  checkinLongitude,
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

    if (hasValidCoordinate(pickupLatitude, pickupLongitude)) {
      const pickupPoint: [number, number] = [
        pickupLatitude as number,
        pickupLongitude as number,
      ];
      points.push(pickupPoint);
      leaflet
        .circleMarker(pickupPoint, {
          radius: 8,
          color: '#1d4ed8',
          fillColor: '#3b82f6',
          fillOpacity: 0.9,
          weight: 2,
        })
        .bindPopup('Pickup location')
        .addTo(markerLayer);
    }

    if (hasValidCoordinate(checkinLatitude, checkinLongitude)) {
      const checkinPoint: [number, number] = [
        checkinLatitude as number,
        checkinLongitude as number,
      ];
      points.push(checkinPoint);
      leaflet
        .circleMarker(checkinPoint, {
          radius: 8,
          color: '#166534',
          fillColor: '#22c55e',
          fillOpacity: 0.9,
          weight: 2,
        })
        .bindPopup('Check-in location')
        .addTo(markerLayer);
    }

    markerLayer.addTo(map);

    if (points.length === 0) {
      map.setView([16.047079, 108.20623], 6);
      return;
    }

    if (points.length === 1) {
      map.setView(points[0], 15);
      return;
    }

    map.fitBounds(leaflet.latLngBounds(points).pad(0.35));
  }, [checkinLatitude, checkinLongitude, pickupLatitude, pickupLongitude]);

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
        'relative isolate z-0 h-64 w-full rounded-md border',
        className
      )}
    />
  );
};
