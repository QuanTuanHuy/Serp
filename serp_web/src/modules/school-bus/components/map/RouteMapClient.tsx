'use client';

import * as React from 'react';
import { CircleMarker, Polyline, Popup, useMap } from 'react-leaflet';
import { latLngBounds } from 'leaflet';
import { LeafletMapShell } from '@/shared/components/map/LeafletMapShell';
import { cn } from '@/shared/utils';
import {
  SCHOOL_BUS_MAP_DEFAULT_CENTER,
  SCHOOL_BUS_MAP_DEFAULT_ZOOM,
  SCHOOL_BUS_MAP_DETAIL_ZOOM,
} from '../../constants';
import { schoolBusBrand, schoolBusUi } from '../../theme';
import type { SchoolBusRoute, SchoolBusRouteStop } from '../../types';

interface RouteMapClientProps {
  route: Pick<SchoolBusRoute, 'schoolName' | 'schoolLatitude' | 'schoolLongitude'>;
  stops: SchoolBusRouteStop[];
  className?: string;
}

export default function RouteMapClient({
  route,
  stops,
  className,
}: RouteMapClientProps) {
  const plottedStops = stops
    .filter(
      (stop) =>
        typeof stop.pickupPointLatitude === 'number' &&
        typeof stop.pickupPointLongitude === 'number'
    )
    .sort((left, right) => left.stopOrder - right.stopOrder);

  const lineCoordinates = [
    ...(typeof route.schoolLatitude === 'number' &&
    typeof route.schoolLongitude === 'number'
      ? ([[route.schoolLatitude, route.schoolLongitude]] as [number, number][])
      : []),
    ...plottedStops.map((stop) => [
      stop.pickupPointLatitude as number,
      stop.pickupPointLongitude as number,
    ] as [number, number]),
  ];

  return (
    <LeafletMapShell
      center={[
        SCHOOL_BUS_MAP_DEFAULT_CENTER.lat,
        SCHOOL_BUS_MAP_DEFAULT_CENTER.lng,
      ]}
      zoom={SCHOOL_BUS_MAP_DEFAULT_ZOOM}
      className={cn('h-[420px] w-full', schoolBusUi.mapFrame, className)}
    >
      <RouteViewport coordinates={lineCoordinates} />

      {typeof route.schoolLatitude === 'number' &&
      typeof route.schoolLongitude === 'number' ? (
        <CircleMarker
          center={[route.schoolLatitude, route.schoolLongitude]}
          radius={11}
          pathOptions={{
            color: schoolBusBrand.roseDark,
            fillColor: schoolBusBrand.rose,
            fillOpacity: 0.85,
          }}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='font-medium text-slate-950'>{route.schoolName}</p>
              <p className='text-xs text-slate-500'>School hub</p>
            </div>
          </Popup>
        </CircleMarker>
      ) : null}

      {plottedStops.map((stop) => (
        <CircleMarker
          key={stop.id}
          center={[
            stop.pickupPointLatitude as number,
            stop.pickupPointLongitude as number,
          ]}
          radius={8}
          pathOptions={{
            color: schoolBusBrand.sky,
            fillColor: '#7dd3fc',
            fillOpacity: 0.85,
          }}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='font-medium text-slate-950'>
                {stop.stopOrder}. {stop.pickupPointName}
              </p>
              <p className='text-xs text-slate-500'>
                {stop.pickupPointAddress || 'No address'}
              </p>
            </div>
          </Popup>
        </CircleMarker>
      ))}

      {lineCoordinates.length >= 2 ? (
        <Polyline
          positions={lineCoordinates}
          pathOptions={{ color: schoolBusBrand.rose, weight: 4 }}
        />
      ) : null}
    </LeafletMapShell>
  );
}

function RouteViewport({
  coordinates,
}: {
  coordinates: [number, number][];
}) {
  const map = useMap();

  React.useEffect(() => {
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

    map.fitBounds(latLngBounds(coordinates), { padding: [36, 36] });
  }, [coordinates, map]);

  return null;
}
