import type { TransportPlanResult } from '../../../types';

export interface EditableStop {
  locationCode: string;
  action: string;
  arrivalTime: string;
  departureTime: string;
  travelTime: number;
  requestId: number | null;
}

export interface EditableRoute {
  truckCode: string;
  depotTruckCode: string;
  stops: EditableStop[];
  totalTravelTime: number;
  driverId?: number;
}

export interface RouteValidation {
  isValid: boolean;
  errors: string[];
  warnings: string[];
}

export interface TransportPlanOutput {
  generatedAt: string;
  statistics: {
    totalTrucks: number;
    totalOperationalStops: number;
    unscheduledCount: number;
  };
  plans: {
    truckCode: string;
    driverId?: number;
    stops: {
      sequence: number;
      locationCode: string;
      action: string;
      plannedArrival: string;
      plannedDeparture: string;
      requestId: number | null;
    }[];
    totalTravelTimeSeconds: number;
  }[];
}

export function isDepotStop(stop: EditableStop): boolean {
  const action = stop.action.toLowerCase();
  return (
    action.includes('depot') ||
    action === 'start' ||
    action === 'end' ||
    action.includes('_start') ||
    action.includes('_end')
  );
}

export function initEditableRoutes(result: TransportPlanResult): EditableRoute[] {
  return (result.truckRoutes ?? []).map((route) => ({
    truckCode: route.truck?.code ?? '',
    depotTruckCode: route.truck?.depotTruckCode ?? '',
    totalTravelTime: route.travelTime ?? 0,
    stops: (route.nodes ?? []).map((node) => ({
      locationCode: node.locationCode,
      action: node.action,
      arrivalTime: node.arrivalTime ?? '',
      departureTime: node.departureTime ?? '',
      travelTime: node.travelTime ?? 0,
      requestId: node.requestId ?? null,
    })),
  }));
}

export function validateRoute(route: EditableRoute): RouteValidation {
  const errors: string[] = [];
  const warnings: string[] = [];

  if (!route.driverId) {
    errors.push('No driver assigned');
  }

  const nonDepotStops = route.stops.filter((stop) => !isDepotStop(stop));
  if (nonDepotStops.length === 0) {
    errors.push('Route has no operational stops assigned');
  }

  for (let i = 0; i < route.stops.length; i += 1) {
    const stop = route.stops[i];
    if (
      stop.arrivalTime &&
      stop.departureTime &&
      stop.departureTime < stop.arrivalTime
    ) {
      warnings.push(
        `Stop ${i + 1} (${stop.locationCode}): departure before arrival`
      );
    }

    if (i > 0) {
      const prev = route.stops[i - 1];
      if (
        prev.departureTime &&
        stop.arrivalTime &&
        stop.arrivalTime < prev.departureTime
      ) {
        warnings.push(
          `Stop ${i + 1} (${stop.locationCode}): arrives before previous stop departs`
        );
      }
    }
  }

  return { isValid: errors.length === 0, errors, warnings };
}

export function generatePlanOutput(
  routes: EditableRoute[],
  unscheduledCount: number
): TransportPlanOutput {
  return {
    generatedAt: new Date().toISOString().replace('T', ' ').slice(0, 19),
    statistics: {
      totalTrucks: routes.length,
      totalOperationalStops: routes.reduce(
        (acc, route) => acc + route.stops.filter((stop) => !isDepotStop(stop)).length,
        0
      ),
      unscheduledCount,
    },
    plans: routes.map((route) => ({
      truckCode: route.truckCode,
      driverId: route.driverId,
      stops: route.stops.map((stop, idx) => ({
        sequence: idx + 1,
        locationCode: stop.locationCode,
        action: stop.action,
        plannedArrival: stop.arrivalTime,
        plannedDeparture: stop.departureTime,
        requestId: stop.requestId,
      })),
      totalTravelTimeSeconds: route.totalTravelTime,
    })),
  };
}
