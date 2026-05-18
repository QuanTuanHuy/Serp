/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile routes map preview
 */

'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui';

export interface RouteMapLine {
  id: number;
  routeCode: string;
  routeName: string;
  origin: {
    name: string;
    latitude: number;
    longitude: number;
  };
  destination: {
    name: string;
    latitude: number;
    longitude: number;
    type: 'HUB' | 'POST_OFFICE';
  };
}

interface SecondMileRoutesMapProps {
  lines: RouteMapLine[];
  selectedRouteId?: number;
}

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};
const DEFAULT_ZOOM = 6;

export const SecondMileRoutesMap: React.FC<SecondMileRoutesMapProps> = ({
  lines,
  selectedRouteId,
}) => {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const mapRef = React.useRef<import('leaflet').Map | null>(null);
  const leafletRef = React.useRef<typeof import('leaflet') | null>(null);
  const layerGroupRef = React.useRef<import('leaflet').LayerGroup | null>(null);

  React.useEffect(() => {
    let cancelled = false;

    const initMap = async () => {
      if (!containerRef.current || mapRef.current) {
        return;
      }

      const leaflet = await import('leaflet');
      if (!containerRef.current || cancelled) {
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

      map.setView([DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude], DEFAULT_ZOOM);
      layerGroupRef.current = leaflet.layerGroup().addTo(map);
      window.setTimeout(() => map.invalidateSize(), 0);
    };

    void initMap();

    return () => {
      cancelled = true;
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
    const map = mapRef.current;
    const layerGroup = layerGroupRef.current;
    if (!leaflet || !map || !layerGroup) {
      return;
    }

    layerGroup.clearLayers();

    const points: [number, number][] = [];
    lines.forEach((line) => {
      const isSelected = selectedRouteId === line.id;
      const color = isSelected ? '#2563eb' : '#64748b';

      const originPoint: [number, number] = [
        line.origin.latitude,
        line.origin.longitude,
      ];
      const destinationPoint: [number, number] = [
        line.destination.latitude,
        line.destination.longitude,
      ];
      points.push(originPoint, destinationPoint);

      leaflet
        .polyline([originPoint, destinationPoint], {
          color,
          weight: isSelected ? 5 : 3,
          opacity: 0.9,
          dashArray: isSelected ? undefined : '6, 10',
        })
        .addTo(layerGroup);

      leaflet
        .circleMarker(originPoint, {
          radius: 6,
          color: '#0f172a',
          weight: 2,
          fillColor: '#22c55e',
          fillOpacity: 0.9,
        })
        .bindPopup(
          `<div style="font-size:12px;line-height:1.4"><strong>${line.routeCode}</strong><br/>${line.origin.name}<br/><span style="color:#64748b">Origin hub</span></div>`
        )
        .addTo(layerGroup);

      leaflet
        .circleMarker(destinationPoint, {
          radius: 6,
          color: '#0f172a',
          weight: 2,
          fillColor: line.destination.type === 'HUB' ? '#f59e0b' : '#ef4444',
          fillOpacity: 0.9,
        })
        .bindPopup(
          `<div style="font-size:12px;line-height:1.4"><strong>${line.routeName}</strong><br/>${line.destination.name}<br/><span style="color:#64748b">Destination ${line.destination.type === 'HUB' ? 'hub' : 'post office'}</span></div>`
        )
        .addTo(layerGroup);
    });

    if (points.length === 0) {
      map.setView([DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude], DEFAULT_ZOOM);
      return;
    }
    if (points.length === 1) {
      map.setView(points[0], 12);
      return;
    }
    map.fitBounds(leaflet.latLngBounds(points).pad(0.25));
  }, [lines, selectedRouteId]);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Route map</CardTitle>
      </CardHeader>
      <CardContent>
        <div ref={containerRef} className='h-[420px] overflow-hidden rounded-lg border' />
      </CardContent>
    </Card>
  );
};
