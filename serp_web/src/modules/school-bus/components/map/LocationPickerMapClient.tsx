'use client';

import * as React from 'react';
import { Marker, Popup, useMap, useMapEvents } from 'react-leaflet';
import { latLngBounds } from 'leaflet';
import { toast } from 'sonner';
import { LeafletMapShell } from '@/shared/components/map/LeafletMapShell';
import { cn } from '@/shared/utils';
import { useLazyReverseMapLocationQuery } from '../../api/schoolBusApi';
import {
  SCHOOL_BUS_MAP_DEFAULT_CENTER,
  SCHOOL_BUS_MAP_DEFAULT_ZOOM,
  SCHOOL_BUS_MAP_DETAIL_ZOOM,
} from '../../constants';
import { schoolBusUi } from '../../theme';
import type { SchoolBusMapLocation } from '../../types';
import { NominatimSearchBox } from './NominatimSearchBox';
import { SchoolBusMapLegend } from './SchoolBusMapLegend';
import { SchoolBusMapWorkspace } from './SchoolBusMapWorkspace';
import { createSchoolBusMarkerIcon } from './mapIcons';

interface LocationValue {
  latitude?: number | null;
  longitude?: number | null;
}

interface ReferenceMarker {
  id: string | number;
  name: string;
  address?: string | null;
  latitude?: number | null;
  longitude?: number | null;
  type: 'school' | 'pickup' | 'depot';
}

interface LocationPickerMapClientProps {
  value?: LocationValue | null;
  onChange: (next: { latitude: number; longitude: number }) => void;
  onAddressResolved?: (address: string) => void;
  referenceMarkers?: ReferenceMarker[];
  title?: string;
}

export default function LocationPickerMapClient({
  value,
  onChange,
  onAddressResolved,
  referenceMarkers = [],
  title = 'Pin location',
}: LocationPickerMapClientProps) {
  const [reverseLocation] = useLazyReverseMapLocationQuery();
  const selectedLocation =
    typeof value?.latitude === 'number' && typeof value?.longitude === 'number'
      ? ([value.latitude, value.longitude] as [number, number])
      : null;

  const handleLocationChange = async (latitude: number, longitude: number) => {
    onChange({ latitude, longitude });

    try {
      const response = await reverseLocation({
        lat: latitude,
        lng: longitude,
      }).unwrap();
      if (response.data.displayName && onAddressResolved) {
        onAddressResolved(response.data.displayName);
      }
    } catch {
      toast.error('Reverse geocoding is unavailable for this location');
    }
  };

  const handleSearchSelect = (location: SchoolBusMapLocation) => {
    if (
      typeof location.latitude !== 'number' ||
      typeof location.longitude !== 'number'
    ) {
      return;
    }

    onChange({
      latitude: location.latitude,
      longitude: location.longitude,
    });

    if (location.displayName && onAddressResolved) {
      onAddressResolved(location.displayName);
    }
  };

  return (
    <div className={cn('space-y-3', schoolBusUi.subtlePanel)}>
      <div>
        <p className='text-sm font-semibold text-slate-950'>{title}</p>
        <p className='mt-1 text-xs text-slate-500'>
          Search, click, or drag the marker to update coordinates.
        </p>
      </div>

      <SchoolBusMapWorkspace
        defaultPreset='map-focus'
        allowFullscreen={false}
        mapHeightClassName='h-[360px]'
        map={
          <LeafletMapShell
            center={
              selectedLocation || [
                SCHOOL_BUS_MAP_DEFAULT_CENTER.lat,
                SCHOOL_BUS_MAP_DEFAULT_CENTER.lng,
              ]
            }
            zoom={
              selectedLocation
                ? SCHOOL_BUS_MAP_DETAIL_ZOOM
                : SCHOOL_BUS_MAP_DEFAULT_ZOOM
            }
            className={cn('h-full w-full', schoolBusUi.mapFrame)}
          >
            <LocationPickerViewport
              selectedLocation={selectedLocation}
              referenceMarkers={referenceMarkers}
            />
            <LocationPickerEvents onLocationChange={handleLocationChange} />

            {referenceMarkers
              .filter(
                (marker) =>
                  typeof marker.latitude === 'number' &&
                  typeof marker.longitude === 'number'
              )
              .map((marker) => (
                <Marker
                  key={marker.id}
                  position={[marker.latitude as number, marker.longitude as number]}
                  icon={createSchoolBusMarkerIcon(
                    marker.type === 'school'
                      ? 'school'
                      : marker.type === 'depot'
                        ? 'depot'
                        : 'pickup'
                  )}
                >
                  <Popup>
                    <div className='space-y-1'>
                      <p className='font-medium text-slate-950'>{marker.name}</p>
                      <p className='text-xs text-slate-500'>
                        {marker.address || 'No address'}
                      </p>
                    </div>
                  </Popup>
                </Marker>
              ))}

            {selectedLocation ? (
              <Marker
                position={selectedLocation}
                draggable
                icon={createSchoolBusMarkerIcon('start')}
                eventHandlers={{
                  dragend: (event) => {
                    const next = event.target.getLatLng();
                    handleLocationChange(next.lat, next.lng);
                  },
                }}
              >
                <Popup>Selected location</Popup>
              </Marker>
            ) : null}
          </LeafletMapShell>
        }
        legend={<SchoolBusMapLegend />}
        panel={
          <div className='space-y-3'>
            <p className='text-sm font-semibold text-slate-950'>
              OpenStreetMap search
            </p>
            <NominatimSearchBox onSelect={handleSearchSelect} />
            <div className='rounded-xl border border-slate-200 bg-slate-50 p-2 text-xs text-slate-600'>
              <p>
                Latitude:{' '}
                {selectedLocation ? selectedLocation[0].toFixed(6) : 'Not set'}
              </p>
              <p>
                Longitude:{' '}
                {selectedLocation ? selectedLocation[1].toFixed(6) : 'Not set'}
              </p>
            </div>
          </div>
        }
      />
    </div>
  );
}

function LocationPickerEvents({
  onLocationChange,
}: {
  onLocationChange: (latitude: number, longitude: number) => void;
}) {
  useMapEvents({
    click: (event) => {
      onLocationChange(event.latlng.lat, event.latlng.lng);
    },
  });

  return null;
}

function LocationPickerViewport({
  selectedLocation,
  referenceMarkers,
}: {
  selectedLocation: [number, number] | null;
  referenceMarkers: ReferenceMarker[];
}) {
  const map = useMap();

  React.useEffect(() => {
    if (selectedLocation) {
      map.setView(selectedLocation, SCHOOL_BUS_MAP_DETAIL_ZOOM);
      return;
    }

    const coordinates = referenceMarkers
      .filter(
        (marker) =>
          typeof marker.latitude === 'number' &&
          typeof marker.longitude === 'number'
      )
      .map(
        (marker) =>
          [marker.latitude as number, marker.longitude as number] as [
            number,
            number,
          ]
      );

    if (coordinates.length === 0) {
      map.setView(
        [SCHOOL_BUS_MAP_DEFAULT_CENTER.lat, SCHOOL_BUS_MAP_DEFAULT_CENTER.lng],
        SCHOOL_BUS_MAP_DEFAULT_ZOOM
      );
      return;
    }

    map.fitBounds(latLngBounds(coordinates), { padding: [32, 32] });
  }, [map, referenceMarkers, selectedLocation]);

  return null;
}
