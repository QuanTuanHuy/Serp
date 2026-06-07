/**
 * Author: GitHub Copilot
 * Description: Shared Leaflet-based location picker for module forms
 */

'use client';

import { useEffect, useRef, useState } from 'react';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

import { cn } from '@/shared/utils';

type LeafletMap = import('leaflet').Map;
type LeafletMarker = import('leaflet').Marker;

interface MapLocationPickerProps {
  initialLat: number;
  initialLng: number;
  onLocationSelect: (lat: number, lng: number) => void;
  className?: string;
  zoom?: number;
  disabled?: boolean;
}

const DEFAULT_ZOOM = 16;

const resolveAssetUrl = (
  asset: string | { src?: string } | { default?: { src?: string } }
) => {
  if (typeof asset === 'string') {
    return asset;
  }

  if (asset && typeof asset === 'object') {
    if ('src' in asset && typeof asset.src === 'string') {
      return asset.src;
    }

    if (
      'default' in asset &&
      asset.default &&
      typeof asset.default.src === 'string'
    ) {
      return asset.default.src;
    }
  }

  return '';
};

const markerIconUrl = resolveAssetUrl(markerIcon);
const markerShadowUrl = resolveAssetUrl(markerShadow);
const fallbackMarkerIconUrl =
  'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png';
const fallbackMarkerShadowUrl =
  'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png';

export const MapLocationPicker = ({
  initialLat,
  initialLng,
  onLocationSelect,
  className,
  zoom = DEFAULT_ZOOM,
  disabled = false,
}: MapLocationPickerProps) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<LeafletMap | null>(null);
  const markerRef = useRef<LeafletMarker | null>(null);
  const onLocationSelectRef = useRef(onLocationSelect);
  const disabledRef = useRef(disabled);
  const [mapReady, setMapReady] = useState(false);
  const [position, setPosition] = useState<[number, number]>([
    initialLat,
    initialLng,
  ]);

  const containerClassName = cn(
    'h-[300px] w-full overflow-hidden rounded-md border',
    className
  );

  useEffect(() => {
    onLocationSelectRef.current = onLocationSelect;
  }, [onLocationSelect]);

  useEffect(() => {
    disabledRef.current = disabled;
  }, [disabled]);

  useEffect(() => {
    setPosition([initialLat, initialLng]);
  }, [initialLat, initialLng]);

  useEffect(() => {
    let isCancelled = false;

    const initializeMap = async () => {
      if (!containerRef.current || mapRef.current) {
        return;
      }

      const importedLeaflet = await import('leaflet');

      if (isCancelled) {
        return;
      }

      const defaultMarkerIcon = importedLeaflet.icon({
        iconUrl: markerIconUrl || fallbackMarkerIconUrl,
        shadowUrl: markerShadowUrl || fallbackMarkerShadowUrl,
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41],
      });

      const map = importedLeaflet.map(containerRef.current, {
        zoomControl: true,
      });
      mapRef.current = map;
      setMapReady(true);

      importedLeaflet
        .tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          maxZoom: 19,
          attribution: '&copy; OpenStreetMap contributors',
        })
        .addTo(map);

      map.setView([initialLat, initialLng], zoom);

      markerRef.current = importedLeaflet
        .marker([initialLat, initialLng], {
          draggable: !disabledRef.current,
          icon: defaultMarkerIcon,
        })
        .addTo(map);

      markerRef.current.on('dragend', () => {
        const latLng = markerRef.current?.getLatLng();

        if (!latLng) {
          return;
        }

        const lat = Number(latLng.lat.toFixed(6));
        const lng = Number(latLng.lng.toFixed(6));

        setPosition([lat, lng]);
        onLocationSelectRef.current(lat, lng);
      });

      map.on('click', (event) => {
        if (disabledRef.current) {
          return;
        }

        const lat = Number(event.latlng.lat.toFixed(10));
        const lng = Number(event.latlng.lng.toFixed(10));

        setPosition([lat, lng]);
        markerRef.current?.setLatLng([lat, lng]);
        onLocationSelectRef.current(lat, lng);
      });

      window.setTimeout(() => {
        map.invalidateSize();
      }, 0);
    };

    void initializeMap();

    return () => {
      isCancelled = true;

      if (mapRef.current) {
        mapRef.current.remove();
      }

      markerRef.current = null;
      mapRef.current = null;
      setMapReady(false);
    };
  }, [initialLat, initialLng, zoom]);

  useEffect(() => {
    const map = mapRef.current;

    if (!map) {
      return;
    }

    map.setView([position[0], position[1]], Math.max(map.getZoom(), zoom));

    if (markerRef.current) {
      markerRef.current.setLatLng([position[0], position[1]]);
    }
  }, [position, zoom]);

  return (
    <div ref={containerRef} className={containerClassName}>
      {!mapReady && (
        <div className='flex h-full items-center justify-center text-sm text-muted-foreground'>
          Đang tải bản đồ...
        </div>
      )}
    </div>
  );
};

export default MapLocationPicker;
