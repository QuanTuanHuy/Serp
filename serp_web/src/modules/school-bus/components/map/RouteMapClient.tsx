'use client';

import React, { useEffect } from 'react';
import { Marker, Polyline, Popup, useMap } from 'react-leaflet';
import { LeafletMapShell } from '@/shared/components/map/LeafletMapShell';
import { useMapExpand } from './MapExpandContext';
import {
  SCHOOL_BUS_MAP_DEFAULT_CENTER,
  SCHOOL_BUS_MAP_DEFAULT_ZOOM,
  SCHOOL_BUS_MAP_DETAIL_ZOOM,
} from '../../constants';
import type {
  SchoolBusRoute,
  SchoolBusRouteAssignment,
  SchoolBusRoutePath,
  SchoolBusRouteStop,
} from '../../types';
import { createSchoolBusMarkerIcon, createStopNumberIcon, type MarkerKind } from './mapIcons';

// ── Auto-fit bounds ───────────────────────────────────────────────────────────

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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [map, positions, fitKey]);
  return null;
}

// ── Map size invalidator ──────────────────────────────────────────────────────

function MapInvalidator({ trigger }: { trigger?: number | boolean }) {
  const map = useMap();
  useEffect(() => {
    const t = setTimeout(() => map.invalidateSize(), 150);
    return () => clearTimeout(t);
  }, [map, trigger]);
  return null;
}

// ── Props ─────────────────────────────────────────────────────────────────────

interface RouteMapClientProps {
  route: SchoolBusRoute;
  stops: SchoolBusRouteStop[];
  assignment?: SchoolBusRouteAssignment | null;
  routePath?: SchoolBusRoutePath | null;
  className?: string;
  fitKey?: number;
}

// ── Component ─────────────────────────────────────────────────────────────────

export default function RouteMapClient({
  route,
  stops,
  assignment,
  routePath,
  className,
  fitKey,
}: RouteMapClientProps) {
  const { isExpanded, expandKey } = useMapExpand();

  const isOutbound = route.routeDirection !== 'RETURN';

  // Terminal coordinates from route metadata
  const startCoord: [number, number] | null =
    typeof route.startLocationLatitude === 'number' &&
    typeof route.startLocationLongitude === 'number'
      ? [route.startLocationLatitude, route.startLocationLongitude]
      : null;

  const endCoord: [number, number] | null =
    typeof route.endLocationLatitude === 'number' &&
    typeof route.endLocationLongitude === 'number'
      ? [route.endLocationLatitude, route.endLocationLongitude]
      : null;

  // Middle stops only — terminals have no pickupPointLatitude in new model
  const sortedMiddleStops = [...stops]
    .filter(
      (s) =>
        typeof s.pickupPointLatitude === 'number' &&
        typeof s.pickupPointLongitude === 'number',
    )
    .sort((a, b) => a.stopOrder - b.stopOrder);

  // All known positions for auto-fit
  const allPositions: [number, number][] = [
    ...(startCoord ? [startCoord] : []),
    ...sortedMiddleStops.map(
      (s) =>
        [s.pickupPointLatitude as number, s.pickupPointLongitude as number] as [
          number,
          number,
        ],
    ),
    ...(endCoord ? [endCoord] : []),
  ];

  // Real road geometry when available
  const actualPathCoords: [number, number][] =
    routePath?.coordinates
      ?.filter(
        (p) => typeof p.latitude === 'number' && typeof p.longitude === 'number',
      )
      .map((p) => [p.latitude, p.longitude] as [number, number]) ?? [];

  // Fallback: straight lines depot/school → middle stops → school/depot
  const fallbackPositions: [number, number][] = [
    ...(startCoord ? [startCoord] : []),
    ...sortedMiddleStops.map(
      (s) =>
        [s.pickupPointLatitude as number, s.pickupPointLongitude as number] as [
          number,
          number,
        ],
    ),
    ...(endCoord ? [endCoord] : []),
  ];

  const resolvedLine: [number, number][] =
    actualPathCoords.length >= 2 ? actualPathCoords : fallbackPositions;

  const isFallback =
    routePath?.fallbackUsed === true ||
    routePath?.geometrySource === 'STRAIGHT_LINE_ESTIMATE' ||
    (routePath != null && actualPathCoords.length < 2);

  // Direction colour: sky/blue for OUTBOUND, emerald for RETURN
  const lineColor = isOutbound ? '#0284c7' : '#059669';

  const defaultCenter: [number, number] = [
    SCHOOL_BUS_MAP_DEFAULT_CENTER.lat,
    SCHOOL_BUS_MAP_DEFAULT_CENTER.lng,
  ];
  const initialCenter =
    allPositions.length > 0 ? allPositions[0] : defaultCenter;
  const initialZoom =
    allPositions.length > 0 ? SCHOOL_BUS_MAP_DETAIL_ZOOM : SCHOOL_BUS_MAP_DEFAULT_ZOOM;

  const startIcon: MarkerKind =
    route.startLocationType === 'DEPOT' ? 'depot' : 'school';
  const endIcon: MarkerKind =
    route.endLocationType === 'DEPOT' ? 'depot' : 'school';

  const markerContent = (
    <>
      {/* Start terminal marker */}
      {startCoord && (
        <Marker
          position={startCoord}
          icon={createSchoolBusMarkerIcon(startIcon, 30)}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='text-xs font-semibold text-amber-700'>
                {route.startLocationType === 'DEPOT' ? '🏭 Depot' : '🎓 School'}
              </p>
              <p className='font-medium'>{route.startLocationName}</p>
              <p className='text-xs text-slate-500'>Route start</p>
              {assignment && (
                <>
                  <p className='text-xs text-slate-500'>
                    🚌 {assignment.busPlateNumber}
                  </p>
                  <p className='text-xs text-slate-500'>
                    👤 {assignment.driverName}
                  </p>
                  {assignment.attendantName && (
                    <p className='text-xs text-slate-500'>
                      🙋 {assignment.attendantName}
                    </p>
                  )}
                </>
              )}
            </div>
          </Popup>
        </Marker>
      )}

      {/* End terminal marker */}
      {endCoord && (
        <Marker
          position={endCoord}
          icon={createSchoolBusMarkerIcon(endIcon, 30)}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='text-xs font-semibold text-slate-700'>
                {route.endLocationType === 'DEPOT' ? '🏭 Depot' : '🎓 School'}
              </p>
              <p className='font-medium'>{route.endLocationName}</p>
              <p className='text-xs text-slate-500'>Route end</p>
            </div>
          </Popup>
        </Marker>
      )}

      {/* Numbered middle-stop markers (indigo, size 26 — consistent with planning map) */}
      {sortedMiddleStops.map((stop, idx) => (
        <Marker
          key={`stop-${stop.id}`}
          position={[
            stop.pickupPointLatitude as number,
            stop.pickupPointLongitude as number,
          ]}
          icon={createStopNumberIcon(idx + 1, 26)}
          zIndexOffset={500}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='text-xs font-semibold text-indigo-700'>
                Stop {idx + 1}
              </p>
              <p className='font-medium'>
                {stop.displayName ?? stop.pickupPointName ?? `Stop #${stop.id}`}
              </p>
              <p className='text-xs text-slate-500'>
                {stop.estimatedStudentCount ?? 0} student(s)
              </p>
            </div>
          </Popup>
        </Marker>
      ))}

      {/* Route polyline — real road geometry if available, else straight-line fallback */}
      {resolvedLine.length >= 2 && (
        <Polyline
          positions={resolvedLine}
          color={isFallback ? '#f59e0b' : lineColor}
          weight={isFallback ? 3 : 4}
          opacity={0.85}
          dashArray={isFallback ? '10 6' : undefined}
        />
      )}
    </>
  );

  const shellContent = (
    <>
      <FitBounds positions={allPositions} fitKey={fitKey} />
      <MapInvalidator trigger={isExpanded ? 1 : expandKey} />
      {markerContent}
    </>
  );

  const fallbackBadge = isFallback && (
    <div className='pointer-events-none absolute bottom-3 left-1/2 z-[1000] -translate-x-1/2'>
      <span className='inline-flex items-center gap-1.5 rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-xs font-medium text-amber-700 shadow-sm'>
        ⚠ Straight-line estimate — road geometry unavailable
      </span>
    </div>
  );

  if (isExpanded) {
    return (
      <div className='relative h-full w-full'>
        <LeafletMapShell
          center={initialCenter}
          zoom={initialZoom}
          className='h-full w-full'
        >
          {shellContent}
        </LeafletMapShell>
        {fallbackBadge}
      </div>
    );
  }

  return (
    <div className='relative h-full w-full'>
      <LeafletMapShell
        center={initialCenter}
        zoom={initialZoom}
        className={className ?? 'h-full w-full'}
      >
        {shellContent}
      </LeafletMapShell>
      {fallbackBadge}
    </div>
  );
}
