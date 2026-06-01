'use client';

import React, { useEffect } from 'react';
import { Marker, Polyline, Popup, useMap } from 'react-leaflet';
import { LeafletMapShell } from '@/shared/components/map/LeafletMapShell';
import {
  SCHOOL_BUS_MAP_DEFAULT_CENTER,
  SCHOOL_BUS_MAP_DEFAULT_ZOOM,
  SCHOOL_BUS_MAP_DETAIL_ZOOM,
} from '../../constants';
import type { SchoolBusRoutePath, SchoolBusRouteStop } from '../../types';
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

function MapInvalidator({ trigger }: { trigger?: number }) {
  const map = useMap();
  useEffect(() => {
    const t = setTimeout(() => map.invalidateSize(), 150);
    return () => clearTimeout(t);
  }, [map, trigger]);
  return null;
}

// ── Props ─────────────────────────────────────────────────────────────────────

export interface DemoMapClientProps {
  stops: SchoolBusRouteStop[];
  routePath?: SchoolBusRoutePath | null;
  busPosition?: { lat: number; lng: number } | null;
  currentStopOrder?: number | null;
  className?: string;
  fitKey?: number;
  invalidateKey?: number;
}

// ── Component ─────────────────────────────────────────────────────────────────

export default function DemoMapClient({
  stops,
  routePath,
  busPosition,
  currentStopOrder,
  className = 'h-full w-full rounded-[24px]',
  fitKey,
  invalidateKey,
}: DemoMapClientProps) {
  // Sort stops by order
  const sortedStops = [...stops].sort((a, b) => a.stopOrder - b.stopOrder);

  // Separate terminals from middle stops
  const startStop = sortedStops.find((s) => s.stopPurpose === 'START_TERMINAL');
  const endStop = sortedStops.find((s) => s.stopPurpose === 'END_TERMINAL');
  const middleStops = sortedStops.filter(
    (s) => s.stopPurpose !== 'START_TERMINAL' && s.stopPurpose !== 'END_TERMINAL'
  );

  // Coordinate helpers
  const getStopCoord = (s: SchoolBusRouteStop): [number, number] | null => {
    if (typeof s.pickupPointLatitude === 'number' && typeof s.pickupPointLongitude === 'number') {
      return [s.pickupPointLatitude, s.pickupPointLongitude];
    }
    return null;
  };

  const startCoord = startStop ? getStopCoord(startStop) : null;
  const endCoord = endStop ? getStopCoord(endStop) : null;

  // All positions for auto-fit
  const allPositions: [number, number][] = [];
  if (startCoord) allPositions.push(startCoord);
  for (const s of middleStops) {
    const c = getStopCoord(s);
    if (c) allPositions.push(c);
  }
  if (endCoord) allPositions.push(endCoord);

  // Route polyline — real geometry if available
  const actualPathCoords: [number, number][] =
    routePath?.coordinates
      ?.filter((p) => typeof p.latitude === 'number' && typeof p.longitude === 'number')
      .map((p) => [p.latitude, p.longitude] as [number, number]) ?? [];

  const fallbackLine: [number, number][] = allPositions;
  const resolvedLine = actualPathCoords.length >= 2 ? actualPathCoords : fallbackLine;

  const isFallback =
    routePath?.fallbackUsed === true ||
    routePath?.geometrySource === 'STRAIGHT_LINE_ESTIMATE' ||
    (routePath != null && actualPathCoords.length < 2);

  const defaultCenter: [number, number] = [
    SCHOOL_BUS_MAP_DEFAULT_CENTER.lat,
    SCHOOL_BUS_MAP_DEFAULT_CENTER.lng,
  ];
  const initialCenter = allPositions.length > 0 ? allPositions[0] : defaultCenter;
  const initialZoom = allPositions.length > 0 ? SCHOOL_BUS_MAP_DETAIL_ZOOM : SCHOOL_BUS_MAP_DEFAULT_ZOOM;

  // Determine icon for terminal stops
  const terminalIcon = (s: SchoolBusRouteStop): MarkerKind =>
    s.locationType === 'DEPOT' ? 'depot' : 'school';

  return (
    <div className='relative h-full w-full'>
      {/* Fallback warning overlay */}
      {isFallback && resolvedLine.length >= 2 && (
        <div className='absolute left-3 top-3 z-[1000] rounded-lg border border-amber-200 bg-amber-50 px-2 py-1 text-[10px] font-medium text-amber-700 shadow-sm'>
          ⚠ Using fallback straight-line route
        </div>
      )}

      <LeafletMapShell center={initialCenter} zoom={initialZoom} className={className}>
        <FitBounds positions={allPositions} fitKey={fitKey} />
        <MapInvalidator trigger={invalidateKey} />

      {/* Start terminal */}
      {startCoord && startStop && (
        <Marker
          position={startCoord}
          icon={createSchoolBusMarkerIcon(terminalIcon(startStop), 30)}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='text-xs font-semibold text-amber-700'>
                {startStop.locationType === 'DEPOT' ? '🏭 Depot' : '🎓 School'}
              </p>
              <p className='font-medium'>{startStop.displayName || startStop.depotName || startStop.schoolName}</p>
              <p className='text-xs text-slate-500'>Route start</p>
            </div>
          </Popup>
        </Marker>
      )}

      {/* End terminal */}
      {endCoord && endStop && (
        <Marker
          position={endCoord}
          icon={createSchoolBusMarkerIcon(terminalIcon(endStop), 30)}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='text-xs font-semibold text-slate-700'>
                {endStop.locationType === 'DEPOT' ? '🏭 Depot' : '🎓 School'}
              </p>
              <p className='font-medium'>{endStop.displayName || endStop.depotName || endStop.schoolName}</p>
              <p className='text-xs text-slate-500'>Route end</p>
            </div>
          </Popup>
        </Marker>
      )}

      {/* Middle stop markers — numbered, highlight current */}
      {middleStops.map((stop, idx) => {
        const coord = getStopCoord(stop);
        if (!coord) return null;
        const isCurrent = currentStopOrder != null && stop.stopOrder === currentStopOrder;
        return (
          <Marker
            key={`stop-${stop.id}`}
            position={coord}
            icon={createStopNumberIcon(idx + 1, isCurrent ? 32 : 26)}
            zIndexOffset={isCurrent ? 1000 : 500}
          >
            <Popup>
              <div className='space-y-1'>
                <p className='text-xs font-semibold text-indigo-700'>
                  Stop {stop.stopOrder}{isCurrent ? ' (current)' : ''}
                </p>
                <p className='font-medium'>
                  {stop.displayName || stop.pickupPointName || `Stop #${stop.id}`}
                </p>
                <p className='text-xs text-slate-500'>
                  {stop.stopPurpose} • {stop.locationType}
                </p>
                {stop.estimatedStudentCount != null && (
                  <p className='text-xs text-slate-500'>
                    {stop.estimatedStudentCount} student(s)
                  </p>
                )}
              </div>
            </Popup>
          </Marker>
        );
      })}

      {/* Route polyline */}
      {resolvedLine.length >= 2 && (
        <Polyline
          positions={resolvedLine}
          color={isFallback ? '#f59e0b' : '#0284c7'}
          weight={isFallback ? 3 : 4}
          opacity={0.85}
          dashArray={isFallback ? '10 6' : undefined}
        />
      )}

      {/* Bus marker */}
      {busPosition && (
        <Marker
          position={[busPosition.lat, busPosition.lng]}
          icon={createSchoolBusMarkerIcon('bus', 36)}
          zIndexOffset={2000}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='text-xs font-semibold text-indigo-700'>🚌 Demo Bus</p>
              <p className='text-xs text-slate-500'>
                {busPosition.lat.toFixed(5)}, {busPosition.lng.toFixed(5)}
              </p>
            </div>
          </Popup>
        </Marker>
      )}
      </LeafletMapShell>
    </div>
  );
}
