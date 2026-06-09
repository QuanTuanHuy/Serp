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
import type { TripAttendanceStopItem } from '../../types';
import { createSchoolBusMarkerIcon, createStopNumberIcon } from './mapIcons';

// ── Auto-fit bounds ───────────────────────────────────────────────────────────
interface FitBoundsProps {
  positions: [number, number][];
}
function FitBounds({ positions }: FitBoundsProps) {
  const map = useMap();
  useEffect(() => {
    if (positions.length === 0) return;
    if (positions.length === 1) {
      map.setView(positions[0], SCHOOL_BUS_MAP_DETAIL_ZOOM);
    } else {
      map.fitBounds(positions, { padding: [48, 48] });
    }
  }, [map, positions]);
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

interface TripMapClientProps {
  stops: TripAttendanceStopItem[];
  tripStatus: string;
  isOutbound: boolean;
  routeGeometry?: string | null;
  routePath?: any;
  className?: string;
}

export default function TripMapClient({
  stops = [],
  tripStatus,
  isOutbound,
  routeGeometry,
  routePath,
  className,
}: TripMapClientProps) {
  const { isExpanded, expandKey } = useMapExpand();

  // Filter stops that have coordinates
  const validStops = [...stops]
    .filter(
      (s) =>
        typeof s.latitude === 'number' &&
        typeof s.longitude === 'number'
    )
    .sort((a, b) => a.stopOrder - b.stopOrder);

  const allPositions: [number, number][] = validStops.map((s) => [
    s.latitude as number,
    s.longitude as number,
  ]);

  // Real road geometry path parsing
  let actualPathCoords: [number, number][] = [];
  let isFallback = true;

  if (routePath?.coordinates && Array.isArray(routePath.coordinates)) {
    actualPathCoords = routePath.coordinates
      .filter((c: any) => typeof c.latitude === 'number' && typeof c.longitude === 'number')
      .map((c: any) => [c.latitude, c.longitude] as [number, number]);
    
    if (actualPathCoords.length >= 2) {
      isFallback = false;
    }
  }

  if (actualPathCoords.length < 2 && routeGeometry) {
    try {
      const parsed = JSON.parse(routeGeometry);
      if (parsed && Array.isArray(parsed.coordinates)) {
        actualPathCoords = parsed.coordinates
          .filter((c: any) => typeof c.latitude === 'number' && typeof c.longitude === 'number')
          .map((c: any) => [c.latitude, c.longitude] as [number, number]);
        
        if (actualPathCoords.length >= 2) {
          isFallback = parsed.fallbackUsed === true || parsed.geometrySource === 'STRAIGHT_LINE_ESTIMATE';
        }
      }
    } catch (e) {
      console.warn('Failed to parse routeGeometry JSON on trip map:', e);
    }
  }

  const resolvedLine: [number, number][] = actualPathCoords.length >= 2 ? actualPathCoords : allPositions;

  // Determine current and next stop based on rules
  const normalizedStatus = (tripStatus || '').toUpperCase();
  const tripIsCompleted = normalizedStatus === 'COMPLETED';
  const tripIsActive = normalizedStatus === 'IN_PROGRESS';
  const tripNotStarted = normalizedStatus === 'CREATED' || normalizedStatus === 'PLANNED';

  let currentStopIndex = -1;
  let nextStopIndex = -1;

  if (tripNotStarted && validStops.length > 0) {
    currentStopIndex = 0;
    if (validStops.length > 1) {
      nextStopIndex = 1;
    }
  } else if (tripIsCompleted && validStops.length > 0) {
    currentStopIndex = validStops.length - 1;
  } else if (tripIsActive && validStops.length > 0) {
    // Current stop = stop currently ARRIVED or BOARDING
    const activeIdx = validStops.findIndex(
      (s) => s.stopStatus === 'ARRIVED' || s.stopStatus === 'BOARDING'
    );
    if (activeIdx !== -1) {
      currentStopIndex = activeIdx;
    } else {
      // If none arrived/boarding, find first non-done
      const firstNonDone = validStops.findIndex(
        (s) => s.stopStatus !== 'DEPARTED' && s.stopStatus !== 'SKIPPED'
      );
      if (firstNonDone !== -1) {
        currentStopIndex = Math.max(0, firstNonDone - 1); // approximate last departed as current
      }
    }

    // Next stop = first PENDING stop
    nextStopIndex = validStops.findIndex((s) => s.stopStatus === 'PENDING');
  }

  const defaultCenter: [number, number] = [
    SCHOOL_BUS_MAP_DEFAULT_CENTER.lat,
    SCHOOL_BUS_MAP_DEFAULT_CENTER.lng,
  ];
  const initialCenter =
    allPositions.length > 0 ? allPositions[0] : defaultCenter;
  const initialZoom =
    allPositions.length > 0 ? SCHOOL_BUS_MAP_DETAIL_ZOOM : SCHOOL_BUS_MAP_DEFAULT_ZOOM;

  const lineColor = isOutbound ? '#0284c7' : '#059669';

  return (
    <div className='relative h-full w-full'>
      <LeafletMapShell
        center={initialCenter}
        zoom={initialZoom}
        className={className ?? 'h-[420px] w-full rounded-2xl overflow-hidden border border-slate-200'}
      >
        <FitBounds positions={allPositions} />
        <MapInvalidator trigger={isExpanded ? 1 : expandKey} />

        {/* Draw route polyline */}
        {resolvedLine.length >= 2 && (
          <Polyline
            positions={resolvedLine}
            color={lineColor}
            weight={4}
            opacity={0.85}
          />
        )}

        {/* Draw markers */}
        {validStops.map((stop, idx) => {
          const isCurrent = idx === currentStopIndex;
          const isNext = idx === nextStopIndex;

          const isDepot = stop.locationType === 'DEPOT';
          const isSchool = stop.locationType === 'SCHOOL';

          let icon;
          if (isCurrent && tripIsActive) {
            // Render bus icon at current active stop
            icon = createSchoolBusMarkerIcon('bus', 34);
          } else if (isSchool) {
            icon = createSchoolBusMarkerIcon('school', 30);
          } else if (isDepot) {
            icon = createSchoolBusMarkerIcon('depot', 30);
          } else {
            icon = createStopNumberIcon(stop.stopOrder, 26);
          }

          // Stop purpose helper
          const purposeLabel =
            stop.stopPurpose === 'START_TERMINAL'
              ? 'Start terminal'
              : stop.stopPurpose === 'END_TERMINAL'
              ? 'End terminal'
              : stop.stopPurpose === 'PICKUP'
              ? 'Pickup stop'
              : stop.stopPurpose === 'DROPOFF'
              ? 'Drop-off stop'
              : 'Stop';

          return (
            <Marker
              key={`stop-${stop.routeStopId}`}
              position={[stop.latitude as number, stop.longitude as number]}
              icon={icon}
              zIndexOffset={isCurrent ? 1000 : isNext ? 800 : 500}
            >
              <Popup>
                <div className='space-y-1 text-xs'>
                  <p className='font-semibold text-slate-800'>
                    {isCurrent && tripIsActive && '🚌 Current stop: '}
                    {isNext && '📍 Next stop: '}
                    {stop.displayName ?? `Stop #${stop.routeStopId}`}
                  </p>
                  <p className='text-[10px] text-slate-400 font-medium'>
                    Order: #{stop.stopOrder} • {purposeLabel}
                  </p>
                  <p className='text-slate-500'>
                    Status: <span className='font-semibold'>{stop.stopStatus}</span>
                  </p>
                  {stop.plannedBoardingCount > 0 && (
                    <p className='text-blue-600'>Planned board: {stop.plannedBoardingCount} students</p>
                  )}
                  {stop.plannedDropoffCount > 0 && (
                    <p className='text-emerald-600'>Planned drop-off: {stop.plannedDropoffCount} students</p>
                  )}
                </div>
              </Popup>
            </Marker>
          );
        })}
      </LeafletMapShell>
      {isFallback && (
        <div className='pointer-events-none absolute bottom-3 left-1/2 z-[1000] -translate-x-1/2'>
          <span className='inline-flex items-center gap-1.5 rounded-full border border-slate-200 bg-white px-3 py-1 text-xs font-medium text-slate-500 shadow-sm'>
            Actual road geometry not yet computed.
          </span>
        </div>
      )}
    </div>
  );
}
