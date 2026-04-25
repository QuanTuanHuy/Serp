/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - Leaflet-based coordinate picker map
 */

'use client';

import React from 'react';
import { cn } from '@/shared/utils';

interface CoordinatePickerMapProps {
  latitude?: number;
  longitude?: number;
  className?: string;
  disabled?: boolean;
  onChange: (latitude: number, longitude: number) => void;
}

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};

const DEFAULT_ZOOM = 6;
const FOCUSED_ZOOM = 15;

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletCircleMarker = import('leaflet').CircleMarker;

export const CoordinatePickerMap: React.FC<CoordinatePickerMapProps> = ({
  latitude,
  longitude,
  className,
  disabled = false,
  onChange,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const leafletRef = React.useRef<LeafletModule | null>(null);
  const mapRef = React.useRef<LeafletMap | null>(null);
  const markerRef = React.useRef<LeafletCircleMarker | null>(null);
  const onChangeRef = React.useRef(onChange);
  const disabledRef = React.useRef(disabled);

  React.useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  React.useEffect(() => {
    disabledRef.current = disabled;
  }, [disabled]);

  const syncMarker = React.useCallback((lat: number, lng: number) => {
    const leaflet = leafletRef.current;
    const map = mapRef.current;

    if (!leaflet || !map) {
      return;
    }

    if (markerRef.current) {
      markerRef.current.setLatLng([lat, lng]);
      return;
    }

    markerRef.current = leaflet
      .circleMarker([lat, lng], {
        radius: 8,
        color: '#1d4ed8',
        fillColor: '#2563eb',
        fillOpacity: 0.85,
        weight: 2,
      })
      .addTo(map);
  }, []);

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

      const hasInitialCoordinate =
        latitude !== undefined && longitude !== undefined;
      const initialLatitude = hasInitialCoordinate
        ? latitude
        : DEFAULT_CENTER.latitude;
      const initialLongitude = hasInitialCoordinate
        ? longitude
        : DEFAULT_CENTER.longitude;

      map.setView(
        [initialLatitude, initialLongitude],
        hasInitialCoordinate ? FOCUSED_ZOOM : DEFAULT_ZOOM
      );

      if (hasInitialCoordinate) {
        syncMarker(initialLatitude, initialLongitude);
      }

      map.on('click', (event) => {
        if (disabledRef.current) {
          return;
        }

        const { lat, lng } = event.latlng;
        const roundedLatitude = Number(lat.toFixed(6));
        const roundedLongitude = Number(lng.toFixed(6));

        syncMarker(roundedLatitude, roundedLongitude);
        onChangeRef.current(roundedLatitude, roundedLongitude);
      });

      // Force size recalculation after the dialog becomes visible.
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
      markerRef.current = null;
      leafletRef.current = null;
    };
  }, [syncMarker]);

  React.useEffect(() => {
    const map = mapRef.current;

    if (!map) {
      return;
    }

    if (latitude === undefined || longitude === undefined) {
      if (markerRef.current) {
        markerRef.current.removeFrom(map);
        markerRef.current = null;
      }
      return;
    }

    syncMarker(latitude, longitude);
    map.setView([latitude, longitude], Math.max(map.getZoom(), FOCUSED_ZOOM));
  }, [latitude, longitude, syncMarker]);

  return (
    <div
      ref={containerRef}
      className={cn('h-64 w-full rounded-md border', className)}
    />
  );
};
