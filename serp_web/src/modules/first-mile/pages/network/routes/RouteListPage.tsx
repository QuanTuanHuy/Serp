/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile routes management page
 */

'use client';

import React from 'react';
import {
  Pencil,
  Plus,
  RefreshCw,
  Search,
  ShieldAlert,
  Trash2,
  X,
} from 'lucide-react';

import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
  Popover,
  PopoverContent,
  PopoverTrigger,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Textarea,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import { TmsCombobox } from '../../../components';
import {
  useCreateSecondMileRouteMutation,
  useDeleteSecondMileRouteMutation,
  useGetHubPostOfficesQuery,
  useGetHubsQuery,
  useGetPostOfficesQuery,
  useGetSecondMileHubStaffAssignmentsQuery,
  useGetSecondMileRoutesQuery,
  useGetSecondMileVehiclesQuery,
  useUpdateSecondMileRouteMutation,
} from '../../../api';
import type {
  Hub,
  PostOffice,
  SecondMileCreateRouteRequest,
  SecondMileHubStaffAssignment,
  SecondMileRoute,
  SecondMileRouteDestinationType,
  SecondMileRouteEndpointType,
  SecondMileRouteStatus,
  SecondMileUpdateRouteRequest,
  SecondMileVehicle,
} from '../../../types';
import {
  SecondMileRoutesMap,
  type RouteMapLine,
} from './components/SecondMileRoutesMap';

type RouteFormMode = 'create' | 'edit';

interface RouteFormState {
  routeName: string;
  originType: SecondMileRouteEndpointType;
  originHubId: string;
  originPostOfficeCode: string;
  destinationType: SecondMileRouteDestinationType;
  destinationHubId: string;
  destinationPostOfficeCode: string;
  vehicleId: string;
  estimatedDistanceKm: string;
  estimatedDurationMinutes: string;
  fixedDepartureTime: string;
  status: SecondMileRouteStatus;
  note: string;
}

interface RouteCoordinates {
  latitude: number;
  longitude: number;
}

interface RouteTravelMetrics {
  distanceKm: number;
  durationMinutes: number;
}

interface OsrmRouteResponse {
  code?: string;
  routes?: Array<{
    distance?: number;
    duration?: number;
  }>;
}

const PAGE_SIZE = 20;
const ALL_FILTER_VALUE = 'ALL';
const NO_VEHICLE_VALUE = '__none__';
const FALLBACK_SPEED_KMH = 40;

type RouteDestinationFilter =
  | typeof ALL_FILTER_VALUE
  | SecondMileRouteDestinationType;
type RouteOriginFilter = typeof ALL_FILTER_VALUE | SecondMileRouteEndpointType;
type RouteTableFilterKey =
  | 'routeCode'
  | 'routeName'
  | 'origin'
  | 'destination'
  | 'destinationType'
  | 'vehicle'
  | 'departure'
  | 'metrics'
  | 'status';

const DEFAULT_FORM: RouteFormState = {
  routeName: '',
  originType: 'HUB',
  originHubId: '',
  originPostOfficeCode: '',
  destinationType: 'HUB',
  destinationHubId: '',
  destinationPostOfficeCode: '',
  vehicleId: '',
  estimatedDistanceKm: '',
  estimatedDurationMinutes: '',
  fixedDepartureTime: '',
  status: 'ACTIVE',
  note: '',
};

const ROUTE_STATUS_OPTIONS: Array<{
  value: SecondMileRouteStatus;
  label: string;
}> = [
  { value: 'ACTIVE', label: 'Đang hoạt động' },
  { value: 'INACTIVE', label: 'Ngừng hoạt động' },
];

const ROUTE_DESTINATION_OPTIONS: Array<{
  value: SecondMileRouteDestinationType;
  label: string;
}> = [
  { value: 'HUB', label: 'Hub' },
  { value: 'POST_OFFICE', label: 'Bưu cục' },
];

const ROUTE_ENDPOINT_OPTIONS: Array<{
  value: SecondMileRouteEndpointType;
  label: string;
}> = [
  { value: 'HUB', label: 'Hub' },
  { value: 'POST_OFFICE', label: 'Bưu cục' },
];

function getStatusBadgeVariant(
  status: SecondMileRouteStatus
): 'default' | 'secondary' | 'outline' {
  if (status === 'ACTIVE') {
    return 'default';
  }
  return 'secondary';
}

function parseOptionalNumber(value: string): number | undefined {
  const trimmed = value.trim();
  if (!trimmed) {
    return undefined;
  }
  const numeric = Number(trimmed);
  if (!Number.isFinite(numeric)) {
    return undefined;
  }
  return numeric;
}

function parseOptionalPositiveInteger(value: string): number | undefined {
  const trimmed = value.trim();
  if (!trimmed) {
    return undefined;
  }
  const numeric = Number(trimmed);
  if (!Number.isInteger(numeric) || numeric <= 0) {
    return undefined;
  }
  return numeric;
}

function parseOptionalNonNegativeInteger(value: string): number | undefined {
  const numeric = parseOptionalNumber(value);
  if (numeric === undefined || numeric < 0 || !Number.isInteger(numeric)) {
    return undefined;
  }
  return numeric;
}

function validateOptionalNonNegativeNumber(
  label: string,
  value: string
): string | null {
  const trimmed = value.trim();
  if (!trimmed) {
    return null;
  }
  const numeric = Number(trimmed);
  if (!Number.isFinite(numeric)) {
    return `${label} phải là số hợp lệ.`;
  }
  if (numeric < 0) {
    return `${label} không được âm.`;
  }
  return null;
}

function validateOptionalNonNegativeInteger(
  label: string,
  value: string
): string | null {
  const baseError = validateOptionalNonNegativeNumber(label, value);
  if (baseError) {
    return baseError;
  }
  const trimmed = value.trim();
  if (trimmed && !Number.isInteger(Number(trimmed))) {
    return `${label} phải là số nguyên.`;
  }
  return null;
}

function toRouteCoordinates(
  value?: Pick<Hub | PostOffice, 'latitude' | 'longitude'>
): RouteCoordinates | null {
  if (
    value?.latitude === undefined ||
    value.longitude === undefined ||
    !Number.isFinite(value.latitude) ||
    !Number.isFinite(value.longitude)
  ) {
    return null;
  }
  return {
    latitude: value.latitude,
    longitude: value.longitude,
  };
}

function formatDistanceFieldValue(value: number): string {
  return String(Number(value.toFixed(1)));
}

function calculateHaversineDistanceKm(
  origin: RouteCoordinates,
  destination: RouteCoordinates
): number {
  const earthRadiusKm = 6371;
  const toRadians = (degrees: number) => (degrees * Math.PI) / 180;
  const deltaLatitude = toRadians(destination.latitude - origin.latitude);
  const deltaLongitude = toRadians(destination.longitude - origin.longitude);
  const originLatitude = toRadians(origin.latitude);
  const destinationLatitude = toRadians(destination.latitude);
  const haversine =
    Math.sin(deltaLatitude / 2) ** 2 +
    Math.cos(originLatitude) *
      Math.cos(destinationLatitude) *
      Math.sin(deltaLongitude / 2) ** 2;

  return (
    earthRadiusKm *
    2 *
    Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine))
  );
}

function estimateFallbackTravelMetrics(
  origin: RouteCoordinates,
  destination: RouteCoordinates
): RouteTravelMetrics {
  const distanceKm = calculateHaversineDistanceKm(origin, destination);
  return {
    distanceKm,
    durationMinutes:
      distanceKm > 0 ? Math.ceil((distanceKm / FALLBACK_SPEED_KMH) * 60) : 0,
  };
}

async function fetchRouteTravelMetrics(
  origin: RouteCoordinates,
  destination: RouteCoordinates,
  signal: AbortSignal
): Promise<RouteTravelMetrics> {
  const coordinates = `${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}`;
  const response = await fetch(
    `https://router.project-osrm.org/route/v1/driving/${coordinates}?overview=false`,
    { signal }
  );
  if (!response.ok) {
    throw new Error('Route metric request failed.');
  }

  const data = (await response.json()) as OsrmRouteResponse;
  const route = data.routes?.[0];
  if (
    data.code !== 'Ok' ||
    !route ||
    route.distance === undefined ||
    route.duration === undefined ||
    !Number.isFinite(route.distance) ||
    !Number.isFinite(route.duration)
  ) {
    throw new Error('Route metric response is invalid.');
  }

  return {
    distanceKm: route.distance / 1000,
    durationMinutes: Math.ceil(route.duration / 60),
  };
}

function formatStatusLabel(status: SecondMileRouteStatus): string {
  return status === 'ACTIVE' ? 'Đang hoạt động' : 'Ngừng hoạt động';
}

function formatDestinationTypeLabel(
  destinationType: SecondMileRouteDestinationType
): string {
  return destinationType === 'HUB' ? 'Hub' : 'Bưu cục';
}

function formatRouteMetric(value?: number, suffix?: string): string {
  if (value === undefined || value === null) {
    return '-';
  }
  return suffix ? `${value} ${suffix}` : String(value);
}

function toFormState(route: SecondMileRoute): RouteFormState {
  return {
    routeName: route.routeName ?? '',
    originType: route.originType ?? 'HUB',
    originHubId: route.originHubId ? String(route.originHubId) : '',
    originPostOfficeCode: route.originPostOfficeCode ?? '',
    destinationType: route.destinationType,
    destinationHubId: route.destinationHubId
      ? String(route.destinationHubId)
      : '',
    destinationPostOfficeCode: route.destinationPostOfficeCode ?? '',
    vehicleId: route.vehicleId ? String(route.vehicleId) : '',
    estimatedDistanceKm:
      route.estimatedDistanceKm !== undefined
        ? String(route.estimatedDistanceKm)
        : '',
    estimatedDurationMinutes:
      route.estimatedDurationMinutes !== undefined
        ? String(route.estimatedDurationMinutes)
        : '',
    fixedDepartureTime: route.fixedDepartureTime ?? '',
    status: route.status,
    note: route.note ?? '',
  };
}

export function RouteListPage() {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    Boolean(state.account.user.profile?.roles?.includes('TMS_ADMIN'))
  );

  const [page, setPage] = React.useState(0);
  const [routeCodeInput, setRouteCodeInput] = React.useState('');
  const [routeCode, setRouteCode] = React.useState<string | undefined>();
  const [searchInput, setSearchInput] = React.useState('');
  const [keyword, setKeyword] = React.useState<string | undefined>();
  const [selectedStatus, setSelectedStatus] = React.useState<
    typeof ALL_FILTER_VALUE | SecondMileRouteStatus
  >(ALL_FILTER_VALUE);
  const [selectedOriginType, setSelectedOriginType] =
    React.useState<RouteOriginFilter>(ALL_FILTER_VALUE);
  const [selectedOriginHubId, setSelectedOriginHubId] = React.useState('');
  const [selectedOriginPostOfficeCode, setSelectedOriginPostOfficeCode] =
    React.useState('');
  const [selectedDestinationType, setSelectedDestinationType] =
    React.useState<RouteDestinationFilter>(ALL_FILTER_VALUE);
  const [selectedDestinationHubId, setSelectedDestinationHubId] =
    React.useState('');
  const [
    selectedDestinationPostOfficeCode,
    setSelectedDestinationPostOfficeCode,
  ] = React.useState('');
  const [selectedVehicleId, setSelectedVehicleId] = React.useState('');
  const [departureFilter, setDepartureFilter] = React.useState('');
  const [metricFilter, setMetricFilter] = React.useState('');
  const [openTableFilter, setOpenTableFilter] =
    React.useState<RouteTableFilterKey | null>(null);
  const [selectedRouteId, setSelectedRouteId] = React.useState<
    number | undefined
  >();

  const [isFormOpen, setIsFormOpen] = React.useState(false);
  const [formMode, setFormMode] = React.useState<RouteFormMode>('create');
  const [editingRouteId, setEditingRouteId] = React.useState<number | null>(
    null
  );
  const [formValues, setFormValues] =
    React.useState<RouteFormState>(DEFAULT_FORM);
  const [isEstimatingTravelMetrics, setIsEstimatingTravelMetrics] =
    React.useState(false);
  const [deleteTarget, setDeleteTarget] =
    React.useState<SecondMileRoute | null>(null);

  const routeCodeFilterRef = React.useRef<HTMLInputElement>(null);
  const routeNameFilterRef = React.useRef<HTMLInputElement>(null);
  const departureFilterRef = React.useRef<HTMLInputElement>(null);
  const metricFilterRef = React.useRef<HTMLInputElement>(null);
  const lastTravelMetricKeyRef = React.useRef<string | null>(null);

  const selectedOriginHubNumericId =
    parseOptionalPositiveInteger(selectedOriginHubId);
  const selectedDestinationHubNumericId = parseOptionalPositiveInteger(
    selectedDestinationHubId
  );
  const selectedVehicleNumericId =
    parseOptionalPositiveInteger(selectedVehicleId);
  const formOriginHubNumericId = parseOptionalPositiveInteger(
    formValues.originHubId
  );
  const formDestinationHubNumericId = parseOptionalPositiveInteger(
    formValues.destinationHubId
  );
  const formOperatingHubNumericId =
    formValues.originType === 'POST_OFFICE'
      ? formDestinationHubNumericId
      : formOriginHubNumericId;

  const {
    data: routesData,
    isFetching,
    refetch,
  } = useGetSecondMileRoutesQuery(
    {
      page,
      size: PAGE_SIZE,
      keyword,
      routeCode,
      originType:
        selectedOriginType === ALL_FILTER_VALUE
          ? undefined
          : selectedOriginType,
      originHubId: selectedOriginHubNumericId,
      originPostOfficeCode:
        selectedOriginType === 'POST_OFFICE'
          ? selectedOriginPostOfficeCode.trim() || undefined
          : undefined,
      destinationType:
        selectedDestinationType === ALL_FILTER_VALUE
          ? undefined
          : selectedDestinationType,
      destinationHubId:
        selectedDestinationType === 'HUB'
          ? selectedDestinationHubNumericId
          : undefined,
      destinationPostOfficeCode:
        selectedDestinationType === 'POST_OFFICE'
          ? selectedDestinationPostOfficeCode.trim() || undefined
          : undefined,
      vehicleId: selectedVehicleNumericId,
      status: selectedStatus === ALL_FILTER_VALUE ? undefined : selectedStatus,
    },
    { skip: !isTmsAdmin }
  );

  const { data: hubsData } = useGetHubsQuery(
    { page: 0, size: 500 },
    { skip: !isTmsAdmin }
  );
  const { data: postOfficesData } = useGetPostOfficesQuery(
    { page: 0, size: 500 },
    { skip: !isTmsAdmin }
  );
  const { data: vehiclesData } = useGetSecondMileVehiclesQuery(
    { page: 0, size: 500 },
    { skip: !isTmsAdmin }
  );
  const {
    data: mappedPostOfficesData,
    isFetching: isFetchingMappedPostOffices,
  } = useGetHubPostOfficesQuery(
    { hubId: formOriginHubNumericId ?? 0, page: 0, size: 500 },
    {
      skip:
        !isTmsAdmin ||
        !isFormOpen ||
        formValues.destinationType !== 'POST_OFFICE' ||
        !formOriginHubNumericId,
    }
  );
  const { data: formVehiclesData, isFetching: isFetchingFormVehicles } =
    useGetSecondMileVehiclesQuery(
      {
        page: 0,
        size: 500,
        hubId: formOperatingHubNumericId,
        status: 'ACTIVE',
      },
      {
        skip: !isTmsAdmin || !isFormOpen || !formOperatingHubNumericId,
      }
    );
  const {
    data: formDriverAssignments,
    isFetching: isFetchingFormDriverAssignments,
  } = useGetSecondMileHubStaffAssignmentsQuery(
    { hubId: formOperatingHubNumericId ?? 0, role: 'DRIVER' },
    {
      skip: !isTmsAdmin || !isFormOpen || !formOperatingHubNumericId,
    }
  );

  const [createRoute, { isLoading: isCreating }] =
    useCreateSecondMileRouteMutation();
  const [updateRoute, { isLoading: isUpdating }] =
    useUpdateSecondMileRouteMutation();
  const [deleteRoute, { isLoading: isDeleting }] =
    useDeleteSecondMileRouteMutation();
  const isSavingRoute = isCreating || isUpdating;
  const isVehicleRequired =
    formValues.originType === 'POST_OFFICE' ||
    formValues.destinationType === 'POST_OFFICE';
  const isRouteFormDependencyLoading =
    isVehicleRequired &&
    (isFetchingMappedPostOffices ||
      isFetchingFormVehicles ||
      isFetchingFormDriverAssignments);

  const hubs = hubsData?.items ?? [];
  const postOffices = postOfficesData?.items ?? [];
  const vehicles = vehiclesData?.items ?? [];
  const mappedPostOffices = mappedPostOfficesData?.items ?? [];
  const formVehicles = formVehiclesData?.items ?? [];
  const routes = routesData?.items ?? [];
  const statusFilterOptions = [
    { value: ALL_FILTER_VALUE, label: 'Tất cả trạng thái' },
    ...ROUTE_STATUS_OPTIONS,
  ];
  const destinationTypeFilterOptions = [
    { value: ALL_FILTER_VALUE, label: 'Tất cả điểm đến' },
    ...ROUTE_DESTINATION_OPTIONS,
  ];
  const originTypeFilterOptions = [
    { value: ALL_FILTER_VALUE, label: 'Tất cả điểm xuất phát' },
    ...ROUTE_ENDPOINT_OPTIONS,
  ];
  const hubComboboxOptions = hubs.map((hub) => ({
    value: String(hub.id),
    label: `${hub.code} - ${hub.name}`,
  }));
  const destinationHubComboboxOptions = hubs
    .filter(
      (hub) =>
        formValues.originType === 'POST_OFFICE' ||
        hub.id !== formOriginHubNumericId
    )
    .map((hub) => ({
      value: String(hub.id),
      label: `${hub.code} - ${hub.name}`,
    }));

  const hubById = React.useMemo<Record<number, Hub>>(() => {
    return hubs.reduce<Record<number, Hub>>((acc, hub) => {
      acc[hub.id] = hub;
      return acc;
    }, {});
  }, [hubs]);

  const postOfficeByCode = React.useMemo<Record<string, PostOffice>>(() => {
    return postOffices.reduce<Record<string, PostOffice>>((acc, po) => {
      acc[po.code] = po;
      return acc;
    }, {});
  }, [postOffices]);

  const vehicleById = React.useMemo<Record<number, SecondMileVehicle>>(() => {
    return vehicles.reduce<Record<number, SecondMileVehicle>>(
      (acc, vehicle) => {
        acc[vehicle.id] = vehicle;
        return acc;
      },
      {}
    );
  }, [vehicles]);
  const formOriginCoordinates = React.useMemo<RouteCoordinates | null>(() => {
    if (formValues.originType === 'HUB') {
      const hubId = parseOptionalPositiveInteger(formValues.originHubId);
      return hubId !== undefined ? toRouteCoordinates(hubById[hubId]) : null;
    }

    const postOfficeCode = formValues.originPostOfficeCode.trim();
    return postOfficeCode
      ? toRouteCoordinates(postOfficeByCode[postOfficeCode])
      : null;
  }, [
    formValues.originHubId,
    formValues.originPostOfficeCode,
    formValues.originType,
    hubById,
    postOfficeByCode,
  ]);
  const formDestinationCoordinates =
    React.useMemo<RouteCoordinates | null>(() => {
      if (formValues.destinationType === 'HUB') {
        const hubId = parseOptionalPositiveInteger(
          formValues.destinationHubId
        );
        return hubId !== undefined ? toRouteCoordinates(hubById[hubId]) : null;
      }

      const postOfficeCode = formValues.destinationPostOfficeCode.trim();
      return postOfficeCode
        ? toRouteCoordinates(postOfficeByCode[postOfficeCode])
        : null;
    }, [
      formValues.destinationHubId,
      formValues.destinationPostOfficeCode,
      formValues.destinationType,
      hubById,
      postOfficeByCode,
    ]);
  const formTravelMetricKey =
    formOriginCoordinates && formDestinationCoordinates
      ? `${formOriginCoordinates.latitude},${formOriginCoordinates.longitude}:${formDestinationCoordinates.latitude},${formDestinationCoordinates.longitude}`
      : null;
  const driverAssignmentByStaffId = React.useMemo<
    Record<number, SecondMileHubStaffAssignment>
  >(() => {
    return (formDriverAssignments ?? []).reduce<
      Record<number, SecondMileHubStaffAssignment>
    >((acc, assignment) => {
      if (assignment.staffId !== undefined && assignment.staffId !== null) {
        acc[assignment.staffId] = assignment;
      }
      return acc;
    }, {});
  }, [formDriverAssignments]);

  const getDriverLabel = (vehicle: SecondMileVehicle) => {
    const assignedStaffId = vehicle.assignedStaffId;
    if (assignedStaffId === undefined || assignedStaffId === null) {
      return 'Chưa có tài xế';
    }
    const assignment = driverAssignmentByStaffId[assignedStaffId];
    return (
      assignment?.staffFullName ||
      assignment?.staffCode ||
      vehicle.assignedStaffFullName ||
      vehicle.assignedStaffCode ||
      `Tài xế #${assignedStaffId}`
    );
  };

  const postOfficeFilterOptions = postOffices.map((postOffice) => ({
    value: postOffice.code,
    label: `${postOffice.code} - ${postOffice.name}`,
  }));
  const routeVehicleFilterOptions = vehicles.map((vehicle) => ({
    value: String(vehicle.id),
    label: `${vehicle.licensePlate}${
      hubById[vehicle.hubId]?.code ? ` - ${hubById[vehicle.hubId].code}` : ''
    }`,
  }));
  const mappedPostOfficeComboboxOptions = mappedPostOffices.map((mapping) => {
    const postOffice = postOfficeByCode[mapping.postOfficeCode];
    return {
      value: mapping.postOfficeCode,
      label: postOffice
        ? `${postOffice.code} - ${postOffice.name}`
        : mapping.postOfficeCode,
    };
  });
  const formVehicleComboboxOptions = [
    ...(formValues.originType === 'HUB' && formValues.destinationType === 'HUB'
      ? [{ value: NO_VEHICLE_VALUE, label: 'Không gán xe' }]
      : []),
    ...formVehicles
      .filter(
        (vehicle) =>
          vehicle.status === 'ACTIVE' &&
          vehicle.assignedStaffId !== undefined &&
          vehicle.assignedStaffId !== null
      )
      .map((vehicle) => ({
        value: String(vehicle.id),
        label: `${vehicle.licensePlate} - ${getDriverLabel(vehicle)}`,
      })),
  ];

  const getHubLabel = React.useCallback((hubId?: number) => {
    if (hubId === undefined || hubId === null) {
      return '-';
    }
    const hub = hubById[hubId];
    return hub ? `${hub.code} - ${hub.name}` : `Hub #${hubId}`;
  }, [hubById]);

  const getPostOfficeLabel = React.useCallback((postOfficeCode?: string) => {
    if (!postOfficeCode) {
      return '-';
    }
    const postOffice = postOfficeByCode[postOfficeCode];
    return postOffice
      ? `${postOffice.code} - ${postOffice.name}`
      : postOfficeCode;
  }, [postOfficeByCode]);

  const getOriginLabel = (route: SecondMileRoute) => {
    if (route.originType === 'POST_OFFICE') {
      return getPostOfficeLabel(route.originPostOfficeCode);
    }
    return getHubLabel(route.originHubId);
  };

  const getVehicleLabel = (vehicleId?: number) => {
    if (vehicleId === undefined || vehicleId === null) {
      return '-';
    }
    const vehicle = vehicleById[vehicleId];
    return vehicle ? vehicle.licensePlate : `Xe #${vehicleId}`;
  };

  const getDestinationLabel = (route: SecondMileRoute) => {
    if (route.destinationType === 'HUB') {
      return getHubLabel(route.destinationHubId);
    }
    return getPostOfficeLabel(route.destinationPostOfficeCode);
  };

  const formMapLines = React.useMemo<RouteMapLine[]>(() => {
    if (!formOriginCoordinates || !formDestinationCoordinates) {
      return [];
    }

    const originName =
      formValues.originType === 'HUB'
        ? getHubLabel(formOriginHubNumericId)
        : getPostOfficeLabel(formValues.originPostOfficeCode);
    const destinationName =
      formValues.destinationType === 'HUB'
        ? getHubLabel(formDestinationHubNumericId)
        : getPostOfficeLabel(formValues.destinationPostOfficeCode);

    if (originName === '-' || destinationName === '-') {
      return [];
    }

    return [
      {
        id: editingRouteId ?? -1,
        routeCode: 'Mã tự sinh',
        routeName: formValues.routeName.trim() || 'Tuyến mới',
        origin: {
          name: originName,
          latitude: formOriginCoordinates.latitude,
          longitude: formOriginCoordinates.longitude,
        },
        destination: {
          name: destinationName,
          latitude: formDestinationCoordinates.latitude,
          longitude: formDestinationCoordinates.longitude,
          type: formValues.destinationType,
        },
      },
    ];
  }, [
    editingRouteId,
    formDestinationCoordinates,
    formDestinationHubNumericId,
    formOriginCoordinates,
    formOriginHubNumericId,
    formValues.destinationPostOfficeCode,
    formValues.destinationType,
    formValues.originPostOfficeCode,
    formValues.originType,
    formValues.routeName,
    getHubLabel,
    getPostOfficeLabel,
  ]);

  const getRouteMetricLabel = React.useCallback(
    (route: SecondMileRoute) =>
      `${formatRouteMetric(route.estimatedDistanceKm, 'km')} / ${formatRouteMetric(
        route.estimatedDurationMinutes,
        'phút'
      )}`,
    []
  );

  const displayRoutes = React.useMemo(() => {
    const departureKeyword = departureFilter.trim().toLowerCase();
    const metricKeyword = metricFilter.trim().toLowerCase();

    return routes.filter((route) => {
      const departureMatches =
        !departureKeyword ||
        (route.fixedDepartureTime || '-')
          .toLowerCase()
          .includes(departureKeyword);
      const metricMatches =
        !metricKeyword ||
        getRouteMetricLabel(route).toLowerCase().includes(metricKeyword);

      return departureMatches && metricMatches;
    });
  }, [departureFilter, getRouteMetricLabel, metricFilter, routes]);

  const mapLines = React.useMemo<RouteMapLine[]>(() => {
    const lines: RouteMapLine[] = [];

    for (const route of displayRoutes) {
      const originHub =
        route.originType === 'HUB' && route.originHubId
          ? hubById[route.originHubId]
          : undefined;
      const originPostOffice =
        route.originType === 'POST_OFFICE' && route.originPostOfficeCode
          ? postOfficeByCode[route.originPostOfficeCode]
          : undefined;
      const originName = originHub
        ? `${originHub.code} - ${originHub.name}`
        : originPostOffice
          ? `${originPostOffice.code} - ${originPostOffice.name}`
          : undefined;
      const originLatitude = originHub?.latitude ?? originPostOffice?.latitude;
      const originLongitude =
        originHub?.longitude ?? originPostOffice?.longitude;
      if (
        !originName ||
        originLatitude === undefined ||
        originLongitude === undefined
      ) {
        continue;
      }

      if (route.destinationType === 'HUB') {
        const destinationHub =
          route.destinationHubId !== undefined
            ? hubById[route.destinationHubId]
            : undefined;
        if (
          !destinationHub ||
          destinationHub.latitude === undefined ||
          destinationHub.longitude === undefined
        ) {
          continue;
        }
        lines.push({
          id: route.id,
          routeCode: route.routeCode,
          routeName: route.routeName,
          origin: {
            name: originName,
            latitude: originLatitude,
            longitude: originLongitude,
          },
          destination: {
            name: `${destinationHub.code} - ${destinationHub.name}`,
            latitude: destinationHub.latitude,
            longitude: destinationHub.longitude,
            type: 'HUB',
          },
        });
        continue;
      }

      const destinationPostOffice = route.destinationPostOfficeCode
        ? postOfficeByCode[route.destinationPostOfficeCode]
        : undefined;
      if (
        !destinationPostOffice ||
        destinationPostOffice.latitude === undefined ||
        destinationPostOffice.longitude === undefined
      ) {
        continue;
      }
      lines.push({
        id: route.id,
        routeCode: route.routeCode,
        routeName: route.routeName,
        origin: {
          name: originName,
          latitude: originLatitude,
          longitude: originLongitude,
        },
        destination: {
          name: `${destinationPostOffice.code} - ${destinationPostOffice.name}`,
          latitude: destinationPostOffice.latitude,
          longitude: destinationPostOffice.longitude,
          type: 'POST_OFFICE',
        },
      });
    }

    return lines;
  }, [displayRoutes, hubById, postOfficeByCode]);

  React.useEffect(() => {
    if (
      !isFormOpen ||
      !formTravelMetricKey ||
      !formOriginCoordinates ||
      !formDestinationCoordinates
    ) {
      setIsEstimatingTravelMetrics(false);
      return;
    }
    if (lastTravelMetricKeyRef.current === formTravelMetricKey) {
      return;
    }
    lastTravelMetricKeyRef.current = formTravelMetricKey;
    if (
      formValues.estimatedDistanceKm.trim() &&
      formValues.estimatedDurationMinutes.trim()
    ) {
      return;
    }

    const controller = new AbortController();
    let cancelled = false;

    const applyTravelMetrics = (metrics: RouteTravelMetrics) => {
      setFormValues((prev) => ({
        ...prev,
        estimatedDistanceKm: prev.estimatedDistanceKm.trim()
          ? prev.estimatedDistanceKm
          : formatDistanceFieldValue(metrics.distanceKm),
        estimatedDurationMinutes: prev.estimatedDurationMinutes.trim()
          ? prev.estimatedDurationMinutes
          : String(metrics.durationMinutes),
      }));
    };

    const loadTravelMetrics = async () => {
      setIsEstimatingTravelMetrics(true);
      try {
        const metrics = await fetchRouteTravelMetrics(
          formOriginCoordinates,
          formDestinationCoordinates,
          controller.signal
        );
        if (!cancelled) {
          applyTravelMetrics(metrics);
        }
      } catch {
        if (!cancelled && !controller.signal.aborted) {
          applyTravelMetrics(
            estimateFallbackTravelMetrics(
              formOriginCoordinates,
              formDestinationCoordinates
            )
          );
        }
      } finally {
        if (!cancelled) {
          setIsEstimatingTravelMetrics(false);
        }
      }
    };

    void loadTravelMetrics();

    return () => {
      cancelled = true;
      controller.abort();
    };
  }, [
    formDestinationCoordinates,
    formOriginCoordinates,
    formTravelMetricKey,
    formValues.estimatedDistanceKm,
    formValues.estimatedDurationMinutes,
    isFormOpen,
  ]);

  const updateField = <K extends keyof RouteFormState>(
    key: K,
    value: RouteFormState[K]
  ) => setFormValues((prev) => ({ ...prev, [key]: value }));

  const updateOriginType = (value: SecondMileRouteEndpointType) => {
    lastTravelMetricKeyRef.current = null;
    setFormValues((prev) => ({
      ...prev,
      originType: value,
      originHubId: value === 'HUB' ? prev.originHubId : '',
      originPostOfficeCode:
        value === 'POST_OFFICE' ? prev.originPostOfficeCode : '',
      destinationType:
        value === 'POST_OFFICE' && prev.destinationType === 'POST_OFFICE'
          ? 'HUB'
          : prev.destinationType,
      destinationHubId: '',
      destinationPostOfficeCode: '',
      vehicleId: '',
      estimatedDistanceKm: '',
      estimatedDurationMinutes: '',
    }));
  };

  const updateOriginHub = (value: string) => {
    lastTravelMetricKeyRef.current = null;
    setFormValues((prev) => ({
      ...prev,
      originHubId: value,
      destinationHubId: '',
      destinationPostOfficeCode: '',
      vehicleId: '',
      estimatedDistanceKm: '',
      estimatedDurationMinutes: '',
    }));
  };

  const updateOriginPostOffice = (value: string) => {
    lastTravelMetricKeyRef.current = null;
    setFormValues((prev) => ({
      ...prev,
      originPostOfficeCode: value,
      vehicleId: '',
      estimatedDistanceKm: '',
      estimatedDurationMinutes: '',
    }));
  };

  const updateDestinationHub = (value: string) => {
    lastTravelMetricKeyRef.current = null;
    setFormValues((prev) => ({
      ...prev,
      destinationHubId: value,
      vehicleId: prev.originType === 'POST_OFFICE' ? '' : prev.vehicleId,
      estimatedDistanceKm: '',
      estimatedDurationMinutes: '',
    }));
  };

  const updateDestinationPostOffice = (value: string) => {
    lastTravelMetricKeyRef.current = null;
    setFormValues((prev) => ({
      ...prev,
      destinationPostOfficeCode: value,
      estimatedDistanceKm: '',
      estimatedDurationMinutes: '',
    }));
  };

  const clearRouteFilters = () => {
    setRouteCodeInput('');
    setRouteCode(undefined);
    setSearchInput('');
    setKeyword(undefined);
    setSelectedStatus(ALL_FILTER_VALUE);
    setSelectedOriginType(ALL_FILTER_VALUE);
    setSelectedOriginHubId('');
    setSelectedOriginPostOfficeCode('');
    setSelectedDestinationType(ALL_FILTER_VALUE);
    setSelectedDestinationHubId('');
    setSelectedDestinationPostOfficeCode('');
    setSelectedVehicleId('');
    setDepartureFilter('');
    setMetricFilter('');
    setPage(0);
  };

  const handleTableFilterSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    setRouteCode(routeCodeInput.trim() || undefined);
    setKeyword(searchInput.trim() || undefined);
    setPage(0);
    setOpenTableFilter(null);
  };

  React.useEffect(() => {
    if (openTableFilter === 'routeCode') {
      routeCodeFilterRef.current?.focus();
      routeCodeFilterRef.current?.select();
    }
    if (openTableFilter === 'routeName') {
      routeNameFilterRef.current?.focus();
      routeNameFilterRef.current?.select();
    }
    if (openTableFilter === 'departure') {
      departureFilterRef.current?.focus();
      departureFilterRef.current?.select();
    }
    if (openTableFilter === 'metrics') {
      metricFilterRef.current?.focus();
      metricFilterRef.current?.select();
    }
  }, [openTableFilter]);

  const isRouteCodeFilterActive = Boolean(routeCodeInput.trim() || routeCode);
  const isRouteNameFilterActive = Boolean(searchInput.trim() || keyword);
  const isOriginFilterActive =
    selectedOriginType !== ALL_FILTER_VALUE ||
    Boolean(selectedOriginHubId) ||
    Boolean(selectedOriginPostOfficeCode);
  const isDestinationTypeFilterActive =
    selectedDestinationType !== ALL_FILTER_VALUE;
  const isDestinationFilterActive =
    Boolean(selectedDestinationHubId) ||
    Boolean(selectedDestinationPostOfficeCode);
  const isVehicleFilterActive = Boolean(selectedVehicleId);
  const isDepartureFilterActive = Boolean(departureFilter.trim());
  const isMetricFilterActive = Boolean(metricFilter.trim());
  const isStatusFilterActive = selectedStatus !== ALL_FILTER_VALUE;

  const renderTextTableFilter = ({
    filterKey,
    title,
    buttonTitle,
    inputRef,
    value,
    onChange,
    onClear,
    placeholder,
    isActive,
  }: {
    filterKey: RouteTableFilterKey;
    title: string;
    buttonTitle: string;
    inputRef: React.RefObject<HTMLInputElement | null>;
    value: string;
    onChange: (value: string) => void;
    onClear: () => void;
    placeholder: string;
    isActive: boolean;
  }) => (
    <Popover
      open={openTableFilter === filterKey}
      onOpenChange={(open) => setOpenTableFilter(open ? filterKey : null)}
    >
      <div className='flex items-center gap-1'>
        <span>{title}</span>
        <PopoverTrigger asChild>
          <Button
            type='button'
            variant={isActive ? 'outline' : 'ghost'}
            size='icon'
            className='size-7'
            title={buttonTitle}
            aria-label={buttonTitle}
          >
            <Search className='h-4 w-4' />
          </Button>
        </PopoverTrigger>
      </div>
      <PopoverContent align='start' sideOffset={8} className='w-72 p-3'>
        <form
          className='flex items-center gap-2'
          onSubmit={handleTableFilterSubmit}
        >
          <Input
            ref={inputRef}
            className='h-9 bg-background'
            value={value}
            onChange={(event) => onChange(event.target.value)}
            placeholder={placeholder}
            disabled={isFetching}
          />
          <Button
            type='submit'
            variant='outline'
            size='icon'
            className='size-9 shrink-0'
            disabled={isFetching}
            title='Tìm kiếm'
            aria-label={buttonTitle}
          >
            <Search className='h-4 w-4' />
          </Button>
          {isActive ? (
            <Button
              type='button'
              variant='ghost'
              size='icon'
              className='size-9 shrink-0'
              disabled={isFetching}
              onClick={onClear}
              title='Xóa tìm kiếm'
              aria-label={`Xóa ${buttonTitle.toLowerCase()}`}
            >
              <X className='h-4 w-4' />
            </Button>
          ) : null}
        </form>
      </PopoverContent>
    </Popover>
  );

  const openCreateDialog = () => {
    lastTravelMetricKeyRef.current = null;
    setFormMode('create');
    setEditingRouteId(null);
    setFormValues(DEFAULT_FORM);
    setIsFormOpen(true);
  };

  const openEditDialog = (route: SecondMileRoute) => {
    lastTravelMetricKeyRef.current = null;
    setFormMode('edit');
    setEditingRouteId(route.id);
    setFormValues(toFormState(route));
    setIsFormOpen(true);
  };

  const validateForm = (values: RouteFormState): string | null => {
    if (!values.routeName.trim()) return 'Vui lòng nhập tên tuyến.';
    if (values.originType === 'HUB' && !values.originHubId) {
      return 'Vui lòng chọn hub xuất phát.';
    }
    if (
      values.originType === 'POST_OFFICE' &&
      !values.originPostOfficeCode.trim()
    ) {
      return 'Vui lòng chọn bưu cục xuất phát.';
    }
    const originHubId = parseOptionalPositiveInteger(values.originHubId);
    const destinationHubId = parseOptionalPositiveInteger(
      values.destinationHubId
    );
    const vehicleId = parseOptionalPositiveInteger(values.vehicleId);
    const operatingHubId =
      values.originType === 'POST_OFFICE' ? destinationHubId : originHubId;
    if (
      values.originType === 'POST_OFFICE' &&
      values.destinationType !== 'HUB'
    ) {
      return 'Tuyến xuất phát từ bưu cục phải có điểm đến là hub.';
    }
    if (values.destinationType === 'HUB' && !values.destinationHubId) {
      return 'Vui lòng chọn hub đích.';
    }
    if (
      values.destinationType === 'HUB' &&
      originHubId !== undefined &&
      destinationHubId !== undefined &&
      originHubId === destinationHubId
    ) {
      return 'Hub xuất phát và hub đích phải khác nhau.';
    }
    if (values.originType === 'POST_OFFICE' && destinationHubId !== undefined) {
      const originPostOffice =
        postOfficeByCode[values.originPostOfficeCode.trim()];
      if (
        originPostOffice?.hubId !== undefined &&
        originPostOffice.hubId !== destinationHubId
      ) {
        return 'Hub đích phải khớp với hub của bưu cục xuất phát.';
      }
    }
    if (
      values.destinationType === 'POST_OFFICE' &&
      !values.destinationPostOfficeCode.trim()
    ) {
      return 'Vui lòng chọn bưu cục đích.';
    }
    if (values.destinationType === 'POST_OFFICE' && !values.vehicleId) {
      return 'Tuyến từ hub đến bưu cục cần chọn xe.';
    }
    if (values.originType === 'POST_OFFICE' && !values.vehicleId) {
      return 'Tuyến từ bưu cục đến hub cần chọn xe.';
    }
    if (
      values.originType === 'HUB' &&
      values.destinationType === 'POST_OFFICE'
    ) {
      if (isFetchingMappedPostOffices) {
        return 'Danh sách bưu cục liên kết đang được tải.';
      }
      const mappedCodes = new Set(
        mappedPostOffices.map((mapping) => mapping.postOfficeCode)
      );
      if (!mappedCodes.has(values.destinationPostOfficeCode.trim())) {
        return 'Bưu cục đích phải được liên kết với hub xuất phát.';
      }
    }
    if (vehicleId !== undefined) {
      const selectedVehicle =
        formVehicles.find((vehicle) => vehicle.id === vehicleId) ??
        vehicleById[vehicleId];
      if (selectedVehicle) {
        if (
          operatingHubId !== undefined &&
          selectedVehicle.hubId !== operatingHubId
        ) {
          return 'Xe phải thuộc hub vận hành.';
        }
        if (selectedVehicle.status !== 'ACTIVE') {
          return 'Xe phải đang hoạt động.';
        }
        if (
          selectedVehicle.assignedStaffId === undefined ||
          selectedVehicle.assignedStaffId === null
        ) {
          return 'Xe phải có tài xế được phân công.';
        }
      }
    }
    const distanceError = validateOptionalNonNegativeNumber(
      'Khoảng cách',
      values.estimatedDistanceKm
    );
    if (distanceError) {
      return distanceError;
    }
    const durationError = validateOptionalNonNegativeInteger(
      'Thời lượng',
      values.estimatedDurationMinutes
    );
    if (durationError) {
      return durationError;
    }
    return null;
  };

  const buildRequestBody = (
    values: RouteFormState
  ): SecondMileCreateRouteRequest | SecondMileUpdateRouteRequest => {
    const vehicleId = parseOptionalPositiveInteger(values.vehicleId);
    const body: SecondMileCreateRouteRequest = {
      route_name: values.routeName.trim(),
      origin_type: values.originType,
      destination_type: values.destinationType,
      vehicle_id: vehicleId,
      estimated_distance_km: parseOptionalNumber(values.estimatedDistanceKm),
      estimated_duration_minutes: parseOptionalNonNegativeInteger(
        values.estimatedDurationMinutes
      ),
      fixed_departure_time: values.fixedDepartureTime || undefined,
      note: values.note.trim() || undefined,
      status: values.status,
    };

    if (values.originType === 'HUB') {
      body.origin_hub_id = parseOptionalPositiveInteger(values.originHubId);
      body.origin_post_office_code = undefined;
    } else {
      body.origin_hub_id = undefined;
      body.origin_post_office_code =
        values.originPostOfficeCode.trim() || undefined;
    }

    if (values.destinationType === 'HUB') {
      body.destination_hub_id = parseOptionalPositiveInteger(
        values.destinationHubId
      );
      body.destination_post_office_code = undefined;
    } else {
      body.destination_hub_id = undefined;
      body.destination_post_office_code =
        values.destinationPostOfficeCode.trim() || undefined;
    }

    return body as SecondMileCreateRouteRequest | SecondMileUpdateRouteRequest;
  };

  if (!isTmsAdmin) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <ShieldAlert className='h-5 w-5' />
            Không có quyền truy cập
          </CardTitle>
          <CardDescription>
            Quản lý tuyến yêu cầu vai trò TMS_ADMIN.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <>
      <div className='space-y-6'>
        <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <h1 className='text-2xl font-bold tracking-tight'>
              Tuyến vận chuyển
            </h1>
            <p className='text-muted-foreground'>
              Quản lý tuyến vận chuyển cố định giữa hub và bưu cục.
            </p>
          </div>
          <div className='flex gap-2'>
            <Button variant='outline' onClick={() => void refetch()}>
              <RefreshCw className='mr-2 h-4 w-4' />
              Làm mới
            </Button>
            <Button onClick={openCreateDialog}>
              <Plus className='mr-2 h-4 w-4' />
              Tạo tuyến
            </Button>
          </div>
        </div>

        <SecondMileRoutesMap
          lines={mapLines}
          selectedRouteId={selectedRouteId}
        />

        <Card>
          <CardHeader>
            <CardTitle>Danh sách tuyến</CardTitle>
            <CardDescription>
              Chọn một dòng để làm nổi bật tuyến trên bản đồ.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='overflow-x-auto'>
              <Table className='min-w-[1380px]'>
                <TableHeader>
                  <TableRow>
                    <TableHead className='w-[150px]'>
                      {renderTextTableFilter({
                        filterKey: 'routeCode',
                        title: 'Mã hành trình',
                        buttonTitle: 'Tìm mã hành trình',
                        inputRef: routeCodeFilterRef,
                        value: routeCodeInput,
                        onChange: setRouteCodeInput,
                        onClear: () => {
                          setRouteCodeInput('');
                          setRouteCode(undefined);
                          setPage(0);
                        },
                        placeholder: 'Tìm mã hành trình...',
                        isActive: isRouteCodeFilterActive,
                      })}
                    </TableHead>
                    <TableHead className='w-[220px]'>
                      {renderTextTableFilter({
                        filterKey: 'routeName',
                        title: 'Tên hành trình',
                        buttonTitle: 'Tìm tên hành trình',
                        inputRef: routeNameFilterRef,
                        value: searchInput,
                        onChange: setSearchInput,
                        onClear: () => {
                          setSearchInput('');
                          setKeyword(undefined);
                          setPage(0);
                        },
                        placeholder: 'Tìm tên hành trình...',
                        isActive: isRouteNameFilterActive,
                      })}
                    </TableHead>
                    <TableHead className='w-[240px]'>
                      <Popover
                        open={openTableFilter === 'origin'}
                        onOpenChange={(open) =>
                          setOpenTableFilter(open ? 'origin' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Nguồn</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isOriginFilterActive ? 'outline' : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc nguồn'
                              aria-label='Lọc nguồn'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-80 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleTableFilterSubmit}
                          >
                            <TmsCombobox
                              id='route-table-filter-origin-type'
                              value={selectedOriginType}
                              onValueChange={(value) => {
                                setSelectedOriginType(
                                  value as RouteOriginFilter
                                );
                                setSelectedOriginHubId('');
                                setSelectedOriginPostOfficeCode('');
                                setSelectedVehicleId('');
                              }}
                              options={originTypeFilterOptions}
                              placeholder='Tất cả nguồn'
                              emptyText='Không tìm thấy loại nguồn'
                              disabled={isFetching}
                            />
                            {selectedOriginType === 'POST_OFFICE' ? (
                              <TmsCombobox
                                id='route-table-filter-origin-post-office'
                                value={selectedOriginPostOfficeCode}
                                onValueChange={setSelectedOriginPostOfficeCode}
                                options={postOfficeFilterOptions}
                                placeholder='Bưu cục nguồn'
                                emptyText='Không tìm thấy bưu cục'
                                clearable
                                disabled={isFetching}
                              />
                            ) : (
                              <TmsCombobox
                                id='route-table-filter-origin-hub'
                                value={selectedOriginHubId}
                                onValueChange={(value) => {
                                  setSelectedOriginHubId(value);
                                  setSelectedVehicleId('');
                                }}
                                options={hubComboboxOptions}
                                placeholder='Hub nguồn'
                                emptyText='Không tìm thấy hub'
                                clearable
                                disabled={isFetching}
                              />
                            )}
                            <div className='flex justify-end gap-2'>
                              {isOriginFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() => {
                                    setSelectedOriginType(ALL_FILTER_VALUE);
                                    setSelectedOriginHubId('');
                                    setSelectedOriginPostOfficeCode('');
                                    setSelectedVehicleId('');
                                    setPage(0);
                                  }}
                                >
                                  Xóa
                                </Button>
                              ) : null}
                              <Button
                                type='submit'
                                variant='outline'
                                size='sm'
                                disabled={isFetching}
                              >
                                Tìm kiếm
                              </Button>
                            </div>
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[160px]'>
                      <Popover
                        open={openTableFilter === 'destinationType'}
                        onOpenChange={(open) =>
                          setOpenTableFilter(open ? 'destinationType' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Loại đích</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isDestinationTypeFilterActive
                                  ? 'outline'
                                  : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc loại đích'
                              aria-label='Lọc loại đích'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-64 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleTableFilterSubmit}
                          >
                            <TmsCombobox
                              id='route-table-filter-destination-type'
                              value={selectedDestinationType}
                              onValueChange={(value) => {
                                setSelectedDestinationType(
                                  value as RouteDestinationFilter
                                );
                                setSelectedDestinationHubId('');
                                setSelectedDestinationPostOfficeCode('');
                              }}
                              options={destinationTypeFilterOptions}
                              placeholder='Tất cả loại đích'
                              emptyText='Không tìm thấy loại đích'
                              disabled={isFetching}
                            />
                            <div className='flex justify-end gap-2'>
                              {isDestinationTypeFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() => {
                                    setSelectedDestinationType(
                                      ALL_FILTER_VALUE
                                    );
                                    setSelectedDestinationHubId('');
                                    setSelectedDestinationPostOfficeCode('');
                                    setPage(0);
                                  }}
                                >
                                  Xóa
                                </Button>
                              ) : null}
                              <Button
                                type='submit'
                                variant='outline'
                                size='sm'
                                disabled={isFetching}
                              >
                                Tìm kiếm
                              </Button>
                            </div>
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[240px]'>
                      <Popover
                        open={openTableFilter === 'destination'}
                        onOpenChange={(open) =>
                          setOpenTableFilter(open ? 'destination' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Đích</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isDestinationFilterActive ? 'outline' : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc đích'
                              aria-label='Lọc đích'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-80 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleTableFilterSubmit}
                          >
                            <TmsCombobox
                              id='route-table-filter-destination-kind'
                              value={selectedDestinationType}
                              onValueChange={(value) => {
                                setSelectedDestinationType(
                                  value as RouteDestinationFilter
                                );
                                setSelectedDestinationHubId('');
                                setSelectedDestinationPostOfficeCode('');
                              }}
                              options={destinationTypeFilterOptions}
                              placeholder='Chọn loại đích'
                              emptyText='Không tìm thấy loại đích'
                              disabled={isFetching}
                            />
                            {selectedDestinationType === 'POST_OFFICE' ? (
                              <TmsCombobox
                                id='route-table-filter-destination-post-office'
                                value={selectedDestinationPostOfficeCode}
                                onValueChange={
                                  setSelectedDestinationPostOfficeCode
                                }
                                options={postOfficeFilterOptions}
                                placeholder='Bưu cục đích'
                                emptyText='Không tìm thấy bưu cục'
                                clearable
                                disabled={isFetching}
                              />
                            ) : (
                              <TmsCombobox
                                id='route-table-filter-destination-hub'
                                value={selectedDestinationHubId}
                                onValueChange={setSelectedDestinationHubId}
                                options={hubComboboxOptions}
                                placeholder='Hub đích'
                                emptyText='Không tìm thấy hub'
                                clearable
                                disabled={isFetching}
                              />
                            )}
                            <div className='flex justify-end gap-2'>
                              {isDestinationFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() => {
                                    setSelectedDestinationHubId('');
                                    setSelectedDestinationPostOfficeCode('');
                                    setPage(0);
                                  }}
                                >
                                  Xóa
                                </Button>
                              ) : null}
                              <Button
                                type='submit'
                                variant='outline'
                                size='sm'
                                disabled={isFetching}
                              >
                                Tìm kiếm
                              </Button>
                            </div>
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[180px]'>
                      <Popover
                        open={openTableFilter === 'vehicle'}
                        onOpenChange={(open) =>
                          setOpenTableFilter(open ? 'vehicle' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Phương tiện</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isVehicleFilterActive ? 'outline' : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc phương tiện'
                              aria-label='Lọc phương tiện'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-72 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleTableFilterSubmit}
                          >
                            <TmsCombobox
                              id='route-table-filter-vehicle'
                              value={selectedVehicleId}
                              onValueChange={setSelectedVehicleId}
                              options={routeVehicleFilterOptions}
                              placeholder='Chọn phương tiện'
                              emptyText='Không tìm thấy phương tiện'
                              clearable
                              disabled={isFetching}
                            />
                            <div className='flex justify-end gap-2'>
                              {isVehicleFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() => {
                                    setSelectedVehicleId('');
                                    setPage(0);
                                  }}
                                >
                                  Xóa
                                </Button>
                              ) : null}
                              <Button
                                type='submit'
                                variant='outline'
                                size='sm'
                                disabled={isFetching}
                              >
                                Tìm kiếm
                              </Button>
                            </div>
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[150px]'>
                      {renderTextTableFilter({
                        filterKey: 'departure',
                        title: 'Giờ đi',
                        buttonTitle: 'Tìm giờ đi',
                        inputRef: departureFilterRef,
                        value: departureFilter,
                        onChange: setDepartureFilter,
                        onClear: () => setDepartureFilter(''),
                        placeholder: 'Tìm giờ đi...',
                        isActive: isDepartureFilterActive,
                      })}
                    </TableHead>
                    <TableHead className='w-[210px]'>
                      {renderTextTableFilter({
                        filterKey: 'metrics',
                        title: 'Khoảng cách / thời gian',
                        buttonTitle: 'Tìm khoảng cách hoặc thời gian',
                        inputRef: metricFilterRef,
                        value: metricFilter,
                        onChange: setMetricFilter,
                        onClear: () => setMetricFilter(''),
                        placeholder: 'Tìm khoảng cách hoặc thời gian...',
                        isActive: isMetricFilterActive,
                      })}
                    </TableHead>
                    <TableHead className='w-[160px]'>
                      <Popover
                        open={openTableFilter === 'status'}
                        onOpenChange={(open) =>
                          setOpenTableFilter(open ? 'status' : null)
                        }
                      >
                        <div className='flex items-center gap-1'>
                          <span>Trạng thái</span>
                          <PopoverTrigger asChild>
                            <Button
                              type='button'
                              variant={
                                isStatusFilterActive ? 'outline' : 'ghost'
                              }
                              size='icon'
                              className='size-7'
                              title='Lọc trạng thái'
                              aria-label='Lọc trạng thái'
                            >
                              <Search className='h-4 w-4' />
                            </Button>
                          </PopoverTrigger>
                        </div>
                        <PopoverContent
                          align='start'
                          sideOffset={8}
                          className='w-64 p-3'
                        >
                          <form
                            className='space-y-3'
                            onSubmit={handleTableFilterSubmit}
                          >
                            <TmsCombobox
                              id='route-table-filter-status'
                              value={selectedStatus}
                              onValueChange={(value) =>
                                setSelectedStatus(
                                  value as
                                    | typeof ALL_FILTER_VALUE
                                    | SecondMileRouteStatus
                                )
                              }
                              options={statusFilterOptions}
                              placeholder='Tất cả trạng thái'
                              emptyText='Không tìm thấy trạng thái'
                              disabled={isFetching}
                            />
                            <div className='flex justify-end gap-2'>
                              {isStatusFilterActive ? (
                                <Button
                                  type='button'
                                  variant='ghost'
                                  size='sm'
                                  disabled={isFetching}
                                  onClick={() => {
                                    setSelectedStatus(ALL_FILTER_VALUE);
                                    setPage(0);
                                  }}
                                >
                                  Xóa
                                </Button>
                              ) : null}
                              <Button
                                type='submit'
                                variant='outline'
                                size='sm'
                                disabled={isFetching}
                              >
                                Tìm kiếm
                              </Button>
                            </div>
                          </form>
                        </PopoverContent>
                      </Popover>
                    </TableHead>
                    <TableHead className='w-[96px] text-right'>
                      <span className='sr-only'>Thao tác</span>
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {displayRoutes.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={10}
                        className='py-8 text-center text-muted-foreground'
                      >
                        {isFetching
                          ? 'Đang tải tuyến...'
                          : 'Không tìm thấy tuyến'}
                      </TableCell>
                    </TableRow>
                  ) : (
                    displayRoutes.map((route) => (
                      <TableRow
                        key={route.id}
                        className='cursor-pointer'
                        onClick={() => setSelectedRouteId(route.id)}
                      >
                        <TableCell className='font-medium'>
                          {route.routeCode}
                        </TableCell>
                        <TableCell>{route.routeName}</TableCell>
                        <TableCell>{getOriginLabel(route)}</TableCell>
                        <TableCell>
                          {formatDestinationTypeLabel(route.destinationType)}
                        </TableCell>
                        <TableCell>{getDestinationLabel(route)}</TableCell>
                        <TableCell>
                          {getVehicleLabel(route.vehicleId)}
                        </TableCell>
                        <TableCell>{route.fixedDepartureTime || '-'}</TableCell>
                        <TableCell>
                          {formatRouteMetric(route.estimatedDistanceKm, 'km')} /{' '}
                          {formatRouteMetric(
                            route.estimatedDurationMinutes,
                            'phút'
                          )}
                        </TableCell>
                        <TableCell>
                          <Badge variant={getStatusBadgeVariant(route.status)}>
                            {formatStatusLabel(route.status)}
                          </Badge>
                        </TableCell>
                        <TableCell className='text-right'>
                          <div className='flex justify-end gap-2'>
                            <Button
                              variant='outline'
                              size='icon'
                              title='Sửa hành trình'
                              aria-label='Sửa hành trình'
                              onClick={(event) => {
                                event.stopPropagation();
                                openEditDialog(route);
                              }}
                            >
                              <Pencil className='h-4 w-4' />
                            </Button>
                            <Button
                              variant='destructive'
                              size='icon'
                              title='Xóa hành trình'
                              aria-label='Xóa hành trình'
                              onClick={(event) => {
                                event.stopPropagation();
                                setDeleteTarget(route);
                              }}
                            >
                              <Trash2 className='h-4 w-4' />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </div>

            {(routesData?.hasNext || routesData?.hasPrevious) && (
              <div className='flex items-center justify-between border-t pt-3'>
                <div className='text-sm text-muted-foreground'>
                  Trang {(routesData?.currentPage ?? 0) + 1} /{' '}
                  {routesData?.totalPages ?? 1}
                </div>
                <div className='flex gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!routesData?.hasPrevious}
                    onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                  >
                    Trước
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!routesData?.hasNext}
                    onClick={() => setPage((prev) => prev + 1)}
                  >
                    Sau
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog
        open={isFormOpen}
        onOpenChange={(open) => {
          if (!isSavingRoute) {
            setIsFormOpen(open);
            if (!open) {
              setEditingRouteId(null);
            }
          }
        }}
      >
        <DialogContent className='max-h-[88vh] w-[98vw] max-w-none overflow-y-auto sm:max-w-[1536px]'>
          <DialogHeader>
            <DialogTitle>
              {formMode === 'create' ? 'Tạo tuyến' : 'Cập nhật tuyến'}
            </DialogTitle>
            <DialogDescription>
              Cấu hình tuyến cố định cho vận chuyển chặng giữa.
            </DialogDescription>
          </DialogHeader>

          <form
            className='space-y-5'
            onSubmit={async (event) => {
              event.preventDefault();
              const error = validateForm(formValues);
              if (error) {
                notification.error(error);
                return;
              }

              const body = buildRequestBody(formValues);
              try {
                if (formMode === 'create') {
                  await createRoute(
                    body as SecondMileCreateRouteRequest
                  ).unwrap();
                  notification.success('Đã tạo tuyến.');
                  if (page !== 0) setPage(0);
                } else if (editingRouteId !== null) {
                  await updateRoute({
                    id: editingRouteId,
                    body: body as SecondMileUpdateRouteRequest,
                  }).unwrap();
                  notification.success('Đã cập nhật tuyến.');
                }
                setIsFormOpen(false);
                setEditingRouteId(null);
                void refetch();
              } catch (errorCreateUpdate) {
                notification.error('Lưu tuyến thất bại.', {
                  description: getErrorMessage(errorCreateUpdate),
                });
              }
            }}
          >
            <div className='grid gap-6 xl:grid-cols-[minmax(0,720px)_minmax(480px,1fr)] 2xl:grid-cols-[minmax(0,760px)_minmax(560px,1fr)]'>
              <div className='space-y-4'>
                <div className='grid gap-4 md:grid-cols-2'>
                  <div className='space-y-2 md:col-span-2'>
                    <Label htmlFor='route-name'>Tên tuyến</Label>
                    <Input
                      id='route-name'
                      value={formValues.routeName}
                      onChange={(event) =>
                        updateField('routeName', event.target.value)
                      }
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='route-origin-type'>
                      Loại điểm xuất phát
                    </Label>
                    <TmsCombobox
                      id='route-origin-type'
                      value={formValues.originType}
                      onValueChange={(value) =>
                        updateOriginType(value as SecondMileRouteEndpointType)
                      }
                      options={ROUTE_ENDPOINT_OPTIONS}
                      placeholder='Chọn loại điểm xuất phát'
                      emptyText='Không tìm thấy loại điểm xuất phát'
                    />
                  </div>
                  {formValues.originType === 'HUB' ? (
                    <div className='space-y-2'>
                      <Label htmlFor='route-origin-hub'>Hub xuất phát</Label>
                      <TmsCombobox
                        id='route-origin-hub'
                        value={formValues.originHubId}
                        onValueChange={updateOriginHub}
                        options={hubComboboxOptions}
                        placeholder='Chọn hub xuất phát'
                        emptyText='Không tìm thấy hub'
                      />
                    </div>
                  ) : (
                    <div className='space-y-2'>
                      <Label htmlFor='route-origin-post-office'>
                        Bưu cục xuất phát
                      </Label>
                      <TmsCombobox
                        id='route-origin-post-office'
                        value={formValues.originPostOfficeCode}
                        onValueChange={updateOriginPostOffice}
                        options={postOfficeFilterOptions}
                        placeholder='Chọn bưu cục xuất phát'
                        emptyText='Không tìm thấy bưu cục'
                      />
                    </div>
                  )}
                  {formValues.destinationType === 'HUB' ? (
                    <div className='space-y-2 md:col-span-2'>
                      <Label htmlFor='route-destination-hub'>Hub đích</Label>
                      <TmsCombobox
                        id='route-destination-hub'
                        value={formValues.destinationHubId}
                        onValueChange={updateDestinationHub}
                        options={destinationHubComboboxOptions}
                        placeholder='Chọn hub đích'
                        emptyText={
                          formValues.originType === 'POST_OFFICE' ||
                          formValues.originHubId
                            ? 'Không tìm thấy hub đích'
                            : 'Chọn hub xuất phát trước'
                        }
                        disabled={
                          formValues.originType === 'HUB' &&
                          !formValues.originHubId
                        }
                      />
                    </div>
                  ) : (
                    <div className='space-y-2 md:col-span-2'>
                      <Label htmlFor='route-destination-post-office'>
                        Bưu cục đích
                      </Label>
                      <TmsCombobox
                        id='route-destination-post-office'
                        value={formValues.destinationPostOfficeCode}
                        onValueChange={updateDestinationPostOffice}
                        options={mappedPostOfficeComboboxOptions}
                        placeholder={
                          formValues.originHubId
                            ? 'Chọn bưu cục đã liên kết'
                            : 'Chọn hub xuất phát trước'
                        }
                        emptyText={
                          formValues.originHubId
                            ? 'Không tìm thấy bưu cục đã liên kết'
                            : 'Chọn hub xuất phát trước'
                        }
                        disabled={!formValues.originHubId}
                        loading={isFetchingMappedPostOffices}
                      />
                    </div>
                  )}
                  <div className='space-y-2'>
                    <Label htmlFor='route-vehicle'>
                      {formValues.originType === 'POST_OFFICE' ||
                      formValues.destinationType === 'POST_OFFICE'
                        ? 'Xe *'
                        : 'Xe'}
                    </Label>
                    <TmsCombobox
                      id='route-vehicle'
                      value={
                        formValues.vehicleId ||
                        (formValues.originType === 'HUB' &&
                        formValues.destinationType === 'HUB'
                          ? NO_VEHICLE_VALUE
                          : '')
                      }
                      onValueChange={(value) =>
                        updateField(
                          'vehicleId',
                          value === NO_VEHICLE_VALUE ? '' : value
                        )
                      }
                      options={formVehicleComboboxOptions}
                      placeholder={
                        formValues.originType === 'POST_OFFICE' ||
                        formValues.destinationType === 'POST_OFFICE'
                          ? 'Chọn xe'
                          : 'Xe tùy chọn'
                      }
                      emptyText={
                        formOperatingHubNumericId
                          ? 'Không tìm thấy xe hoạt động đã gán tài xế'
                          : formValues.originType === 'POST_OFFICE'
                            ? 'Chọn hub đích trước'
                            : 'Chọn hub xuất phát trước'
                      }
                      disabled={!formOperatingHubNumericId}
                      loading={
                        isFetchingFormVehicles ||
                        isFetchingFormDriverAssignments
                      }
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='route-status'>Trạng thái</Label>
                    <TmsCombobox
                      id='route-status'
                      value={formValues.status}
                      onValueChange={(value) =>
                        updateField('status', value as SecondMileRouteStatus)
                      }
                      options={ROUTE_STATUS_OPTIONS}
                      placeholder='Chọn trạng thái'
                      emptyText='Không tìm thấy trạng thái'
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='distance'>
                      Khoảng cách di chuyển thực tế (km)
                    </Label>
                    <Input
                      id='distance'
                      type='number'
                      min='0'
                      step='0.1'
                      value={formValues.estimatedDistanceKm}
                      onChange={(event) =>
                        updateField('estimatedDistanceKm', event.target.value)
                      }
                      placeholder={
                        isEstimatingTravelMetrics
                          ? 'Đang tính...'
                          : 'Tùy chọn'
                      }
                    />
                  </div>
                  <div className='space-y-2'>
                    <Label htmlFor='duration'>Thời lượng (phút)</Label>
                    <Input
                      id='duration'
                      type='number'
                      min='0'
                      step='1'
                      value={formValues.estimatedDurationMinutes}
                      onChange={(event) =>
                        updateField(
                          'estimatedDurationMinutes',
                          event.target.value
                        )
                      }
                      placeholder={
                        isEstimatingTravelMetrics
                          ? 'Đang tính...'
                          : 'Tùy chọn'
                      }
                    />
                  </div>
                  <div className='space-y-2 md:col-span-2'>
                    <Label htmlFor='departure-time'>
                      Giờ xuất phát cố định
                    </Label>
                    <Input
                      id='departure-time'
                      type='time'
                      value={formValues.fixedDepartureTime}
                      onChange={(event) =>
                        updateField('fixedDepartureTime', event.target.value)
                      }
                    />
                  </div>
                  <div className='space-y-2 md:col-span-2'>
                    <Label htmlFor='note'>Ghi chú</Label>
                    <Textarea
                      id='note'
                      value={formValues.note}
                      onChange={(event) =>
                        updateField('note', event.target.value)
                      }
                      className='min-h-24'
                    />
                  </div>
                </div>
              </div>

              <div className='min-w-0 xl:sticky xl:top-0 xl:self-start'>
                <SecondMileRoutesMap
                  lines={formMapLines}
                  selectedRouteId={editingRouteId ?? -1}
                  title='Bản đồ tuyến đang tạo'
                  description={
                    formMapLines.length > 0
                      ? 'Xem trước đường nối giữa điểm xuất phát và hub đích.'
                      : 'Chọn điểm xuất phát và hub đích có tọa độ để xem trước tuyến.'
                  }
                  emptyText='Chưa đủ tọa độ điểm xuất phát và hub đích để hiển thị tuyến.'
                  mapClassName='h-[520px] min-h-[420px] overflow-hidden rounded-lg border'
                />
              </div>
            </div>

            <div className='flex justify-end gap-2 border-t pt-4'>
              <Button
                type='button'
                variant='outline'
                onClick={() => setIsFormOpen(false)}
              >
                Hủy
              </Button>
              <Button
                type='submit'
                disabled={isSavingRoute || isRouteFormDependencyLoading}
              >
                {isSavingRoute
                  ? 'Đang lưu...'
                  : isRouteFormDependencyLoading
                    ? 'Đang tải dữ liệu...'
                    : formMode === 'create'
                      ? 'Tạo tuyến'
                      : 'Cập nhật tuyến'}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isDeleting) {
            setDeleteTarget(null);
          }
        }}
        title='Xóa tuyến'
        description={
          deleteTarget
            ? `Xóa tuyến ${deleteTarget.routeCode} - ${deleteTarget.routeName}?`
            : undefined
        }
        confirmText='Xóa'
        variant='destructive'
        isLoading={isDeleting}
        onConfirm={async () => {
          if (!deleteTarget) {
            return;
          }
          try {
            await deleteRoute(deleteTarget.id).unwrap();
            notification.success('Đã xóa tuyến.');
            setDeleteTarget(null);
            void refetch();
          } catch (deleteError) {
            notification.error('Xóa tuyến thất bại.', {
              description: getErrorMessage(deleteError),
            });
          }
        }}
      />
    </>
  );
}
