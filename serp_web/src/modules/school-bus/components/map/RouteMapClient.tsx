'use client';

import * as React from 'react';
import { Marker, Polyline, Popup, useMap } from 'react-leaflet';
import { latLngBounds } from 'leaflet';
import { LeafletMapShell } from '@/shared/components/map/LeafletMapShell';
import { cn } from '@/shared/utils';
import {
  SCHOOL_BUS_MAP_DEFAULT_CENTER,
  SCHOOL_BUS_MAP_DEFAULT_ZOOM,
  SCHOOL_BUS_MAP_DETAIL_ZOOM,
} from '../../constants';
import { schoolBusBrand, schoolBusUi } from '../../theme';
import type {
  SchoolBusRoute,
  SchoolBusRouteAssignment,
  SchoolBusRouteStop,
} from '../../types';
import { createDirectionArrowIcon, createSchoolBusMarkerIcon } from './mapIcons';

interface RouteMapClientProps {
  route: SchoolBusRoute;
  stops: SchoolBusRouteStop[];
  assignment?: SchoolBusRouteAssignment | null;
  className?: string;
}

export default function RouteMapClient({
  route,
  stops,
  assignment,
  className,
}: RouteMapClientProps) {
  const plottedStops = stops
    .filter(
      (stop) =>
        typeof stop.pickupPointLatitude === 'number' &&
        typeof stop.pickupPointLongitude === 'number'
    )
    .sort((left, right) => left.stopOrder - right.stopOrder);

  const startCoordinate =
    typeof route.startLocationLatitude === 'number' &&
    typeof route.startLocationLongitude === 'number'
      ? ([route.startLocationLatitude, route.startLocationLongitude] as [
          number,
          number,
        ])
      : null;
  const endCoordinate =
    typeof route.endLocationLatitude === 'number' &&
    typeof route.endLocationLongitude === 'number'
      ? ([route.endLocationLatitude, route.endLocationLongitude] as [
          number,
          number,
        ])
      : null;
  const lineCoordinates = [
    ...(startCoordinate ? [startCoordinate] : []),
    ...plottedStops.map((stop) => [
      stop.pickupPointLatitude as number,
      stop.pickupPointLongitude as number,
    ] as [number, number]),
    ...(endCoordinate ? [endCoordinate] : []),
  ];
  const routeColor =
    route.routeDirection === 'RETURN'
      ? schoolBusBrand.emerald
      : schoolBusBrand.rose;
  const isReturn = route.routeDirection === 'RETURN';
  const arrowSegments = lineCoordinates
    .map((point, index) => ({ point, index }))
    .slice(1)
    .map(({ point, index }) => {
      const prev = lineCoordinates[index - 1];
      const midLat = (prev[0] + point[0]) / 2;
      const midLng = (prev[1] + point[1]) / 2;
      const angle =
        (Math.atan2(point[0] - prev[0], point[1] - prev[1]) * 180) / Math.PI +
        90;
      return {
        id: `${index}-${midLat}-${midLng}`,
        position: [midLat, midLng] as [number, number],
        angle,
      };
    });
  const busPosition = startCoordinate
    ? ([startCoordinate[0] + 0.00022, startCoordinate[1] + 0.00022] as [
        number,
        number,
      ])
    : null;

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

      {startCoordinate ? (
        <Marker
          position={startCoordinate}
          icon={createSchoolBusMarkerIcon(
            route.startLocationType === 'DEPOT' ? 'depot' : 'start',
            30
          )}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='font-medium text-slate-950'>
                {route.startLocationName}
              </p>
              <p className='text-xs text-slate-500'>
                Start - {route.startLocationType}
              </p>
            </div>
          </Popup>
        </Marker>
      ) : null}

      {endCoordinate ? (
        <Marker
          position={endCoordinate}
          icon={createSchoolBusMarkerIcon(
            route.endLocationType === 'DEPOT' ? 'depot' : 'end',
            30
          )}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='font-medium text-slate-950'>
                {route.endLocationName}
              </p>
              <p className='text-xs text-slate-500'>
                End - {route.endLocationType}
              </p>
            </div>
          </Popup>
        </Marker>
      ) : null}

      {plottedStops.map((stop) => (
        <Marker
          key={stop.id}
          position={[
            stop.pickupPointLatitude as number,
            stop.pickupPointLongitude as number,
          ]}
          icon={createSchoolBusMarkerIcon(
            stop.stopType === 'DROPOFF' ? 'dropoff' : 'pickup'
          )}
        >
          <Popup>
            <div className='space-y-1'>
              <p className='font-medium text-slate-950'>
                {stop.stopOrder}. {stop.pickupPointName}
              </p>
              <p className='text-xs font-medium text-sky-700'>
                {stop.stopType === 'DROPOFF' ? 'Drop-off stop' : 'Pickup stop'}
              </p>
              <p className='text-xs text-slate-500'>
                {stop.pickupPointAddress || 'No address'}
              </p>
            </div>
          </Popup>
        </Marker>
      ))}

      {assignment && busPosition ? (
        <Marker position={busPosition} icon={createSchoolBusMarkerIcon('bus', 26)}>
          <Popup>
            <div className='space-y-1'>
              <p className='font-medium text-slate-950'>
                Bus {assignment.busPlateNumber}
              </p>
              <p className='text-xs text-slate-500'>
                Driver: {assignment.driverName}
              </p>
            </div>
          </Popup>
        </Marker>
      ) : null}

      {lineCoordinates.length >= 2 ? (
        <Polyline
          positions={lineCoordinates}
          pathOptions={{
            color: routeColor,
            weight: 4,
            dashArray: isReturn ? '10 14' : undefined,
          }}
        />
      ) : null}

      {arrowSegments.map((arrow) => (
        <Marker
          key={arrow.id}
          position={arrow.position}
          icon={createDirectionArrowIcon(routeColor, arrow.angle)}
          interactive={false}
        />
      ))}
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
