/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Transit check-in map
 */

'use client';

import * as React from 'react';

import { cn } from '@/shared/utils';

import type {
  BagDistributionManifest,
  Hub,
  PostOffice,
} from '../../../../types';
import { destinationLabel, formatDateTime } from '../transitModels';

type LeafletModule = typeof import('leaflet');
type LeafletMap = import('leaflet').Map;
type LeafletLayerGroup = import('leaflet').LayerGroup;

interface TransitCheckinMapProps {
  manifests: BagDistributionManifest[];
  hubs: Hub[];
  postOffices: PostOffice[];
  selectedManifestId?: number;
  loading?: boolean;
  className?: string;
  onSelectManifest?: (manifestId: number) => void;
}

interface MapPoint {
  latitude: number;
  longitude: number;
  label: string;
  detail?: string;
  color: string;
  fillColor: string;
  manifestId?: number;
}

const DEFAULT_CENTER: [number, number] = [16.047079, 108.20623];

const hasValidCoordinate = (latitude?: number, longitude?: number) =>
  latitude !== undefined &&
  longitude !== undefined &&
  Number.isFinite(latitude) &&
  Number.isFinite(longitude);

const findHub = (
  hubs: Hub[],
  hubId?: number,
  hubCode?: string
): Hub | undefined =>
  hubs.find(
    (hub) =>
      (hubId !== undefined && hub.id === hubId) ||
      (hubCode !== undefined && hub.code === hubCode)
  );

const findPostOffice = (
  postOffices: PostOffice[],
  code?: string
): PostOffice | undefined =>
  postOffices.find((postOffice) => postOffice.code === code);

const escapeHtml = (value: string) =>
  value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');

const buildPopup = (point: MapPoint) => {
  const detail = point.detail
    ? `<div style="margin-top:4px;color:#64748b">${escapeHtml(point.detail)}</div>`
    : '';

  return `<strong>${escapeHtml(point.label)}</strong>${detail}`;
};

const makeEndpointPoint = (
  label: string,
  latitude?: number,
  longitude?: number,
  detail?: string
): MapPoint | null => {
  if (!hasValidCoordinate(latitude, longitude)) {
    return null;
  }

  return {
    latitude: latitude as number,
    longitude: longitude as number,
    label,
    detail,
    color: '#1d4ed8',
    fillColor: '#3b82f6',
  };
};

const buildMapPoints = (
  manifest: BagDistributionManifest,
  hubs: Hub[],
  postOffices: PostOffice[]
): MapPoint[] => {
  const originHub = findHub(hubs, manifest.originHubId, manifest.originHubCode);
  const destinationHub =
    manifest.destinationType === 'HUB'
      ? findHub(hubs, manifest.destinationHubId, manifest.destinationHubCode)
      : undefined;
  const destinationPostOffice =
    manifest.destinationType === 'POST_OFFICE'
      ? findPostOffice(postOffices, manifest.destinationPostOfficeCode)
      : undefined;
  const points: MapPoint[] = [];

  const originPoint = makeEndpointPoint(
    `Hub xuất phát ${originHub?.code ?? manifest.originHubCode ?? ''}`.trim(),
    originHub?.latitude,
    originHub?.longitude,
    originHub?.name
  );
  if (originPoint) points.push(originPoint);

  if (
    hasValidCoordinate(
      manifest.driverStartLatitude,
      manifest.driverStartLongitude
    )
  ) {
    points.push({
      latitude: manifest.driverStartLatitude as number,
      longitude: manifest.driverStartLongitude as number,
      label: 'Check-in xuất phát',
      detail:
        manifest.driverStartLocationLabel ??
        formatDateTime(manifest.driverStartCheckinAt),
      color: '#15803d',
      fillColor: '#22c55e',
      manifestId: manifest.id,
    });
  }

  if (
    hasValidCoordinate(manifest.driverEndLatitude, manifest.driverEndLongitude)
  ) {
    points.push({
      latitude: manifest.driverEndLatitude as number,
      longitude: manifest.driverEndLongitude as number,
      label: 'Check-in đến nơi',
      detail:
        manifest.driverEndLocationLabel ??
        formatDateTime(manifest.driverEndCheckinAt),
      color: '#be123c',
      fillColor: '#fb7185',
      manifestId: manifest.id,
    });
  }

  const destinationPoint = makeEndpointPoint(
    destinationLabel(
      manifest.destinationType,
      manifest.destinationHubId,
      manifest.destinationHubCode,
      manifest.destinationPostOfficeCode
    ),
    destinationHub?.latitude ?? destinationPostOffice?.latitude,
    destinationHub?.longitude ?? destinationPostOffice?.longitude,
    destinationHub?.name ?? destinationPostOffice?.name
  );
  if (destinationPoint) points.push(destinationPoint);

  return points;
};

export function TransitCheckinMap({
  manifests,
  hubs,
  postOffices,
  selectedManifestId,
  loading,
  className,
  onSelectManifest,
}: TransitCheckinMapProps) {
  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const leafletRef = React.useRef<LeafletModule | null>(null);
  const mapRef = React.useRef<LeafletMap | null>(null);
  const markerLayerRef = React.useRef<LeafletLayerGroup | null>(null);

  const visibleManifests = React.useMemo(() => {
    if (!selectedManifestId) return manifests.slice(0, 12);
    return manifests.filter((manifest) => manifest.id === selectedManifestId);
  }, [manifests, selectedManifestId]);

  const syncMap = React.useCallback(() => {
    const leaflet = leafletRef.current;
    const map = mapRef.current;

    if (!leaflet || !map) {
      return;
    }

    if (markerLayerRef.current) {
      markerLayerRef.current.removeFrom(map);
    }

    const layer = leaflet.layerGroup();
    markerLayerRef.current = layer;
    const bounds: [number, number][] = [];

    visibleManifests.forEach((manifest) => {
      const points = buildMapPoints(manifest, hubs, postOffices);
      const routePoints = points.map(
        (point) => [point.latitude, point.longitude] as [number, number]
      );

      if (routePoints.length > 1) {
        leaflet
          .polyline(routePoints, {
            color: manifest.id === selectedManifestId ? '#0f172a' : '#64748b',
            opacity: manifest.id === selectedManifestId ? 0.85 : 0.45,
            weight: manifest.id === selectedManifestId ? 4 : 2,
          })
          .addTo(layer);
      }

      points.forEach((point) => {
        const latLng: [number, number] = [point.latitude, point.longitude];
        bounds.push(latLng);
        const marker = leaflet
          .circleMarker(latLng, {
            radius: manifest.id === selectedManifestId ? 8 : 6,
            color: point.color,
            fillColor: point.fillColor,
            fillOpacity: 0.9,
            weight: 2,
          })
          .bindPopup(buildPopup(point))
          .addTo(layer);

        if (point.manifestId && onSelectManifest) {
          marker.on('click', () =>
            onSelectManifest(point.manifestId as number)
          );
        }
      });
    });

    layer.addTo(map);

    if (bounds.length === 0) {
      map.setView(DEFAULT_CENTER, 6);
      return;
    }

    if (bounds.length === 1) {
      map.setView(bounds[0], 13);
      return;
    }

    map.fitBounds(leaflet.latLngBounds(bounds).pad(0.28));
  }, [
    hubs,
    onSelectManifest,
    postOffices,
    selectedManifestId,
    visibleManifests,
  ]);

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
      const map = leaflet.map(containerRef.current, { zoomControl: true });
      mapRef.current = map;

      leaflet
        .tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '&copy; OpenStreetMap contributors',
        })
        .addTo(map);

      syncMap();
      window.setTimeout(() => map.invalidateSize(), 0);
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
  }, [syncMap]);

  React.useEffect(() => {
    syncMap();
  }, [syncMap]);

  return (
    <div className='relative'>
      <div
        ref={containerRef}
        className={cn(
          'relative isolate z-0 h-[360px] w-full rounded-md border',
          className
        )}
      />
      {loading ? (
        <div className='absolute inset-x-3 top-3 rounded-md border bg-background/90 px-3 py-2 text-sm text-muted-foreground shadow-sm'>
          Đang tải dữ liệu bản đồ...
        </div>
      ) : null}
      {!loading && manifests.length === 0 ? (
        <div className='absolute inset-x-3 top-3 rounded-md border bg-background/90 px-3 py-2 text-sm text-muted-foreground shadow-sm'>
          Chưa có dữ liệu check-in để hiển thị.
        </div>
      ) : null}
    </div>
  );
}
