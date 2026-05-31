'use client';

import React, { useEffect } from 'react';
import { Marker, Polyline, Popup, useMap } from 'react-leaflet';
import { LeafletMapShell } from '@/shared/components/map/LeafletMapShell';
import {
  createSchoolBusMarkerIcon,
  createPickupWithCountIcon,
  createStopNumberIcon,
} from './mapIcons';
import {
  SCHOOL_BUS_MAP_DEFAULT_CENTER,
  SCHOOL_BUS_MAP_DEFAULT_ZOOM,
  SCHOOL_BUS_MAP_DETAIL_ZOOM,
} from '../../constants';
import type { SchoolBusRoutePath, SchoolBusRouteStop } from '../../types';

// ── Auto-fit bounds when markers / fitKey change ──────────────────────────────

interface FitBoundsProps {
  positions: [number, number][];
  fitKey?: number;
}
function FitBounds({ positions, fitKey }: FitBoundsProps) {
  const map = useMap();
  useEffect(() => {
    if (positions.length === 0) return;
    if (positions.length === 1) {
      map.setView(positions[0], SCHOOL_BUS_MAP_DETAIL_ZOOM);
    } else {
      map.fitBounds(positions, { padding: [48, 48] });
    }
    // fitKey is an intentional extra dependency to let callers trigger a re-fit
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [map, positions, fitKey]);
  return null;
}

// ── Invalidate Map Size ──────────────────────────────────────────────────────

function MapInvalidator({ trigger }: { trigger?: number | boolean }) {
  const map = useMap();
  useEffect(() => {
    const timer = setTimeout(() => {
      map.invalidateSize();
    }, 150);
    return () => clearTimeout(timer);
  }, [map, trigger]);
  return null;
}

// ── Props ────────────────────────────────────────────────────────────────────

interface PlanningPickupPoint {
  pickupPointId: number;
  pickupPointName: string;
  latitude?: number | null;
  longitude?: number | null;
  studentCount: number;
}

interface PlanningSchool {
  id?: number;
  name?: string;
  latitude?: number | null;
  longitude?: number | null;
}

interface PlanningDepot {
  id: number;
  name: string;
  latitude?: number | null;
  longitude?: number | null;
  address?: string | null;
}

export interface PlanningMapClientProps {
  school?: PlanningSchool | null;
  pickupPoints?: PlanningPickupPoint[];
  /** Stops of the currently selected route (in order) */
  selectedRouteStops?: SchoolBusRouteStop[];
  /** Real road geometry returned by GET /routes/{id}/path */
  selectedRoutePath?: SchoolBusRoutePath | null;
  depots?: PlanningDepot[];
  className?: string;
  isMapExpanded?: boolean;
  /**
   * When set to 'route', FitBounds uses the selected-route stop positions.
   * When set to 'all' (default), FitBounds uses all known positions.
   */
  fitTarget?: 'all' | 'route';
  /**
   * Bump this number to programmatically re-trigger a fit (e.g. after
   * clicking "Fit All" or "Fit Route" in the toolbar).
   */
  fitKey?: number;
}

// ── Component ────────────────────────────────────────────────────────────────

export default function PlanningMapClient({
  school,
  pickupPoints = [],
  selectedRouteStops = [],
  selectedRoutePath,
  depots = [],
  className = 'h-[420px] w-full rounded-[24px]',
  isMapExpanded,
  fitTarget,
  fitKey,
}: PlanningMapClientProps) {
  // Build list of all known positions for auto-fit ("fit all")
  const allPositions: [number, number][] = [];

  if (typeof school?.latitude === 'number' && typeof school?.longitude === 'number') {
    allPositions.push([school.latitude, school.longitude]);
  }
  for (const pp of pickupPoints) {
    if (typeof pp.latitude === 'number' && typeof pp.longitude === 'number') {
      allPositions.push([pp.latitude, pp.longitude]);
    }
  }
  for (const d of depots) {
    if (typeof d.latitude === 'number' && typeof d.longitude === 'number') {
      allPositions.push([d.latitude, d.longitude]);
    }
  }

  // Route stop positions (in order) — used for polyline + "fit route"
  // Uses displayName coordinates (new terminal model) or legacy pickupPoint coords
  const sortedStops = [...selectedRouteStops]
    .filter((s) => {
      const lat = typeof s.pickupPointLatitude === 'number'
        ? s.pickupPointLatitude
        : null;
      const lon = typeof s.pickupPointLongitude === 'number'
        ? s.pickupPointLongitude
        : null;
      return lat !== null && lon !== null;
    })
    .sort((a, b) => a.stopOrder - b.stopOrder);

  const routeLinePositions: [number, number][] = sortedStops.map(
    (s) => [s.pickupPointLatitude as number, s.pickupPointLongitude as number]
  );

  // Use actual road geometry when available, fallback to straight-line stop connections
  const actualPathCoordinates: [number, number][] =
    selectedRoutePath?.coordinates
      ?.filter(
        (p) => typeof p.latitude === 'number' && typeof p.longitude === 'number'
      )
      .map((p) => [p.latitude, p.longitude] as [number, number]) ?? [];
  const resolvedLinePositions =
    actualPathCoordinates.length >= 2 ? actualPathCoordinates : routeLinePositions;

  const isFallback =
    selectedRoutePath?.fallbackUsed === true ||
    selectedRoutePath?.geometrySource === 'STRAIGHT_LINE_ESTIMATE' ||
    (selectedRoutePath != null && actualPathCoordinates.length < 2);

  // Positions passed to FitBounds depend on fitTarget
  const fitPositions: [number, number][] =
    fitTarget === 'route' && resolvedLinePositions.length > 0
      ? resolvedLinePositions
      : allPositions;

  const defaultCenter: [number, number] = [
    SCHOOL_BUS_MAP_DEFAULT_CENTER.lat,
    SCHOOL_BUS_MAP_DEFAULT_CENTER.lng,
  ];

  const initialCenter = allPositions.length > 0 ? allPositions[0] : defaultCenter;
  const initialZoom =
    allPositions.length > 0 ? SCHOOL_BUS_MAP_DETAIL_ZOOM : SCHOOL_BUS_MAP_DEFAULT_ZOOM;

  const markerContent = (
    <>
      {/* School marker */}
      {typeof school?.latitude === 'number' && typeof school?.longitude === 'number' && (
        <Marker
          position={[school.latitude, school.longitude]}
          icon={createSchoolBusMarkerIcon('school', 30)}
        >
          <Popup>
            <div>
              <p className='text-xs font-semibold text-rose-700'>🎓 School</p>
              <p className='font-medium'>{school.name ?? 'School'}</p>
            </div>
          </Popup>
        </Marker>
      )}

      {/* Depot markers */}
      {depots.map((d) => {
        if (typeof d.latitude !== 'number' || typeof d.longitude !== 'number') return null;
        return (
          <Marker
            key={`depot-${d.id}`}
            position={[d.latitude, d.longitude]}
            icon={createSchoolBusMarkerIcon('depot', 26)}
          >
            <Popup>
              <div>
                <p className='text-xs font-semibold text-amber-700'>🏭 Depot</p>
                <p className='font-medium'>{d.name}</p>
                {d.address && <p className='text-xs text-slate-500'>{d.address}</p>}
              </div>
            </Popup>
          </Marker>
        );
      })}

      {/* Pickup point markers — with student-count badge */}
      {pickupPoints.map((pp) => {
        if (typeof pp.latitude !== 'number' || typeof pp.longitude !== 'number') return null;
        return (
          <Marker
            key={`pp-${pp.pickupPointId}`}
            position={[pp.latitude, pp.longitude]}
            icon={createPickupWithCountIcon(pp.studentCount, 28)}
          >
            <Popup>
              <div>
                <p className='text-xs font-semibold text-sky-700'>📍 Pickup point</p>
                <p className='font-medium'>{pp.pickupPointName}</p>
                <p className='text-xs text-slate-500'>{pp.studentCount} student(s)</p>
              </div>
            </Popup>
          </Marker>
        );
      })}

      {/* Selected route: numbered stop markers (indigo, size 26 — distinct from pickup sky blue) */}
      {sortedStops.map((stop, idx) => (
        <Marker
          key={`stop-${stop.id}`}
          position={[stop.pickupPointLatitude as number, stop.pickupPointLongitude as number]}
          icon={createStopNumberIcon(idx + 1, 26)}
          zIndexOffset={500}
        >
          <Popup>
            <div>
              <p className='text-xs font-semibold text-indigo-700'>Stop {idx + 1}</p>
              <p className='font-medium'>{stop.pickupPointName ?? `Stop #${stop.id}`}</p>
              <p className='text-xs text-slate-500'>
                {stop.estimatedStudentCount ?? 0} student(s)
              </p>
              {stop.plannedArrivalTime && (
                <p className='text-xs text-slate-400'>ETA: {stop.plannedArrivalTime}</p>
              )}
            </div>
          </Popup>
        </Marker>
      ))}

      {/* Route polyline — real road geometry if available, else straight-line fallback */}
      {resolvedLinePositions.length >= 2 && (
        <Polyline
          positions={resolvedLinePositions}
          color={isFallback ? '#f59e0b' : '#0369a1'}
          weight={isFallback ? 3 : 4}
          opacity={0.85}
          dashArray={isFallback ? '10 6' : undefined}
        />
      )}
    </>
  );

  const shellContent = (
    <>
      <FitBounds positions={fitPositions} fitKey={fitKey} />
      <MapInvalidator trigger={isMapExpanded ? 1 : fitKey} />
      {markerContent}
    </>
  );

  if (isMapExpanded) {
    return (
      <div className='relative h-full w-full'>
        <LeafletMapShell
          center={initialCenter}
          zoom={initialZoom}
          className='h-full w-full'
        >
          {shellContent}
        </LeafletMapShell>
        {isFallback && (
          <div className='pointer-events-none absolute bottom-3 left-1/2 z-[1000] -translate-x-1/2'>
            <span className='inline-flex items-center gap-1.5 rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700 shadow-sm'>
              ⚠ Straight-line estimate — road geometry unavailable
            </span>
          </div>
        )}
      </div>
    );
  }

  return (
    <div className='relative h-full w-full'>
      <LeafletMapShell center={initialCenter} zoom={initialZoom} className={className}>
        {shellContent}
      </LeafletMapShell>
      {isFallback && (
        <div className='pointer-events-none absolute bottom-3 left-1/2 z-[1000] -translate-x-1/2'>
          <span className='inline-flex items-center gap-1.5 rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700 shadow-sm'>
            ⚠ Straight-line estimate — road geometry unavailable
          </span>
        </div>
      )}
    </div>
  );
}
