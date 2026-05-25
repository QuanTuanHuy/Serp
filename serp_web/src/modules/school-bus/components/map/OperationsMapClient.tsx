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
import { useMapMarkerVisibility } from './MapMarkerVisibilityContext';
import { useMapExpand } from './MapExpandContext';

// ── Map size invalidator — reacts to expand/collapse ─────────────────────────
function MapInvalidator({ trigger }: { trigger: number }) {
  const map = useMap();
  React.useEffect(() => {
    const t = setTimeout(() => map.invalidateSize(), 150);
    return () => clearTimeout(t);
  }, [map, trigger]);
  return null;
}

export interface StudentMapMarker {
  key: string;
  studentName: string;
  pointName: string;
  latitude: number;
  longitude: number;
  role: 'pickup' | 'dropoff';
}

interface OperationsMapClientProps {
  schools?: SchoolBusSchool[];
  pickupPoints?: SchoolBusPickupPoint[];
  depots?: SchoolBusDepot[];
  studentMarkers?: StudentMapMarker[];
  selectedSchoolId?: number | null;
  selectedPickupPointId?: number | null;
  selectedDepotId?: number | null;
  onSchoolSelect?: (id: number) => void;
  onPickupPointSelect?: (id: number) => void;
  onDepotSelect?: (id: number) => void;
  className?: string;
  /** Bump to programmatically fit-all markers (ignores current selection). */
  fitAllKey?: number;
  /** Bump to re-center on the currently selected item. */
  fitSelectedKey?: number;
}

export default function OperationsMapClient({
  schools = [],
  pickupPoints = [],
  depots = [],
  studentMarkers = [],
  selectedSchoolId,
  selectedPickupPointId,
  selectedDepotId,
  onSchoolSelect,
  onPickupPointSelect,
  onDepotSelect,
  className,
  fitAllKey,
  fitSelectedKey,
}: OperationsMapClientProps) {
  const { isVisible } = useMapMarkerVisibility();

  // ── Normalize all markers into a unified list ─────────────────────
  const groupedMarkers = React.useMemo(() => {
    type MarkerItem = {
      kind: 'school' | 'depot' | 'pickup' | 'student';
      lat: number;
      lng: number;
      /** Priority for icon selection (lower = higher priority) */
      priority: number;
      popup: React.ReactNode;
      onClick?: () => void;
    };

    const items: MarkerItem[] = [];

    if (isVisible('school')) {
      schools
        .filter((s) => typeof s.latitude === 'number' && typeof s.longitude === 'number')
        .forEach((s) =>
          items.push({
            kind: 'school',
            lat: s.latitude as number,
            lng: s.longitude as number,
            priority: 0,
            onClick: () => onSchoolSelect?.(s.id),
            popup: (
              <div className='space-y-0.5'>
                <p className='text-xs font-semibold text-rose-700'>🎓 School</p>
                <p className='font-medium text-slate-950'>{s.name}</p>
                <p className='text-xs text-slate-500'>{s.address || 'No address'}</p>
              </div>
            ),
          })
        );
    }

    if (isVisible('depot')) {
      depots
        .filter((d) => typeof d.latitude === 'number' && typeof d.longitude === 'number')
        .forEach((d) =>
          items.push({
            kind: 'depot',
            lat: d.latitude as number,
            lng: d.longitude as number,
            priority: 1,
            onClick: () => onDepotSelect?.(d.id),
            popup: (
              <div className='space-y-0.5'>
                <p className='text-xs font-semibold text-amber-700'>🏭 Depot</p>
                <p className='font-medium text-slate-950'>{d.name}</p>
                <p className='text-xs text-slate-500'>{d.address || 'No address'}</p>
              </div>
            ),
          })
        );
    }

    if (isVisible('pickup')) {
      pickupPoints
        .filter((p) => typeof p.latitude === 'number' && typeof p.longitude === 'number')
        .forEach((p) =>
          items.push({
            kind: 'pickup',
            lat: p.latitude as number,
            lng: p.longitude as number,
            priority: 2,
            onClick: () => onPickupPointSelect?.(p.id),
            popup: (
              <div className='space-y-0.5'>
                <p className='text-xs font-semibold text-sky-700'>📍 Pickup point</p>
                <p className='font-medium text-slate-950'>{p.name}</p>
                <p className='text-xs text-slate-500'>{p.address}</p>
              </div>
            ),
          })
        );
    }

    if (isVisible('student')) {
      studentMarkers.forEach((sm) =>
        items.push({
          kind: 'student',
          lat: sm.latitude,
          lng: sm.longitude,
          priority: 3,
          popup: (
            <div className='space-y-0.5'>
              <p className='text-xs font-semibold text-violet-700'>
                {sm.role === 'pickup' ? '🔵' : '🟢'} Student
              </p>
              <p className='font-medium text-slate-950'>{sm.studentName}</p>
              <p className='text-xs text-slate-500'>
                {sm.role === 'pickup' ? 'Pickup' : 'Drop-off'}: {sm.pointName}
              </p>
            </div>
          ),
        })
      );
    }

    // Group by coordinate key (rounded to 5 decimal places ≈ 1m precision)
    const groups = new Map<string, MarkerItem[]>();
    for (const item of items) {
      const key = `${item.lat.toFixed(5)}_${item.lng.toFixed(5)}`;
      const group = groups.get(key);
      if (group) {
        group.push(item);
      } else {
        groups.set(key, [item]);
      }
    }

    return Array.from(groups.entries()).map(([key, group]) => {
      // Sort by priority → pick highest-priority kind for icon
      group.sort((a, b) => a.priority - b.priority);
      const primary = group[0];
      // Collect the first onClick from the highest-priority interactive item
      const firstClick = group.find((g) => g.onClick)?.onClick;
      return { key, primary, group, onClick: firstClick };
    });
  }, [
    schools, pickupPoints, depots, studentMarkers,
    isVisible, onSchoolSelect, onPickupPointSelect, onDepotSelect,
  ]);

  const { isExpanded, expandKey } = useMapExpand();
  const leafletClass = isExpanded
    ? cn('h-full w-full', schoolBusUi.mapFrame)
    : cn('h-[420px] w-full', schoolBusUi.mapFrame, className);

  return (
    <LeafletMapShell
      center={[
        SCHOOL_BUS_MAP_DEFAULT_CENTER.lat,
        SCHOOL_BUS_MAP_DEFAULT_CENTER.lng,
      ]}
      zoom={SCHOOL_BUS_MAP_DEFAULT_ZOOM}
      className={leafletClass}
    >
      <MapInvalidator trigger={expandKey} />
      <OperationsViewport
        schools={schools}
        pickupPoints={pickupPoints}
        depots={depots}
        selectedSchoolId={selectedSchoolId}
        selectedPickupPointId={selectedPickupPointId}
        selectedDepotId={selectedDepotId}
        fitAllKey={fitAllKey}
        fitSelectedKey={fitSelectedKey}
      />

      {groupedMarkers.map(({ key, primary, group, onClick }) => (
        <Marker
          key={key}
          position={[primary.lat, primary.lng]}
          icon={createSchoolBusMarkerIcon(
            primary.kind,
            primary.kind === 'student' ? 26 : 32
          )}
          eventHandlers={onClick ? { click: onClick } : undefined}
        >
          <Popup maxWidth={280}>
            <div
              className='space-y-2'
              style={{ maxHeight: '220px', overflowY: 'auto' }}
            >
              {group.length > 1 && (
                <p className='rounded-md bg-slate-100 px-2 py-0.5 text-center text-[10px] font-semibold text-slate-500'>
                  {group.length} items at this location
                </p>
              )}
              {group.map((item, idx) => (
                <div
                  key={idx}
                  className={
                    idx > 0
                      ? 'border-t border-slate-100 pt-2'
                      : ''
                  }
                >
                  {item.popup}
                </div>
              ))}
            </div>
          </Popup>
        </Marker>
      ))}
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
  fitAllKey,
  fitSelectedKey,
}: {
  schools: SchoolBusSchool[];
  pickupPoints: SchoolBusPickupPoint[];
  depots: SchoolBusDepot[];
  selectedSchoolId?: number | null;
  selectedPickupPointId?: number | null;
  selectedDepotId?: number | null;
  fitAllKey?: number;
  fitSelectedKey?: number;
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
  }, [depots, fitSelectedKey, map, pickupPoints, schools, selectedDepotId, selectedPickupPointId, selectedSchoolId]);

  // Fit All: triggered by button — always fits all markers regardless of selection.
  React.useEffect(() => {
    if (!fitAllKey) return;
    const coordinates: [number, number][] = [
      ...schools
        .filter((s) => typeof s.latitude === 'number' && typeof s.longitude === 'number')
        .map((s) => [s.latitude as number, s.longitude as number] as [number, number]),
      ...pickupPoints
        .filter((p) => typeof p.latitude === 'number' && typeof p.longitude === 'number')
        .map((p) => [p.latitude as number, p.longitude as number] as [number, number]),
      ...depots
        .filter((d) => typeof d.latitude === 'number' && typeof d.longitude === 'number')
        .map((d) => [d.latitude as number, d.longitude as number] as [number, number]),
    ];
    if (coordinates.length === 0) {
      map.setView(
        [SCHOOL_BUS_MAP_DEFAULT_CENTER.lat, SCHOOL_BUS_MAP_DEFAULT_CENTER.lng],
        SCHOOL_BUS_MAP_DEFAULT_ZOOM
      );
    } else if (coordinates.length === 1) {
      map.setView(coordinates[0], SCHOOL_BUS_MAP_DETAIL_ZOOM);
    } else {
      map.fitBounds(latLngBounds(coordinates), { padding: [32, 32] });
    }
    // fitAllKey is the intentional trigger; schools/pickupPoints/depots are
    // intentionally omitted so the effect only fires when the button is clicked.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [map, fitAllKey]);

  return null;
}
