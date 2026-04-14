'use client';

import * as React from 'react';
import { Marker, Popup, useMap } from 'react-leaflet';
import { latLngBounds } from 'leaflet';
import { LeafletMapShell } from '@/shared/components/map/LeafletMapShell';
import { cn } from '@/shared/utils';
import {
  SCHOOL_BUS_MAP_DEFAULT_CENTER,
  SCHOOL_BUS_MAP_DEFAULT_ZOOM,
  SCHOOL_BUS_MAP_DETAIL_ZOOM,
} from '../../constants';
import { schoolBusUi } from '../../theme';
import type {
  SchoolBusDepot,
  SchoolBusPickupPoint,
  SchoolBusSchool,
} from '../../types';
import { createSchoolBusMarkerIcon } from './mapIcons';

interface OperationsMapClientProps {
  schools?: SchoolBusSchool[];
  pickupPoints?: SchoolBusPickupPoint[];
  depots?: SchoolBusDepot[];
  selectedSchoolId?: number | null;
  selectedPickupPointId?: number | null;
  selectedDepotId?: number | null;
  onSchoolSelect?: (id: number) => void;
  onPickupPointSelect?: (id: number) => void;
  onDepotSelect?: (id: number) => void;
  className?: string;
}

export default function OperationsMapClient({
  schools = [],
  pickupPoints = [],
  depots = [],
  selectedSchoolId,
  selectedPickupPointId,
  selectedDepotId,
  onSchoolSelect,
  onPickupPointSelect,
  onDepotSelect,
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
        depots={depots}
        selectedSchoolId={selectedSchoolId}
        selectedPickupPointId={selectedPickupPointId}
        selectedDepotId={selectedDepotId}
      />

      {schools
        .filter(
          (school) =>
            typeof school.latitude === 'number' &&
            typeof school.longitude === 'number'
        )
        .map((school) => {
          return (
            <Marker
              key={`school-${school.id}`}
              position={[school.latitude as number, school.longitude as number]}
              icon={createSchoolBusMarkerIcon('school')}
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
            </Marker>
          );
        })}

      {pickupPoints
        .filter(
          (pickupPoint) =>
            typeof pickupPoint.latitude === 'number' &&
            typeof pickupPoint.longitude === 'number'
        )
        .map((pickupPoint) => {
          return (
            <Marker
              key={`pickup-${pickupPoint.id}`}
              position={[
                pickupPoint.latitude as number,
                pickupPoint.longitude as number,
              ]}
              icon={createSchoolBusMarkerIcon('pickup')}
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
            </Marker>
          );
        })}

      {depots
        .filter(
          (depot) =>
            typeof depot.latitude === 'number' &&
            typeof depot.longitude === 'number'
        )
        .map((depot) => {
          return (
            <Marker
              key={`depot-${depot.id}`}
              position={[depot.latitude as number, depot.longitude as number]}
              icon={createSchoolBusMarkerIcon('depot')}
              eventHandlers={{
                click: () => onDepotSelect?.(depot.id),
              }}
            >
              <Popup>
                <div className='space-y-1'>
                  <p className='font-medium text-slate-950'>{depot.name}</p>
                  <p className='text-xs text-slate-500'>
                    {depot.address || 'No address'}
                  </p>
                  <p className='text-xs font-medium text-amber-700'>Depot</p>
                </div>
              </Popup>
            </Marker>
          );
        })}
    </LeafletMapShell>
  );
}

function OperationsViewport({
  schools,
  pickupPoints,
  depots,
  selectedSchoolId,
  selectedPickupPointId,
  selectedDepotId,
}: {
  schools: SchoolBusSchool[];
  pickupPoints: SchoolBusPickupPoint[];
  depots: SchoolBusDepot[];
  selectedSchoolId?: number | null;
  selectedPickupPointId?: number | null;
  selectedDepotId?: number | null;
}) {
  const map = useMap();

  React.useEffect(() => {
    const selectedDepot = depots.find(
      (depot) =>
        depot.id === selectedDepotId &&
        typeof depot.latitude === 'number' &&
        typeof depot.longitude === 'number'
    );

    if (selectedDepot) {
      map.setView(
        [selectedDepot.latitude as number, selectedDepot.longitude as number],
        SCHOOL_BUS_MAP_DETAIL_ZOOM
      );
      return;
    }

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
      ...depots
        .filter(
          (depot) =>
            typeof depot.latitude === 'number' &&
            typeof depot.longitude === 'number'
        )
        .map((depot) => [depot.latitude as number, depot.longitude as number] as [number, number]),
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
  }, [depots, map, pickupPoints, schools, selectedDepotId, selectedPickupPointId, selectedSchoolId]);

  return null;
}
