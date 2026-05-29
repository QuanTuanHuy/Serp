'use client';

import { useEffect, useMemo, useRef, useState } from 'react';
import dynamic from 'next/dynamic';
import { useRouter } from 'next/navigation';
import {
  AlertCircle,
  ArrowRight,
  CheckCircle2,
  Loader2,
  MoveRight,
  Route,
  Truck,
} from 'lucide-react';
import {
  DragDropContext,
  Draggable,
  Droppable,
  type DropResult,
} from '@hello-pangea/dnd';
import { toast } from 'sonner';
import { AccessDenied } from '@/modules/account/components';
import { selectUserProfile } from '@/modules/account/store';
import { useAppSelector } from '@/shared/hooks';
import {
  Badge,
  Button,
  Card,
  CardContent,
  Checkbox,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import {
  useCreateManualRouteMutation,
  useGetDispatcherContainersQuery,
  useGetDispatcherDriversQuery,
  useGetDispatcherLocationsQuery,
  useGetDispatcherRequestsQuery,
  useGetDispatcherTrailersQuery,
  useGetDispatcherTrucksQuery,
} from '../../api/ttcrsApi';
import type {
  ContainerItem,
  DriverItem,
  RequestType,
  TrailerItem,
  TruckItem,
  TtcrsRequest,
} from '../../types';
import type { RoutePolylineData } from '../../components/RouteMapPanel';
import { cn } from '@/shared/utils';

type WizardStep = 1 | 2;

type LogicalAction =
  | 'DEPOT_START'
  | 'DEPOT_END'
  | 'PICKUP_TRAILER'
  | 'DROP_TRAILER'
  | 'PICKUP_CONTAINER'
  | 'DELIVERY_CONTAINER';

interface ManualStopCard {
  id: string;
  title: string;
  description: string;
  locationCode: string;
  logicalAction: LogicalAction;
  apiAction: string;
  requestId: number | null;
  trailerId: number | null;
  earliestAt: string | null;
  lateAt: string | null;
}

interface ManualSelection {
  truck: TruckItem;
  startTrailer: TrailerItem;
  driver: DriverItem;
  returnDepotTruck: string;
  returnDepotTrailer: string;
  requests: TtcrsRequest[];
  replacementTrailerByRequest: Record<number, TrailerItem>;
  containerByRequest: Record<number, ContainerItem>;
}

interface RouteValidationResult {
  errors: string[];
}

const DATE_FMT = new Intl.DateTimeFormat('vi-VN', {
  hour: '2-digit',
  minute: '2-digit',
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});

const TYPE_CLASS: Record<RequestType, string> = {
  OF: 'bg-green-500 hover:bg-green-500/90 text-white border-transparent',
  IF: 'bg-blue-500 hover:bg-blue-500/90 text-white border-transparent',
  OE: 'bg-amber-500 hover:bg-amber-500/90 text-white border-transparent',
  IE: 'bg-purple-500 hover:bg-purple-500/90 text-white border-transparent',
};

const DynamicRouteMapPanel = dynamic(
  () =>
    import('../../components/RouteMapPanel').then((module) => ({
      default: module.RouteMapPanel,
    })),
  {
    ssr: false,
    loading: () => (
      <div className='flex h-full w-full items-center justify-center rounded-lg bg-muted/30 animate-pulse'>
        <span className='text-xs text-muted-foreground'>Loading map...</span>
      </div>
    ),
  }
);

function formatDateTime(value: string | null) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return DATE_FMT.format(date);
}

function toDbDateTime(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate()
  )} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function parseDateTimeInput(value: string): Date | null {
  if (!value) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

function computeLatestFirstOperationalDeadline(requests: TtcrsRequest[]) {
  const candidates = requests
    .flatMap((request) => [request.lateAtSrc, request.lateAtDest])
    .filter((value): value is string => Boolean(value))
    .map((value) => new Date(value))
    .filter((date) => !Number.isNaN(date.getTime()));
  if (candidates.length === 0) return null;
  return new Date(Math.min(...candidates.map((date) => date.getTime())));
}

function computeEarliestFirstOperationalTime(requests: TtcrsRequest[]) {
  const candidates = requests
    .flatMap((request) => [request.earlyAtSrc, request.earlyAtDest])
    .filter((value): value is string => Boolean(value))
    .map((value) => new Date(value))
    .filter((date) => !Number.isNaN(date.getTime()));
  if (candidates.length === 0) return null;
  return new Date(Math.max(...candidates.map((date) => date.getTime())));
}

function logicalActionClass(action: LogicalAction): string {
  switch (action) {
    case 'DEPOT_START':
    case 'DEPOT_END':
      return 'bg-slate-100 text-slate-700 border-slate-200';
    case 'PICKUP_TRAILER':
    case 'DROP_TRAILER':
      return 'bg-blue-100 text-blue-700 border-blue-200';
    case 'PICKUP_CONTAINER':
      return 'bg-emerald-100 text-emerald-700 border-emerald-200';
    case 'DELIVERY_CONTAINER':
      return 'bg-orange-100 text-orange-700 border-orange-200';
  }
}

function getTimeWindowLabel(card: ManualStopCard) {
  const lines: string[] = [];

  if (card.earliestAt) {
    lines.push(`Earliest: ${formatDateTime(card.earliestAt)}`);
  }
  if (card.lateAt) {
    lines.push(`Latest: ${formatDateTime(card.lateAt)}`);
  }

  return lines.length > 0 ? lines : ['No time window'];
}

function buildStopCards(selection: ManualSelection): {
  cards: Record<string, ManualStopCard>;
  routeIds: string[];
  poolIds: string[];
} {
  const cards: Record<string, ManualStopCard> = {};

  const addCard = (card: ManualStopCard) => {
    cards[card.id] = card;
  };

  addCard({
    id: 'depot-truck-start',
    title: 'Depot Truck Start',
    description: `Truck ${selection.truck.code}`,
    locationCode: selection.truck.currentLocationCode!,
    logicalAction: 'DEPOT_START',
    apiAction: 'START_TRUCK',
    requestId: null,
    trailerId: null,
    earliestAt: null,
    lateAt: null,
  });

  addCard({
    id: 'pickup-start-trailer',
    title: 'Pickup Start Trailer',
    description: `Trailer ${selection.startTrailer.code}`,
    locationCode: selection.startTrailer.currentLocationCode!,
    logicalAction: 'PICKUP_TRAILER',
    apiAction: 'PICKUP_MOOC',
    requestId: null,
    trailerId: selection.startTrailer.id,
    earliestAt: null,
    lateAt: null,
  });

  addCard({
    id: 'drop-end-trailer',
    title: 'Return Start Trailer',
    description: `Trailer ${selection.startTrailer.code}`,
    locationCode: selection.returnDepotTrailer,
    logicalAction: 'DROP_TRAILER',
    apiAction: 'DELIVERY_MOOC',
    requestId: null,
    trailerId: null,
    earliestAt: null,
    lateAt: null,
  });

  addCard({
    id: 'depot-truck-end',
    title: 'Depot Truck End',
    description: `Truck ${selection.truck.code}`,
    locationCode: selection.returnDepotTruck,
    logicalAction: 'DEPOT_END',
    apiAction: 'END_TRUCK',
    requestId: null,
    trailerId: null,
    earliestAt: null,
    lateAt: null,
  });

  selection.requests.forEach((request) => {
    const selectedContainer = selection.containerByRequest[request.id];
    const sourceLocationCode =
      request.type === 'OE' && selectedContainer?.currentLocationCode
        ? selectedContainer.currentLocationCode
        : request.srcLocationCode ?? request.destLocationCode;
    const sourceDescription =
      request.type === 'OE' && selectedContainer
        ? `Container ${selectedContainer.code}`
        : request.srcLocationCode ?? '—';

    addCard({
      id: `req-${request.id}-src`,
      title: `Request #${request.id} - Source`,
      description: sourceDescription,
      locationCode: sourceLocationCode,
      logicalAction: 'PICKUP_CONTAINER',
      apiAction: 'PICKUP_EMPTYCONT',
      requestId: request.id,
      trailerId: null,
      earliestAt: request.earlyAtSrc,
      lateAt: request.lateAtSrc,
    });

    addCard({
      id: `req-${request.id}-dest`,
      title: `Request #${request.id} - Destination`,
      description: request.destLocationCode,
      locationCode: request.destLocationCode,
      logicalAction: 'DELIVERY_CONTAINER',
      apiAction: 'DELIVERY_EMPTYCONT',
      requestId: request.id,
      trailerId: null,
      earliestAt: request.earlyAtDest,
      lateAt: request.lateAtDest,
    });

    if (request.dropTrailerRequired) {
      const replacement = selection.replacementTrailerByRequest[request.id];
      addCard({
        id: `req-${request.id}-drop-trailer`,
        title: `Request #${request.id} - Drop Trailer`,
        description: request.destLocationCode,
        locationCode: request.destLocationCode,
        logicalAction: 'DROP_TRAILER',
        apiAction: 'DELIVERY_MOOC',
        requestId: request.id,
        trailerId: null,
        earliestAt: null,
        lateAt: null,
      });
      addCard({
        id: `req-${request.id}-pickup-trailer`,
        title: `Request #${request.id} - Pickup Replacement`,
        description: `Trailer ${replacement.code}`,
        locationCode: replacement.currentLocationCode!,
        logicalAction: 'PICKUP_TRAILER',
        apiAction: 'PICKUP_MOOC',
        requestId: request.id,
        trailerId: replacement.id,
        earliestAt: null,
        lateAt: null,
      });
    }
  });

  const routeIds = [
    'depot-truck-start',
    'pickup-start-trailer',
    'drop-end-trailer',
    'depot-truck-end',
  ];
  const poolIds = Object.keys(cards).filter((id) => !routeIds.includes(id));

  return { cards, routeIds, poolIds };
}

function validateRoute(
  routeCards: ManualStopCard[],
  selection: ManualSelection
): RouteValidationResult {
  const errors: string[] = [];

  if (routeCards.length === 0) {
    errors.push('Route is empty.');
    return { errors };
  }

  if (routeCards[0].id !== 'depot-truck-start') {
    errors.push('Departure truck depot must be the first stop.');
  }
  if (routeCards[routeCards.length - 1].id !== 'depot-truck-end') {
    errors.push('Return truck depot must be the last stop.');
  }

  if (new Set(routeCards.map((card) => card.id)).size !== routeCards.length) {
    errors.push('Invalid route sequence (duplicate stops).');
  }

  const requestById = new Map(selection.requests.map((request) => [request.id, request]));
  let currentTrailerId: number | null = null;
  const droppedTrailers = new Set<number>();
  const loadedRequests = new Set<number>();
  let hasOperationalRequestStop = false;

  routeCards.forEach((card, index) => {
    if (card.logicalAction === 'PICKUP_TRAILER') {
      if (currentTrailerId != null) {
        errors.push(
          `Stop #${index + 1}: Truck cannot pick up a trailer while another trailer is attached.`
        );
      }
      if (card.trailerId == null) {
        errors.push(`Stop #${index + 1}: Trailer pickup is missing trailer reference.`);
      } else {
        currentTrailerId = card.trailerId;
      }
    } else if (card.logicalAction === 'DROP_TRAILER') {
      if (card.id === 'drop-end-trailer') {
        if (currentTrailerId == null) {
          errors.push(`Stop #${index + 1}: Cannot return trailer when no trailer is attached.`);
        } else {
          if (droppedTrailers.has(currentTrailerId)) {
            errors.push(
              `Stop #${index + 1}: Cannot return a trailer that was already dropped at a destination.`
            );
          }
          currentTrailerId = null;
        }
      } else {
        if (currentTrailerId == null) {
          errors.push(`Stop #${index + 1}: Cannot drop trailer when no trailer is attached.`);
        } else if (card.trailerId != null && card.trailerId !== currentTrailerId) {
          errors.push(`Stop #${index + 1}: Dropped trailer does not match current trailer.`);
        } else {
          droppedTrailers.add(currentTrailerId);
          currentTrailerId = null;
        }
      }
    } else if (card.logicalAction === 'PICKUP_CONTAINER') {
      hasOperationalRequestStop = true;
      if (currentTrailerId == null) {
        errors.push(`Stop #${index + 1}: Cannot pick up container when no trailer is attached.`);
      }
      if (card.requestId == null) {
        errors.push(`Stop #${index + 1}: Container pickup is missing requestId.`);
      } else {
        if (loadedRequests.has(card.requestId)) {
          errors.push(`Stop #${index + 1}: Request #${card.requestId} has duplicate container pickup.`);
        }
        if (loadedRequests.size >= 2) {
          errors.push(`Stop #${index + 1}: Trailer capacity exceeded (max 2 containers).`);
        }
        loadedRequests.add(card.requestId);
      }
    } else if (card.logicalAction === 'DELIVERY_CONTAINER') {
      hasOperationalRequestStop = true;
      if (currentTrailerId == null) {
        errors.push(`Stop #${index + 1}: Cannot deliver container when no trailer is attached.`);
      }
      if (card.requestId == null) {
        errors.push(`Stop #${index + 1}: Container delivery is missing requestId.`);
      } else if (!loadedRequests.has(card.requestId)) {
        errors.push(`Stop #${index + 1}: Delivering container for Request #${card.requestId} before pickup.`);
      } else {
        loadedRequests.delete(card.requestId);
      }
      const request = card.requestId != null ? requestById.get(card.requestId) : undefined;
      if (request?.dropTrailerRequired) {
        if (currentTrailerId == null) {
          errors.push(
            `Stop #${index + 1}: Request #${card.requestId} requires trailer drop but no trailer is attached.`
          );
        } else {
          droppedTrailers.add(currentTrailerId);
          currentTrailerId = null;
        }
      }
    }
  });

  if (!hasOperationalRequestStop) {
    errors.push(
      'Route must include at least one valid request, not only depot start and depot end.'
    );
  }

  if (currentTrailerId != null) {
    errors.push('Route ends with a trailer still attached.');
  }

  const routeCardIds = new Set(routeCards.map((card) => card.id));
  selection.requests.forEach((request) => {
    const sourceId = `req-${request.id}-src`;
    const destId = `req-${request.id}-dest`;

    const sourceIdx = routeCards.findIndex((card) => card.id === sourceId);
    const destIdx = routeCards.findIndex((card) => card.id === destId);

    if (sourceIdx === -1 || destIdx === -1) {
      errors.push(`Request #${request.id} is missing source or destination stop in route.`);
      return;
    }
    if (sourceIdx > destIdx) {
      errors.push(`Request #${request.id} has source stop after destination stop.`);
    }
  });

  return { errors };
}

function StepIndicator({ step }: { step: WizardStep }) {
  const steps = [
    { n: 1, label: 'Select Resources' },
    { n: 2, label: 'Build Route' },
  ] as const;
  return (
    <div className='mb-6 flex items-center gap-3'>
      {steps.map((item, index) => (
        <div key={item.n} className='flex items-center gap-3'>
          {index > 0 && <div className='h-px w-14 bg-border' />}
          <div className='flex items-center gap-2'>
            <div
              className={`flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold ${
                step === item.n
                  ? 'bg-orange-600 text-white'
                  : step > item.n
                    ? 'bg-orange-600/20 text-orange-600'
                    : 'bg-muted text-muted-foreground'
              }`}
            >
              {step > item.n ? '✓' : item.n}
            </div>
            <span
              className={`text-sm font-medium ${
                step === item.n ? 'text-foreground' : 'text-muted-foreground'
              }`}
            >
              {item.label}
            </span>
          </div>
        </div>
      ))}
    </div>
  );
}

function TypeBadge({ type }: { type: RequestType }) {
  return (
    <Badge
      className={cn(
        'rounded px-2 py-0.5 text-xs font-bold tracking-wide',
        TYPE_CLASS[type] ?? 'bg-gray-400 text-white border-transparent'
      )}
    >
      {type}
    </Badge>
  );
}

export function CreateManualRoutePage() {
  const router = useRouter();
  const user = useAppSelector(selectUserProfile);
  const isDispatcher = user?.roles?.includes('TTCRS_DISPATCHER') ?? false;

  const [step, setStep] = useState<WizardStep>(1);
  const [selectedTruckId, setSelectedTruckId] = useState<number | null>(null);
  const [selectedStartTrailerId, setSelectedStartTrailerId] = useState<number | null>(null);
  const [selectedDriverId, setSelectedDriverId] = useState<number | null>(null);
  const [returnDepotTruck, setReturnDepotTruck] = useState('');
  const [returnDepotTrailer, setReturnDepotTrailer] = useState('');
  const [selectedRequestIds, setSelectedRequestIds] = useState<Set<number>>(new Set());
  const [replacementTrailerByRequest, setReplacementTrailerByRequest] = useState<
    Record<number, number>
  >({});
  const [containerByRequest, setContainerByRequest] = useState<Record<number, number>>({});

  const [cardsById, setCardsById] = useState<Record<string, ManualStopCard>>({});
  const [poolIds, setPoolIds] = useState<string[]>([]);
  const [routeIds, setRouteIds] = useState<string[]>([]);
  const [startRouteTime, setStartRouteTime] = useState('');
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [etaByCardId, setEtaByCardId] = useState<Record<string, string>>({});
  const durationCacheRef = useRef<Map<string, number>>(new Map());

  const [createManualRoute, { isLoading: isSaving }] = useCreateManualRouteMutation();

  const { data: requestsData, isLoading: isLoadingRequests } = useGetDispatcherRequestsQuery({
    statuses: ['PENDING'],
    page: 0,
    size: 200,
    sortBy: 'id',
    sortDirection: 'desc',
  });
  const { data: containersData, isLoading: isLoadingContainers } =
    useGetDispatcherContainersQuery();
  const { data: trucksData, isLoading: isLoadingTrucks } = useGetDispatcherTrucksQuery();
  const { data: trailersData, isLoading: isLoadingTrailers } = useGetDispatcherTrailersQuery();
  const { data: driversData, isLoading: isLoadingDrivers } = useGetDispatcherDriversQuery();
  const { data: locationsData } = useGetDispatcherLocationsQuery();

  const requests = requestsData?.data?.items ?? [];
  const containers = containersData?.data ?? [];
  const trucks = trucksData?.data ?? [];
  const trailers = trailersData?.data ?? [];
  const drivers = driversData?.data ?? [];
  const locations = locationsData?.data ?? [];

  const locationGeoMap = useMemo(() => {
    const map = new Map<string, { lat: number; lng: number }>();
    locations.forEach((location) => {
      if (location.lat != null && location.lng != null) {
        map.set(location.locationCode, { lat: location.lat, lng: location.lng });
      }
    });
    return map;
  }, [locations]);

  const truckDepotLocations = useMemo(
    () => locations.filter((location) => location.type === 'DEPOT_TRUCK'),
    [locations]
  );
  const trailerDepotLocations = useMemo(
    () => locations.filter((location) => location.type === 'DEPOT_TRAILER'),
    [locations]
  );
  const selectedRequests = useMemo(
    () => requests.filter((request) => selectedRequestIds.has(request.id)),
    [requests, selectedRequestIds]
  );

  const selectedTruck = useMemo(
    () => trucks.find((truck) => truck.id === selectedTruckId) ?? null,
    [trucks, selectedTruckId]
  );
  const selectedStartTrailer = useMemo(
    () => trailers.find((trailer) => trailer.id === selectedStartTrailerId) ?? null,
    [trailers, selectedStartTrailerId]
  );
  const selectedDriver = useMemo(
    () => drivers.find((driver) => driver.userId === selectedDriverId) ?? null,
    [drivers, selectedDriverId]
  );

  const replacementTrailerMap = useMemo(() => {
    const map: Record<number, TrailerItem> = {};
    selectedRequests
      .filter((request) => request.dropTrailerRequired)
      .forEach((request) => {
        const trailerId = replacementTrailerByRequest[request.id];
        const trailer = trailers.find((item) => item.id === trailerId);
        if (trailer) {
          map[request.id] = trailer;
        }
      });
    return map;
  }, [replacementTrailerByRequest, selectedRequests, trailers]);

  const containerSelectionMap = useMemo(() => {
    const map: Record<number, ContainerItem> = {};
    selectedRequests
      .filter((request) => request.type === 'OE')
      .forEach((request) => {
        const containerId = containerByRequest[request.id];
        const container = containers.find((item) => item.id === containerId);
        if (container) {
          map[request.id] = container;
        }
      });
    return map;
  }, [containerByRequest, containers, selectedRequests]);

  const latestFirstOperationalDeadline = useMemo(
    () => computeLatestFirstOperationalDeadline(selectedRequests),
    [selectedRequests]
  );

  const earliestFirstOperationalTime = useMemo(
    () => computeEarliestFirstOperationalTime(selectedRequests),
    [selectedRequests]
  );

  useEffect(() => {
    if (step === 2) {
      setValidationErrors([]);
      setEtaByCardId({});
    }
  }, [step, routeIds, startRouteTime]);

  const isStep1Loading =
    isLoadingRequests ||
    isLoadingContainers ||
    isLoadingTrucks ||
    isLoadingTrailers ||
    isLoadingDrivers;

  const handleToggleRequest = (requestId: number) => {
    setSelectedRequestIds((prev) => {
      const next = new Set(prev);
      if (next.has(requestId)) {
        next.delete(requestId);
      } else {
        next.add(requestId);
      }
      return next;
    });
  };

  const handleBuildRoute = () => {
    if (!selectedTruck || !selectedStartTrailer || !selectedDriver) {
      toast.error('Please select truck, start trailer, and driver.');
      return;
    }
    if (!selectedTruck.currentLocationCode) {
      toast.error('Selected truck has no current location.');
      return;
    }
    if (!selectedStartTrailer.currentLocationCode) {
      toast.error('Selected start trailer has no current location.');
      return;
    }
    if (!returnDepotTruck || !returnDepotTrailer) {
      toast.error('Please select return depot truck and return depot trailer.');
      return;
    }
    if (selectedRequests.length === 0) {
      toast.error('Please select at least one PENDING request.');
      return;
    }

    const requiredDropRequests = selectedRequests.filter(
      (request) => request.dropTrailerRequired
    );
    const missingReplacement = requiredDropRequests.filter(
      (request) => !replacementTrailerMap[request.id]?.currentLocationCode
    );
    if (missingReplacement.length > 0) {
      toast.error(
        `Requests requiring trailer drop are missing valid replacement trailers: ${missingReplacement
          .map((request) => `#${request.id}`)
          .join(', ')}`
      );
      return;
    }

    const missingContainerSelection = selectedRequests.filter(
      (request) =>
        request.type === 'OE' && !containerSelectionMap[request.id]?.currentLocationCode
    );
    if (missingContainerSelection.length > 0) {
      toast.error(
        `OE requests require a container selection with a valid location: ${missingContainerSelection
          .map((request) => `#${request.id}`)
          .join(', ')}`
      );
      return;
    }

    const selection: ManualSelection = {
      truck: selectedTruck,
      startTrailer: selectedStartTrailer,
      driver: selectedDriver,
      returnDepotTruck,
      returnDepotTrailer,
      requests: selectedRequests,
      replacementTrailerByRequest: replacementTrailerMap,
      containerByRequest: containerSelectionMap,
    };

    const built = buildStopCards(selection);
    setCardsById(built.cards);
    setRouteIds(built.routeIds);
    setPoolIds(built.poolIds);
    setStep(2);
  };

  const onDragEnd = (result: DropResult) => {
    const { destination, source } = result;
    if (!destination) return;

    const src = source.droppableId;
    const dst = destination.droppableId;
    if (src === dst) {
      const sourceList = src === 'pool' ? [...poolIds] : [...routeIds];
      const [moved] = sourceList.splice(source.index, 1);
      sourceList.splice(destination.index, 0, moved);
      if (src === 'pool') setPoolIds(sourceList);
      else setRouteIds(sourceList);
      return;
    }

    const sourceList = src === 'pool' ? [...poolIds] : [...routeIds];
    const destList = dst === 'pool' ? [...poolIds] : [...routeIds];
    const [moved] = sourceList.splice(source.index, 1);
    destList.splice(destination.index, 0, moved);

    if (src === 'pool') setPoolIds(sourceList);
    else setRouteIds(sourceList);
    if (dst === 'pool') setPoolIds(destList);
    else setRouteIds(destList);
  };

  const routeCards = routeIds.map((id) => cardsById[id]).filter(Boolean);
  const showMap =
    routeCards.length > 0 &&
    Object.keys(etaByCardId).length === routeCards.length &&
    validationErrors.length === 0;

  const routePolylines = useMemo<RoutePolylineData[]>(() => {
    if (!showMap) return [];

    const stopsWithCoords = routeCards
      .map((stop) => {
        const geo = locationGeoMap.get(stop.locationCode);
        if (!geo || !etaByCardId[stop.id]) {
          return null;
        }

        return {
          locationCode: stop.locationCode,
          action: stop.apiAction,
          arrivalTime: etaByCardId[stop.id],
          isDepot: stop.logicalAction === 'DEPOT_START' || stop.logicalAction === 'DEPOT_END',
          lat: geo.lat,
          lng: geo.lng,
        };
      })
      .filter((stop): stop is NonNullable<typeof stop> => stop !== null);

    return [
      {
        truckCode: selectedTruck?.code ?? 'Manual Route',
        color: '#f97316',
        coords: stopsWithCoords.map((stop) => [stop.lat, stop.lng] as [number, number]),
        stops: stopsWithCoords,
      },
    ];
  }, [showMap, routeCards, etaByCardId, locationGeoMap, selectedTruck]);

  if (!isDispatcher && user !== null) {
    return (
      <AccessDenied
        reason='authorization'
        title='Access Denied'
        description="You don't have the TTCRS_DISPATCHER role required to access this page."
        variant='minimal'
        size='md'
        showBackButton
        showHomeButton
      />
    );
  }

  const runValidateAndCalculateEta = async () => {
    if (!selectedTruck || !selectedDriver) {
      toast.error('Truck/driver information is missing.');
      return;
    }
    const selection: ManualSelection = {
      truck: selectedTruck,
      startTrailer: selectedStartTrailer!,
      driver: selectedDriver,
      returnDepotTruck,
      returnDepotTrailer,
      requests: selectedRequests,
      replacementTrailerByRequest: replacementTrailerMap,
      containerByRequest: containerSelectionMap,
    };

    const baseValidation = validateRoute(routeCards, selection);
    const errors = [...baseValidation.errors];

    const startDate = parseDateTimeInput(startRouteTime);
    if (!startDate) {
      errors.push('Please enter a valid Start Route Time.');
    }

    if (errors.length > 0) {
      setValidationErrors(errors);
      setEtaByCardId({});
      toast.error(errors[0]);
      return;
    }

    const locationMap = new Map(locations.map((location) => [location.locationCode, location]));
    const computedEta: Record<string, string> = {};
    let currentTime = new Date(startDate!);

    for (let index = 0; index < routeCards.length; index += 1) {
      const stop = routeCards[index];
      if (index === 0) {
        // first stop starts at user-defined route start time
      } else {
        const prev = routeCards[index - 1];
        const from = locationMap.get(prev.locationCode);
        const to = locationMap.get(stop.locationCode);
        if (
          from?.lat == null ||
          from?.lng == null ||
          to?.lat == null ||
          to?.lng == null
        ) {
          errors.push(
            `Missing coordinates to calculate ETA from ${prev.locationCode} -> ${stop.locationCode}.`
          );
          continue;
        }

        const key = `${from.locationCode}=>${to.locationCode}`;
        let durationSec = durationCacheRef.current.get(key);
        if (durationSec == null) {
          const url = `https://router.project-osrm.org/route/v1/driving/${from.lng},${from.lat};${to.lng},${to.lat}?overview=false`;
          try {
            const response = await fetch(url, { signal: AbortSignal.timeout(8000) });
            if (!response.ok) {
              errors.push(`OSRM returned an invalid response for leg ${key}.`);
              continue;
            }
            const json = await response.json();
            durationSec =
              json?.code === 'Ok' && json?.routes?.[0]?.duration != null
                ? Number(json.routes[0].duration)
                : undefined;
            if (durationSec == null || Number.isNaN(durationSec)) {
              errors.push(`OSRM did not return duration for leg ${key}.`);
              continue;
            }
            durationCacheRef.current.set(key, durationSec);
          } catch {
            errors.push(`Unable to call OSRM for leg ${key}.`);
            continue;
          }
        }

        currentTime = new Date(currentTime.getTime() + durationSec * 1000);
      }

      if (stop.earliestAt) {
        const earliest = new Date(stop.earliestAt);
        if (!Number.isNaN(earliest.getTime()) && currentTime < earliest) {
          currentTime = earliest;
        }
      }

      computedEta[stop.id] = toDbDateTime(currentTime);
    }

    if (errors.length > 0) {
      setValidationErrors(errors);
      setEtaByCardId({});
      toast.error(errors[0]);
      return;
    }

    setValidationErrors([]);
    setEtaByCardId(computedEta);
    toast.success('Validation passed, ETA has been calculated.');
  };

  const handleSave = async () => {
    if (!selectedTruck || !selectedDriver) return;
    if (routeCards.length === 0) {
      toast.error('Route is empty.');
      return;
    }
    if (Object.keys(etaByCardId).length !== routeCards.length) {
      toast.error('Please run Validate & Calculate ETA before saving.');
      return;
    }

    const payload = {
      plans: [
        {
          truckCode: selectedTruck.code,
          driverId: selectedDriver.userId,
          stops: (() => {
            let currentTrailerId: number | null = null;
            return routeCards.map((card, idx) => {
              let trailerId: number | null = card.trailerId ?? null;

              if (card.logicalAction === 'PICKUP_TRAILER') {
                currentTrailerId = trailerId;
              } else if (card.logicalAction === 'DROP_TRAILER') {
                trailerId = trailerId ?? currentTrailerId;
                currentTrailerId = null;
              }

              return {
                sequence: idx + 1,
                locationCode: card.locationCode,
                action: card.apiAction,
                plannedArrival: etaByCardId[card.id],
                requestId: card.requestId,
                trailerId,
              };
            });
          })(),
        },
      ],
    };

    try {
      await createManualRoute(payload).unwrap();
      toast.success('Manual route created successfully.');
      router.push('/ttcrs/dispatcher/routes');
    } catch {
      toast.error('Failed to save manual route.');
    }
  };

  return (
    <div className='flex min-h-screen flex-col overflow-x-hidden bg-background px-12 py-6'>
      <div className='mb-6 flex flex-col gap-1'>
        <h1 className='text-2xl font-bold tracking-tight text-foreground'>
          Create Manual Route
        </h1>
        <p className='text-sm text-muted-foreground'>
          Build a truck route manually with resource selection, stop ordering, validation and ETA.
        </p>
      </div>

      <StepIndicator step={step} />

      {step === 1 ? (
        <div className='space-y-4'>
          <Card>
            <CardContent className='grid grid-cols-1 gap-4 p-4 md:grid-cols-3'>
              <div className='space-y-2'>
                <Label>Truck</Label>
                <Select
                  value={selectedTruckId ? String(selectedTruckId) : ''}
                  onValueChange={(value) => setSelectedTruckId(Number(value))}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select truck' />
                  </SelectTrigger>
                  <SelectContent>
                    {trucks.map((truck) => (
                      <SelectItem
                        key={truck.id}
                        value={String(truck.id)}
                        disabled={!truck.currentLocationCode}
                      >
                        {truck.code} ({truck.status})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>Return Depot Truck</Label>
                <Select value={returnDepotTruck} onValueChange={setReturnDepotTruck}>
                  <SelectTrigger>
                    <SelectValue placeholder='Select return depot truck' />
                  </SelectTrigger>
                  <SelectContent>
                    {truckDepotLocations.map((location) => (
                      <SelectItem key={location.id} value={location.locationCode}>
                        {location.locationCode}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>Driver</Label>
                <Select
                  value={selectedDriverId ? String(selectedDriverId) : ''}
                  onValueChange={(value) => setSelectedDriverId(Number(value))}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select driver' />
                  </SelectTrigger>
                  <SelectContent>
                    {drivers.map((driver) => (
                      <SelectItem key={driver.userId} value={String(driver.userId)}>
                        {driver.name} ({driver.status})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>Start Trailer</Label>
                <Select
                  value={selectedStartTrailerId ? String(selectedStartTrailerId) : ''}
                  onValueChange={(value) => setSelectedStartTrailerId(Number(value))}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select start trailer' />
                  </SelectTrigger>
                  <SelectContent>
                    {trailers.map((trailer) => (
                      <SelectItem
                        key={trailer.id}
                        value={String(trailer.id)}
                        disabled={!trailer.currentLocationCode}
                      >
                        {trailer.code} ({trailer.status})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label>Return Depot Trailer</Label>
                <Select value={returnDepotTrailer} onValueChange={setReturnDepotTrailer}>
                  <SelectTrigger>
                    <SelectValue placeholder='Select return depot trailer' />
                  </SelectTrigger>
                  <SelectContent>
                    {trailerDepotLocations.map((location) => (
                      <SelectItem key={location.id} value={location.locationCode}>
                        {location.locationCode}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='flex items-end justify-end gap-2'>
                <Button variant='outline' onClick={() => router.back()}>
                  Cancel
                </Button>
                <Button
                  className='bg-orange-600 text-white hover:bg-orange-700'
                  onClick={handleBuildRoute}
                  disabled={isStep1Loading}
                >
                  {isStep1Loading ? (
                    <Loader2 className='h-4 w-4 animate-spin' />
                  ) : (
                    <>
                      Next
                      <ArrowRight className='ml-1 h-4 w-4' />
                    </>
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className='p-0'>
              {isLoadingRequests ? (
                <div className='flex items-center justify-center gap-2 py-12 text-muted-foreground'>
                  <Loader2 className='h-4 w-4 animate-spin' />
                  Loading requests...
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow className='bg-muted/20'>
                      <TableHead className='w-14'>Pick</TableHead>
                      <TableHead>Request</TableHead>
                      <TableHead>Origin</TableHead>
                      <TableHead>Destination</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Drop Trailer</TableHead>
                      <TableHead>Replacement Trailer</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {requests.map((request) => {
                      const checked = selectedRequestIds.has(request.id);
                      const requiresDrop = Boolean(request.dropTrailerRequired);
                      const selectedContainerId = containerByRequest[request.id]
                        ? String(containerByRequest[request.id])
                        : '';
                      return (
                        <TableRow key={request.id}>
                          <TableCell>
                            <Checkbox
                              checked={checked}
                              onCheckedChange={() => handleToggleRequest(request.id)}
                            />
                          </TableCell>
                          <TableCell className='font-medium'>
                            #{request.id}
                          </TableCell>
                          <TableCell>
                            {request.type === 'OE' ? (
                              <Select
                                value={selectedContainerId}
                                onValueChange={(value) =>
                                  setContainerByRequest((prev) => ({
                                    ...prev,
                                    [request.id]: Number(value),
                                  }))
                                }
                                disabled={!checked || isLoadingContainers}
                              >
                                <SelectTrigger className='h-8'>
                                  <SelectValue
                                    placeholder={
                                      isLoadingContainers ? 'Loading...' : 'Select container'
                                    }
                                  />
                                </SelectTrigger>
                                <SelectContent>
                                  {containers.map((container) => (
                                    <SelectItem
                                      key={container.id}
                                      value={String(container.id)}
                                      disabled={!container.currentLocationCode}
                                    >
                                      {container.code}
                                      {container.currentLocationCode
                                        ? ` (${container.currentLocationCode})`
                                        : ' (no location)'}
                                    </SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            ) : (
                              request.srcLocationCode ?? '—'
                            )}
                          </TableCell>
                          <TableCell>{request.destLocationCode}</TableCell>
                          <TableCell><TypeBadge type={request.type} /></TableCell>
                          <TableCell>
                            <Badge
                              className={
                                requiresDrop
                                  ? 'bg-amber-100 text-amber-700'
                                  : 'bg-slate-100 text-slate-700'
                              }
                            >
                              {requiresDrop ? 'Yes' : 'No'}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            {requiresDrop ? (
                              <Select
                                value={
                                  replacementTrailerByRequest[request.id]
                                    ? String(replacementTrailerByRequest[request.id])
                                    : ''
                                }
                                onValueChange={(value) =>
                                  setReplacementTrailerByRequest((prev) => ({
                                    ...prev,
                                    [request.id]: Number(value),
                                  }))
                                }
                                disabled={!checked}
                              >
                                <SelectTrigger className='h-8'>
                                  <SelectValue placeholder='Select trailer' />
                                </SelectTrigger>
                                <SelectContent>
                                  {trailers
                                    .filter((trailer) => trailer.id !== selectedStartTrailerId)
                                    .map((trailer) => (
                                      <SelectItem
                                        key={trailer.id}
                                        value={String(trailer.id)}
                                        disabled={!trailer.currentLocationCode}
                                      >
                                        {trailer.code} ({trailer.status})
                                      </SelectItem>
                                    ))}
                                </SelectContent>
                              </Select>
                            ) : (
                              <span className='text-xs text-muted-foreground'>—</span>
                            )}
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </div>
      ) : (
        <div className='space-y-4'>
          <Card>
            <CardContent className='flex flex-col gap-3 p-4 md:flex-row md:items-center md:justify-between'>
              <div className='space-y-1'>
                {/* <p className='text-sm text-muted-foreground'>
                  Earliest time to reach first operational stop (after depot):{' '}
                  <span className='font-semibold text-foreground'>
                    {earliestFirstOperationalTime
                      ? formatDateTime(earliestFirstOperationalTime.toISOString())
                      : 'N/A'}
                  </span>
                </p> */}
                <p className='text-sm text-muted-foreground'>
                  Latest deadline to reach first operational stop (after picking start trailer):{' '}
                  <span className='font-semibold text-foreground'>
                    {latestFirstOperationalDeadline
                      ? formatDateTime(latestFirstOperationalDeadline.toISOString())
                      : 'N/A'}
                  </span>
                </p>
              </div>

              <div className='flex items-center gap-2'>
                <Label htmlFor='start-route-time' className='whitespace-nowrap'>
                  Start Route Time
                </Label>
                <Input
                  id='start-route-time'
                  type='datetime-local'
                  value={startRouteTime}
                  onChange={(event) => setStartRouteTime(event.target.value)}
                  className='w-[230px]'
                />
              </div>
            </CardContent>
          </Card>

          <DragDropContext onDragEnd={onDragEnd}>
            <div className='grid grid-cols-1 gap-4 lg:grid-cols-2'>
              <Card className='min-h-[420px]'>
                <CardContent className='p-4'>
                  <h3 className='mb-3 text-sm font-semibold text-foreground'>Stop Card Pool</h3>
                  <Droppable droppableId='pool'>
                    {(provided, snapshot) => (
                      <div
                        ref={provided.innerRef}
                        {...provided.droppableProps}
                        className={`space-y-2 rounded-md border border-dashed p-2 ${
                          snapshot.isDraggingOver ? 'bg-muted/40' : 'bg-background'
                        }`}
                      >
                        {poolIds.map((id, index) => {
                          const card = cardsById[id];
                          if (!card) return null;
                          const timeWindow =
                            card.requestId != null &&
                            (card.id.endsWith('-src') || card.id.endsWith('-dest'))
                              ? getTimeWindowLabel(card)
                              : null;

                          const requiresDropAtDest =
                            card.requestId != null &&
                            card.id.endsWith('-dest') &&
                            Boolean(cardsById[`req-${card.requestId}-drop-trailer`]);

                          return (
                            <Draggable key={id} draggableId={id} index={index}>
                              {(dragProvided, dragSnapshot) => (
                                <div
                                  ref={dragProvided.innerRef}
                                  {...dragProvided.draggableProps}
                                  {...dragProvided.dragHandleProps}
                                  className={`rounded-md border bg-card p-3 ${
                                    dragSnapshot.isDragging ? 'shadow-md' : ''
                                  }`}
                                >
                                  <div className='mb-1 flex items-start justify-between gap-2'>
                                    <p className='text-sm font-medium text-foreground'>{card.title}</p>
                                    <div className='flex items-center gap-2'>
                                      <Badge className={logicalActionClass(card.logicalAction)}>
                                        {card.logicalAction}
                                      </Badge>
                                      {requiresDropAtDest && (
                                        <Badge className='bg-amber-100 text-amber-700'>
                                          Drop trailer at destination
                                        </Badge>
                                      )}
                                    </div>
                                  </div>
                                  <p className='text-xs text-muted-foreground'>
                                    Location: <span className='font-medium'>{card.locationCode}</span>
                                  </p>
                                  {timeWindow && (
                                    <div className='mt-2 space-y-0.5 text-xs text-muted-foreground'>
                                      <div className='font-semibold text-foreground'>Time window</div>
                                      {timeWindow.map((line) => (
                                        <div key={line}>{line}</div>
                                      ))}
                                    </div>
                                  )}
                                </div>
                              )}
                            </Draggable>
                          );
                        })}
                        {provided.placeholder}
                        {poolIds.length === 0 && (
                          <p className='py-10 text-center text-xs text-muted-foreground'>
                            No cards left in pool.
                          </p>
                        )}
                      </div>
                    )}
                  </Droppable>
                </CardContent>
              </Card>

              <Card className='min-h-[420px]'>
                <CardContent className='p-4'>
                  <h3 className='mb-3 text-sm font-semibold text-foreground'>
                    Route Sequence (top = first stop)
                  </h3>
                  <Droppable droppableId='route'>
                    {(provided, snapshot) => (
                      <div
                        ref={provided.innerRef}
                        {...provided.droppableProps}
                        className={`space-y-2 rounded-md border border-dashed p-2 ${
                          snapshot.isDraggingOver ? 'bg-orange-50 dark:bg-orange-950/10' : 'bg-background'
                        }`}
                      >
                        {routeIds.map((id, index) => {
                          const card = cardsById[id];
                          if (!card) return null;
                          const timeWindow =
                            card.requestId != null &&
                            (card.id.endsWith('-src') || card.id.endsWith('-dest'))
                              ? getTimeWindowLabel(card)
                              : null;

                          const requiresDropAtDest =
                            card.requestId != null &&
                            card.id.endsWith('-dest') &&
                            Boolean(cardsById[`req-${card.requestId}-drop-trailer`]);

                          return (
                            <Draggable key={id} draggableId={id} index={index}>
                              {(dragProvided, dragSnapshot) => (
                                <div
                                  ref={dragProvided.innerRef}
                                  {...dragProvided.draggableProps}
                                  {...dragProvided.dragHandleProps}
                                  className={`rounded-md border bg-card p-3 ${
                                    dragSnapshot.isDragging ? 'shadow-md' : ''
                                  }`}
                                >
                                  <div className='mb-1 flex items-start justify-between gap-2'>
                                    <p className='text-sm font-medium text-foreground'>
                                      #{index + 1} {card.title}
                                    </p>
                                    <div className='flex items-center gap-2'>
                                      <Badge className={logicalActionClass(card.logicalAction)}>
                                        {card.logicalAction}
                                      </Badge>
                                      {requiresDropAtDest && (
                                        <Badge className='bg-amber-100 text-amber-700'>
                                          Drop trailer at destination
                                        </Badge>
                                      )}
                                    </div>
                                  </div>
                                  <p className='text-xs text-muted-foreground'>
                                    {card.locationCode}
                                    {etaByCardId[card.id] ? (
                                      <>
                                        {' '}
                                        <MoveRight className='mx-1 inline h-3 w-3' />
                                        ETA: <span className='font-medium'>{etaByCardId[card.id]}</span>
                                      </>
                                    ) : null}
                                  </p>
                                  {timeWindow && (
                                    <div className='mt-2 space-y-0.5 text-xs text-muted-foreground'>
                                      <div className='font-semibold text-foreground'>Time window</div>
                                      {timeWindow.map((line) => (
                                        <div key={line}>{line}</div>
                                      ))}
                                    </div>
                                  )}
                                </div>
                              )}
                            </Draggable>
                          );
                        })}
                        {provided.placeholder}
                        {routeIds.length === 0 && (
                          <p className='py-10 text-center text-xs text-muted-foreground'>
                            Drag stop cards here to build route.
                          </p>
                        )}
                      </div>
                    )}
                  </Droppable>
                </CardContent>
              </Card>
            </div>
          </DragDropContext>

          {validationErrors.length > 0 && (
            <Card className='border-red-200 bg-red-50 dark:bg-red-950/10'>
              <CardContent className='p-4'>
                <div className='mb-2 flex items-center gap-2 text-red-700 dark:text-red-400'>
                  <AlertCircle className='h-4 w-4' />
                  <p className='text-sm font-semibold'>Validation errors</p>
                </div>
                <ul className='space-y-1 text-sm text-red-700 dark:text-red-400'>
                  {validationErrors.map((error) => (
                    <li key={error}>- {error}</li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          )}

          <div className='flex items-center justify-between'>
            <Button variant='outline' onClick={() => setStep(1)}>
              Back
            </Button>
            <div className='flex items-center gap-2'>
              <Button variant='outline' onClick={runValidateAndCalculateEta}>
                <Route className='mr-1 h-4 w-4' />
                Validate & Calculate ETA
              </Button>
              <Button
                className='bg-orange-600 text-white hover:bg-orange-700'
                onClick={handleSave}
                disabled={isSaving}
              >
                {isSaving ? (
                  <Loader2 className='mr-1 h-4 w-4 animate-spin' />
                ) : (
                  <CheckCircle2 className='mr-1 h-4 w-4' />
                )}
                Save Manual Route
              </Button>
            </div>
          </div>
        </div>
      )}

      <div className='mt-6 flex items-center gap-2 rounded-lg border bg-muted/20 px-3 py-2 text-xs text-muted-foreground'>
        <Truck className='h-4 w-4' />
        Drag & drop cards between Pool and Route to compose manual sequence.
      </div>

      {showMap && (
        <Card className='mt-6 overflow-hidden border shadow-sm'>
          <CardContent className='p-0'>
            <div className='h-[520px] w-full'>
              <DynamicRouteMapPanel routes={routePolylines} />
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}

