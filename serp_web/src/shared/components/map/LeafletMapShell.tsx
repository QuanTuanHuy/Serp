'use client';

import type { ReactNode } from 'react';
import L from 'leaflet';
// @ts-ignore
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
// @ts-ignore
import markerIcon from 'leaflet/dist/images/marker-icon.png';
// @ts-ignore
import markerShadow from 'leaflet/dist/images/marker-shadow.png';
import { MapContainer, TileLayer } from 'react-leaflet';

const resolveLeafletAsset = (asset: string | { src: string }) =>
  typeof asset === 'string' ? asset : asset.src;

L.Icon.Default.mergeOptions({
  iconRetinaUrl: resolveLeafletAsset(markerIcon2x),
  iconUrl: resolveLeafletAsset(markerIcon),
  shadowUrl: resolveLeafletAsset(markerShadow),
});

interface LeafletMapShellProps {
  center: [number, number];
  zoom: number;
  children?: ReactNode;
  className?: string;
  scrollWheelZoom?: boolean;
}

export function LeafletMapShell({
  center,
  zoom,
  children,
  className = 'h-[420px] w-full rounded-[24px]',
  scrollWheelZoom = true,
}: LeafletMapShellProps) {
  return (
    <MapContainer
      center={center}
      zoom={zoom}
      scrollWheelZoom={scrollWheelZoom}
      className={className}
    >
      <TileLayer
        attribution='&copy; OpenStreetMap contributors'
        url='https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
      />
      {children}
    </MapContainer>
  );
}
