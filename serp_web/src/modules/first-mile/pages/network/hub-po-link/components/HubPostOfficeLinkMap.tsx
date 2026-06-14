/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub post office link map
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

export interface HubPostOfficeMapHub {
  id: number;
  code: string;
  name: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  status?: string;
}

export interface HubPostOfficeMapPoint {
  code: string;
  name: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  status?: string;
}

interface HubPostOfficeLinkMapProps {
  hub?: HubPostOfficeMapHub;
  postOffices: HubPostOfficeMapPoint[];
  selectedPostOfficeCode?: string;
  loading?: boolean;
  className?: string;
  onPostOfficeClick?: (postOfficeCode: string) => void;
}

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletLayerGroup = import('leaflet').LayerGroup;

const DEFAULT_CENTER = {
  latitude: 16.047079,
  longitude: 108.20623,
};
const DEFAULT_ZOOM = 6;

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

function buildPopupRow(label: string, value?: string): string {
  return `<div style="display:grid;grid-template-columns:76px minmax(0,1fr);gap:8px;align-items:start"><span style="color:#64748b">${label}</span><span style="color:#0f172a;word-break:break-word">${escapeHtml(value || '--')}</span></div>`;
}

function buildHubPopup(hub: HubPostOfficeMapHub): string {
  const coordinates =
    hub.latitude !== undefined && hub.longitude !== undefined
      ? `${formatCoordinate(hub.latitude)}, ${formatCoordinate(hub.longitude)}`
      : '--';

  return `<div style="min-width:240px;max-width:320px;font-size:12px;line-height:1.45"><strong style="display:block;margin-bottom:8px;font-size:13px;color:#0f172a">${escapeHtml(hub.name)}</strong><div style="display:grid;gap:5px">${[
    buildPopupRow('Type', 'Hub'),
    buildPopupRow('Code', hub.code),
    buildPopupRow('Address', hub.address),
    buildPopupRow('Coordinates', coordinates),
    buildPopupRow('Status', hub.status),
  ].join('')}</div></div>`;
}

function buildPostOfficePopup(point: HubPostOfficeMapPoint): string {
  const coordinates =
    point.latitude !== undefined && point.longitude !== undefined
      ? `${formatCoordinate(point.latitude)}, ${formatCoordinate(point.longitude)}`
      : '--';

  return `<div style="min-width:240px;max-width:320px;font-size:12px;line-height:1.45"><strong style="display:block;margin-bottom:8px;font-size:13px;color:#0f172a">${escapeHtml(point.name)}</strong><div style="display:grid;gap:5px">${[
    buildPopupRow('Type', 'Post office'),
    buildPopupRow('Code', point.code),
    buildPopupRow('Address', point.address),
    buildPopupRow('Coordinates', coordinates),
    buildPopupRow('Status', point.status),
  ].join('')}</div></div>`;
}

function hasCoordinates(
  item: HubPostOfficeMapHub | HubPostOfficeMapPoint | undefined
): item is (HubPostOfficeMapHub | HubPostOfficeMapPoint) & {
  latitude: number;
  longitude: number;
} {
  return (
    item?.latitude !== undefined &&
    item.latitude !== null &&
    item.longitude !== undefined &&
    item.longitude !== null
  );
}

export function HubPostOfficeLinkMap({
  hub,
  postOffices,
  selectedPostOfficeCode,
  loading = false,
  className,
  onPostOfficeClick,
}: HubPostOfficeLinkMapProps) {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const leafletRef = React.useRef<LeafletModule | null>(null);
  const mapRef = React.useRef<LeafletMap | null>(null);
  const layerGroupRef = React.useRef<LeafletLayerGroup | null>(null);

  React.useEffect(() => {
    let isCancelled = false;

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

    const coordinates: Array<[number, number]> = [];

    if (hasCoordinates(hub)) {
      const hubLatLng: [number, number] = [hub.latitude, hub.longitude];
      coordinates.push(hubLatLng);

      leaflet
        .circleMarker(hubLatLng, {
          radius: 10,
          color: '#0f172a',
          weight: 2,
          fillColor: '#0ea5e9',
          fillOpacity: 0.92,
        })
        .bindPopup(buildHubPopup(hub))
        .addTo(layerGroup);

      postOffices.forEach((postOffice) => {
        if (!hasCoordinates(postOffice)) {
          return;
        }

        const isSelected = postOffice.code === selectedPostOfficeCode;
        const poLatLng: [number, number] = [
          postOffice.latitude,
          postOffice.longitude,
        ];
        coordinates.push(poLatLng);

        leaflet
          .polyline([hubLatLng, poLatLng], {
            color: isSelected ? '#f97316' : '#64748b',
            weight: isSelected ? 4 : 2,
            opacity: isSelected ? 0.9 : 0.55,
            dashArray: isSelected ? undefined : '6 6',
          })
          .addTo(layerGroup);

        const marker = leaflet
          .circleMarker(poLatLng, {
            radius: isSelected ? 9 : 7,
            color: isSelected ? '#ea580c' : '#047857',
            weight: 2,
            fillColor: isSelected ? '#fb923c' : '#10b981',
            fillOpacity: 0.88,
          })
          .bindPopup(buildPostOfficePopup(postOffice));

        marker.on('click', () => {
          marker.openPopup();
          onPostOfficeClick?.(postOffice.code);
        });

        marker.addTo(layerGroup);
      });
    }

    if (coordinates.length > 1) {
      const bounds = leaflet.latLngBounds(coordinates);
      map.fitBounds(bounds.pad(0.18), { maxZoom: 13 });
    } else if (coordinates.length === 1) {
      map.setView(coordinates[0], 12);
    } else {
      map.setView(
        [DEFAULT_CENTER.latitude, DEFAULT_CENTER.longitude],
        DEFAULT_ZOOM
      );
    }

    window.setTimeout(() => {
      map.invalidateSize();
    }, 0);
  }, [hub, onPostOfficeClick, postOffices, selectedPostOfficeCode]);

  const geocodedPostOfficeCount = postOffices.filter(hasCoordinates).length;
  const hasGeocodedHub = hasCoordinates(hub);
  const hasMapContent = hasGeocodedHub && geocodedPostOfficeCount > 0;

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>Link map</CardTitle>
        <CardDescription>
          Visual lines show post offices currently assigned to the selected hub.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <div className='relative'>
          <div
            ref={containerRef}
            className={cn('h-[460px] w-full overflow-hidden rounded-md border')}
          />

          <div className='pointer-events-none absolute left-3 top-3 rounded-md border bg-background/95 px-3 py-2 text-xs text-muted-foreground shadow-sm'>
            {loading
              ? 'Loading links...'
              : `${geocodedPostOfficeCount} mapped markers`}
          </div>

          {!loading && !hub ? (
            <div className='pointer-events-none absolute inset-x-3 bottom-3 rounded-md border bg-background/95 px-3 py-2 text-sm text-muted-foreground shadow-sm'>
              Select a hub to view linked post offices.
            </div>
          ) : null}

          {!loading && hub && !hasMapContent ? (
            <div className='pointer-events-none absolute inset-x-3 bottom-3 rounded-md border bg-background/95 px-3 py-2 text-sm text-muted-foreground shadow-sm'>
              This hub needs coordinates and at least one geocoded linked post
              office before lines can be drawn.
            </div>
          ) : null}
        </div>
      </CardContent>
    </Card>
  );
}
