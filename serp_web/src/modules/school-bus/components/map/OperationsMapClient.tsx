'use client';

import * as React from 'react';
import { CircleMarker, Popup, useMap } from 'react-leaflet';
import { latLngBounds } from 'leaflet';
import { LeafletMapShell } from '@/shared/components/map/LeafletMapShell';
import { cn } from '@/shared/utils';
import {
  SCHOOL_BUS_MAP_DEFAULT_CENTER,
  SCHOOL_BUS_MAP_DEFAULT_ZOOM,
  SCHOOL_BUS_MAP_DETAIL_ZOOM,
} from '../../constants';
import { schoolBusBrand, schoolBusUi } from '../../theme';
import type { SchoolBusPickupPoint, SchoolBusSchool } from '../../types';

interface OperationsMapClientProps {
  schools?: SchoolBusSchool[];
  pickupPoints?: SchoolBusPickupPoint[];
  selectedSchoolId?: number | null;
  selectedPickupPointId?: number | null;
  onSchoolSelect?: (id: number) => void;
  onPickupPointSelect?: (id: number) => void;
  className?: string;
}

export default function OperationsMapClient({
  schools = [],
  pickupPoints = [],
  selectedSchoolId,
  selectedPickupPointId,
  onSchoolSelect,
  onPickupPointSelect,
  className,
}: OperationsMapClientProps) {
  return (
    <LeafletMapShell
      center={[
        SCHOOL_BUS_MAP_DEFAULT_CENTER.lat,
        SCHOOL_BUS_MAP_DEFAULT_CENTER.lng,
      ]}
      zoom={SCHOOL_BUS_MAP_DEFAULT_ZOOM}
      className={cn('h-[420px] w-full', schoolBusUi.mapFrame, className)}
    >
      <OperationsViewport
        schools={schools}
        pickupPoints={pickupPoints}
        selectedSchoolId={selectedSchoolId}
        selectedPickupPointId={selectedPickupPointId}
      />

      {schools
        .filter(
          (school) =>
            typeof school.latitude === 'number' &&
            typeof school.longitude === 'number'
        )
        .map((school) => {
          const selected = school.id === selectedSchoolId;
          return (
            <CircleMarker
              key={`school-${school.id}`}
              center={[school.latitude as number, school.longitude as number]}
              radius={selected ? 12 : 10}
              pathOptions={{
                color: selected ? schoolBusBrand.roseDark : schoolBusBrand.rose,
                fillColor: selected ? schoolBusBrand.rose : '#fb7185',
                fillOpacity: 0.85,
              }}
              eventHandlers={{
                click: () => onSchoolSelect?.(school.id),
              }}
            >
              <Popup>
                <div className='space-y-1'>
                  <p className='font-medium text-slate-950'>{school.name}</p>
                  <p className='text-xs text-slate-500'>
                    {school.address || 'No address'}
                  </p>
                </div>
              </Popup>
            </CircleMarker>
          );
        })}

      {pickupPoints
        .filter(
          (pickupPoint) =>
            typeof pickupPoint.latitude === 'number' &&
            typeof pickupPoint.longitude === 'number'
        )
        .map((pickupPoint) => {
          const selected = pickupPoint.id === selectedPickupPointId;
          return (
            <CircleMarker
              key={`pickup-${pickupPoint.id}`}
              center={[
                pickupPoint.latitude as number,
                pickupPoint.longitude as number,
              ]}
              radius={selected ? 10 : 8}
              pathOptions={{
                color: selected ? schoolBusBrand.roseDark : schoolBusBrand.sky,
                fillColor: selected ? schoolBusBrand.rose : '#7dd3fc',
                fillOpacity: 0.85,
              }}
              eventHandlers={{
                click: () => onPickupPointSelect?.(pickupPoint.id),
              }}
            >
              <Popup>
                <div className='space-y-1'>
                  <p className='font-medium text-slate-950'>{pickupPoint.name}</p>
                  <p className='text-xs text-slate-500'>
                    {pickupPoint.address}
                  </p>
                </div>
              </Popup>
            </CircleMarker>
          );
        })}
    </LeafletMapShell>
  );
}

function OperationsViewport({
  schools,
  pickupPoints,
  selectedSchoolId,
  selectedPickupPointId,
}: {
  schools: SchoolBusSchool[];
  pickupPoints: SchoolBusPickupPoint[];
  selectedSchoolId?: number | null;
  selectedPickupPointId?: number | null;
}) {
  const map = useMap();

  React.useEffect(() => {
    const selectedPickup = pickupPoints.find(
      (pickupPoint) =>
        pickupPoint.id === selectedPickupPointId &&
        typeof pickupPoint.latitude === 'number' &&
        typeof pickupPoint.longitude === 'number'
    );

    if (selectedPickup) {
      map.setView(
        [selectedPickup.latitude as number, selectedPickup.longitude as number],
        SCHOOL_BUS_MAP_DETAIL_ZOOM
      );
      return;
    }

    const selectedSchool = schools.find(
      (school) =>
        school.id === selectedSchoolId &&
        typeof school.latitude === 'number' &&
        typeof school.longitude === 'number'
    );

    if (selectedSchool) {
      map.setView(
        [selectedSchool.latitude as number, selectedSchool.longitude as number],
        SCHOOL_BUS_MAP_DETAIL_ZOOM
      );
      return;
    }

    const coordinates = [
      ...schools
        .filter(
          (school) =>
            typeof school.latitude === 'number' &&
            typeof school.longitude === 'number'
        )
        .map((school) => [school.latitude as number, school.longitude as number] as [number, number]),
      ...pickupPoints
        .filter(
          (pickupPoint) =>
            typeof pickupPoint.latitude === 'number' &&
            typeof pickupPoint.longitude === 'number'
        )
        .map((pickupPoint) => [
          pickupPoint.latitude as number,
          pickupPoint.longitude as number,
        ] as [number, number]),
    ];

    if (coordinates.length === 0) {
      map.setView(
        [SCHOOL_BUS_MAP_DEFAULT_CENTER.lat, SCHOOL_BUS_MAP_DEFAULT_CENTER.lng],
        SCHOOL_BUS_MAP_DEFAULT_ZOOM
      );
      return;
    }

    if (coordinates.length === 1) {
      map.setView(coordinates[0], SCHOOL_BUS_MAP_DETAIL_ZOOM);
      return;
    }

    map.fitBounds(latLngBounds(coordinates), { padding: [32, 32] });
  }, [map, pickupPoints, schools, selectedPickupPointId, selectedSchoolId]);

  return null;
}
