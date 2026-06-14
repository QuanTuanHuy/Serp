/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile routes management page
 */

'use client';

import React from 'react';
import { Plus, RefreshCw, Search, ShieldAlert, Trash2, X } from 'lucide-react';

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
  routeCode: string;
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

const PAGE_SIZE = 20;
const ALL_FILTER_VALUE = 'ALL';
const NO_VEHICLE_VALUE = '__none__';

type RouteDestinationFilter =
  | typeof ALL_FILTER_VALUE
  | SecondMileRouteDestinationType;
type RouteOriginFilter = typeof ALL_FILTER_VALUE | SecondMileRouteEndpointType;

const DEFAULT_FORM: RouteFormState = {
  routeCode: '',
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
  { value: 'ACTIVE', label: 'Active' },
  { value: 'INACTIVE', label: 'Inactive' },
];

const ROUTE_DESTINATION_OPTIONS: Array<{
  value: SecondMileRouteDestinationType;
  label: string;
}> = [
  { value: 'HUB', label: 'Hub' },
  { value: 'POST_OFFICE', label: 'Post office' },
];

const ROUTE_ENDPOINT_OPTIONS: Array<{
  value: SecondMileRouteEndpointType;
  label: string;
}> = [
  { value: 'HUB', label: 'Hub' },
  { value: 'POST_OFFICE', label: 'Post office' },
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
    return `${label} must be a valid number.`;
  }
  if (numeric < 0) {
    return `${label} cannot be negative.`;
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
    return `${label} must be a whole number.`;
  }
  return null;
}

function formatStatusLabel(status: SecondMileRouteStatus): string {
  return status === 'ACTIVE' ? 'Active' : 'Inactive';
}

function formatDestinationTypeLabel(
  destinationType: SecondMileRouteDestinationType
): string {
  return destinationType === 'HUB' ? 'Hub' : 'Post office';
}

function formatRouteMetric(value?: number, suffix?: string): string {
  if (value === undefined || value === null) {
    return '-';
  }
  return suffix ? `${value} ${suffix}` : String(value);
}

function toFormState(route: SecondMileRoute): RouteFormState {
  return {
    routeCode: route.routeCode ?? '',
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
  const [deleteTarget, setDeleteTarget] =
    React.useState<SecondMileRoute | null>(null);

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
    { value: ALL_FILTER_VALUE, label: 'All statuses' },
    ...ROUTE_STATUS_OPTIONS,
  ];
  const destinationTypeFilterOptions = [
    { value: ALL_FILTER_VALUE, label: 'All destinations' },
    ...ROUTE_DESTINATION_OPTIONS,
  ];
  const originTypeFilterOptions = [
    { value: ALL_FILTER_VALUE, label: 'All origins' },
    ...ROUTE_ENDPOINT_OPTIONS,
  ];
  const formDestinationTypeOptions =
    formValues.originType === 'POST_OFFICE'
      ? ROUTE_DESTINATION_OPTIONS.filter((option) => option.value === 'HUB')
      : ROUTE_DESTINATION_OPTIONS;
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
      return 'No driver';
    }
    const assignment = driverAssignmentByStaffId[assignedStaffId];
    return (
      assignment?.staffFullName ||
      assignment?.staffCode ||
      vehicle.assignedStaffFullName ||
      vehicle.assignedStaffCode ||
      `Driver #${assignedStaffId}`
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
      ? [{ value: NO_VEHICLE_VALUE, label: 'No vehicle' }]
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

  const getHubLabel = (hubId?: number) => {
    if (hubId === undefined || hubId === null) {
      return '-';
    }
    const hub = hubById[hubId];
    return hub ? `${hub.code} - ${hub.name}` : `Hub #${hubId}`;
  };

  const getPostOfficeLabel = (postOfficeCode?: string) => {
    if (!postOfficeCode) {
      return '-';
    }
    const postOffice = postOfficeByCode[postOfficeCode];
    return postOffice
      ? `${postOffice.code} - ${postOffice.name}`
      : postOfficeCode;
  };

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
    return vehicle ? vehicle.licensePlate : `Vehicle #${vehicleId}`;
  };

  const getDestinationLabel = (route: SecondMileRoute) => {
    if (route.destinationType === 'HUB') {
      return getHubLabel(route.destinationHubId);
    }
    return getPostOfficeLabel(route.destinationPostOfficeCode);
  };

  const mapLines = React.useMemo<RouteMapLine[]>(() => {
    const lines: RouteMapLine[] = [];

    for (const route of routes) {
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
  }, [hubById, postOfficeByCode, routes]);

  const updateField = <K extends keyof RouteFormState>(
    key: K,
    value: RouteFormState[K]
  ) => setFormValues((prev) => ({ ...prev, [key]: value }));

  const updateOriginType = (value: SecondMileRouteEndpointType) => {
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
    }));
  };

  const updateOriginHub = (value: string) => {
    setFormValues((prev) => ({
      ...prev,
      originHubId: value,
      destinationHubId: '',
      destinationPostOfficeCode: '',
      vehicleId: '',
    }));
  };

  const updateOriginPostOffice = (value: string) => {
    setFormValues((prev) => ({
      ...prev,
      originPostOfficeCode: value,
      vehicleId: '',
    }));
  };

  const updateDestinationType = (value: SecondMileRouteDestinationType) => {
    setFormValues((prev) => ({
      ...prev,
      destinationType: value,
      destinationHubId: value === 'HUB' ? prev.destinationHubId : '',
      destinationPostOfficeCode:
        value === 'POST_OFFICE' ? prev.destinationPostOfficeCode : '',
      vehicleId: '',
    }));
  };

  const clearRouteFilters = () => {
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
    setPage(0);
  };

  const openCreateDialog = () => {
    setFormMode('create');
    setEditingRouteId(null);
    setFormValues(DEFAULT_FORM);
    setIsFormOpen(true);
  };

  const openEditDialog = (route: SecondMileRoute) => {
    setFormMode('edit');
    setEditingRouteId(route.id);
    setFormValues(toFormState(route));
    setIsFormOpen(true);
  };

  const validateForm = (values: RouteFormState): string | null => {
    if (!values.routeCode.trim()) return 'Route code is required.';
    if (!values.routeName.trim()) return 'Route name is required.';
    if (values.originType === 'HUB' && !values.originHubId) {
      return 'Origin hub is required.';
    }
    if (
      values.originType === 'POST_OFFICE' &&
      !values.originPostOfficeCode.trim()
    ) {
      return 'Origin post office is required.';
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
      return 'Post office origin routes must target a hub.';
    }
    if (values.destinationType === 'HUB' && !values.destinationHubId) {
      return 'Destination hub is required.';
    }
    if (
      values.destinationType === 'HUB' &&
      originHubId !== undefined &&
      destinationHubId !== undefined &&
      originHubId === destinationHubId
    ) {
      return 'Origin hub and destination hub must be different.';
    }
    if (values.originType === 'POST_OFFICE' && destinationHubId !== undefined) {
      const originPostOffice =
        postOfficeByCode[values.originPostOfficeCode.trim()];
      if (
        originPostOffice?.hubId !== undefined &&
        originPostOffice.hubId !== destinationHubId
      ) {
        return 'Destination hub must match the origin post office hub.';
      }
    }
    if (
      values.destinationType === 'POST_OFFICE' &&
      !values.destinationPostOfficeCode.trim()
    ) {
      return 'Destination post office is required.';
    }
    if (values.destinationType === 'POST_OFFICE' && !values.vehicleId) {
      return 'Vehicle is required for hub-post office routes.';
    }
    if (values.originType === 'POST_OFFICE' && !values.vehicleId) {
      return 'Vehicle is required for post office-hub routes.';
    }
    if (
      values.originType === 'HUB' &&
      values.destinationType === 'POST_OFFICE'
    ) {
      if (isFetchingMappedPostOffices) {
        return 'Post office mappings are still loading.';
      }
      const mappedCodes = new Set(
        mappedPostOffices.map((mapping) => mapping.postOfficeCode)
      );
      if (!mappedCodes.has(values.destinationPostOfficeCode.trim())) {
        return 'Destination post office must be mapped to the origin hub.';
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
          return 'Vehicle must belong to the operating hub.';
        }
        if (selectedVehicle.status !== 'ACTIVE') {
          return 'Vehicle must be active.';
        }
        if (
          selectedVehicle.assignedStaffId === undefined ||
          selectedVehicle.assignedStaffId === null
        ) {
          return 'Vehicle must have an assigned driver.';
        }
      }
    }
    const distanceError = validateOptionalNonNegativeNumber(
      'Distance',
      values.estimatedDistanceKm
    );
    if (distanceError) {
      return distanceError;
    }
    const durationError = validateOptionalNonNegativeInteger(
      'Duration',
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
      route_code: values.routeCode.trim(),
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
            Access denied
          </CardTitle>
          <CardDescription>
            Route management requires TMS_ADMIN role.
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
            <h1 className='text-2xl font-bold tracking-tight'>Routes</h1>
            <p className='text-muted-foreground'>
              Fixed transport routes between hubs and post offices.
            </p>
          </div>
          <div className='flex gap-2'>
            <Button variant='outline' onClick={() => void refetch()}>
              <RefreshCw className='mr-2 h-4 w-4' />
              Refresh
            </Button>
            <Button onClick={openCreateDialog}>
              <Plus className='mr-2 h-4 w-4' />
              New route
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Filters</CardTitle>
          </CardHeader>
          <CardContent>
            <form
              className='grid gap-3 md:grid-cols-2 xl:grid-cols-8'
              onSubmit={(event) => {
                event.preventDefault();
                setPage(0);
                setKeyword(searchInput.trim() || undefined);
              }}
            >
              <div className='relative md:col-span-2 xl:col-span-2'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  className='pl-10'
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                  placeholder='Route code or name...'
                />
              </div>
              <TmsCombobox
                id='route-filter-status'
                value={selectedStatus}
                onValueChange={(value) => {
                  setSelectedStatus(
                    value as typeof ALL_FILTER_VALUE | SecondMileRouteStatus
                  );
                  setPage(0);
                }}
                options={statusFilterOptions}
                placeholder='All statuses'
                emptyText='No statuses found'
                className='w-full'
              />
              <TmsCombobox
                id='route-filter-origin-type'
                value={selectedOriginType}
                onValueChange={(value) => {
                  setSelectedOriginType(value as RouteOriginFilter);
                  setSelectedOriginHubId('');
                  setSelectedOriginPostOfficeCode('');
                  setSelectedVehicleId('');
                  setPage(0);
                }}
                options={originTypeFilterOptions}
                placeholder='All origins'
                emptyText='No origin types found'
                className='w-full'
              />
              {selectedOriginType === 'POST_OFFICE' ? (
                <TmsCombobox
                  id='route-filter-origin-post-office'
                  value={selectedOriginPostOfficeCode}
                  onValueChange={(value) => {
                    setSelectedOriginPostOfficeCode(value);
                    setPage(0);
                  }}
                  options={postOfficeFilterOptions}
                  placeholder='Origin post office'
                  emptyText='No post offices found'
                  clearable
                  className='w-full'
                />
              ) : (
                <TmsCombobox
                  id='route-filter-origin-hub'
                  value={selectedOriginHubId}
                  onValueChange={(value) => {
                    setSelectedOriginHubId(value);
                    setSelectedVehicleId('');
                    setPage(0);
                  }}
                  options={hubComboboxOptions}
                  placeholder='Origin hub'
                  emptyText='No hubs found'
                  clearable
                  className='w-full'
                />
              )}
              <TmsCombobox
                id='route-filter-destination-type'
                value={selectedDestinationType}
                onValueChange={(value) => {
                  setSelectedDestinationType(value as RouteDestinationFilter);
                  setSelectedDestinationHubId('');
                  setSelectedDestinationPostOfficeCode('');
                  setPage(0);
                }}
                options={destinationTypeFilterOptions}
                placeholder='All destinations'
                emptyText='No destination types found'
                className='w-full'
              />
              {selectedDestinationType === 'HUB' ? (
                <TmsCombobox
                  id='route-filter-destination-hub'
                  value={selectedDestinationHubId}
                  onValueChange={(value) => {
                    setSelectedDestinationHubId(value);
                    setPage(0);
                  }}
                  options={hubComboboxOptions}
                  placeholder='Destination hub'
                  emptyText='No hubs found'
                  clearable
                  className='w-full'
                />
              ) : selectedDestinationType === 'POST_OFFICE' ? (
                <TmsCombobox
                  id='route-filter-destination-post-office'
                  value={selectedDestinationPostOfficeCode}
                  onValueChange={(value) => {
                    setSelectedDestinationPostOfficeCode(value);
                    setPage(0);
                  }}
                  options={postOfficeFilterOptions}
                  placeholder='Destination post office'
                  emptyText='No post offices found'
                  clearable
                  className='w-full'
                />
              ) : (
                <TmsCombobox
                  id='route-filter-vehicle'
                  value={selectedVehicleId}
                  onValueChange={(value) => {
                    setSelectedVehicleId(value);
                    setPage(0);
                  }}
                  options={routeVehicleFilterOptions}
                  placeholder='Vehicle'
                  emptyText='No vehicles found'
                  clearable
                  className='w-full'
                />
              )}
              {selectedDestinationType !== ALL_FILTER_VALUE && (
                <TmsCombobox
                  id='route-filter-vehicle'
                  value={selectedVehicleId}
                  onValueChange={(value) => {
                    setSelectedVehicleId(value);
                    setPage(0);
                  }}
                  options={routeVehicleFilterOptions}
                  placeholder='Vehicle'
                  emptyText='No vehicles found'
                  clearable
                  className='w-full'
                />
              )}
              <div className='flex gap-2 md:col-span-2 xl:col-span-7 xl:justify-end'>
                <Button type='submit'>
                  <Search className='mr-2 h-4 w-4' />
                  Search
                </Button>
                <Button
                  type='button'
                  variant='outline'
                  onClick={clearRouteFilters}
                >
                  <X className='mr-2 h-4 w-4' />
                  Reset
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>

        <SecondMileRoutesMap
          lines={mapLines}
          selectedRouteId={selectedRouteId}
        />

        <Card>
          <CardHeader>
            <CardTitle>Route list</CardTitle>
            <CardDescription>
              Click a row to highlight that route on map.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='overflow-x-auto'>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Route code</TableHead>
                    <TableHead>Route name</TableHead>
                    <TableHead>Origin</TableHead>
                    <TableHead>Destination type</TableHead>
                    <TableHead>Destination</TableHead>
                    <TableHead>Assigned vehicle</TableHead>
                    <TableHead>Departure</TableHead>
                    <TableHead>Distance / duration</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className='text-right'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {routes.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={10}
                        className='py-8 text-center text-muted-foreground'
                      >
                        {isFetching ? 'Loading routes...' : 'No routes found'}
                      </TableCell>
                    </TableRow>
                  ) : (
                    routes.map((route) => (
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
                            'min'
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
                              size='sm'
                              onClick={(event) => {
                                event.stopPropagation();
                                openEditDialog(route);
                              }}
                            >
                              Edit
                            </Button>
                            <Button
                              variant='destructive'
                              size='sm'
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
                  Page {(routesData?.currentPage ?? 0) + 1} /{' '}
                  {routesData?.totalPages ?? 1}
                </div>
                <div className='flex gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!routesData?.hasPrevious}
                    onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                  >
                    Previous
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!routesData?.hasNext}
                    onClick={() => setPage((prev) => prev + 1)}
                  >
                    Next
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
        <DialogContent className='max-h-[85vh] max-w-5xl overflow-y-auto'>
          <DialogHeader>
            <DialogTitle>
              {formMode === 'create' ? 'Create route' : 'Update route'}
            </DialogTitle>
            <DialogDescription>
              Configure a fixed line for second-mile transport.
            </DialogDescription>
          </DialogHeader>

          <form
            className='space-y-4'
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
                  notification.success('Route created.');
                  if (page !== 0) setPage(0);
                } else if (editingRouteId !== null) {
                  await updateRoute({
                    id: editingRouteId,
                    body: body as SecondMileUpdateRouteRequest,
                  }).unwrap();
                  notification.success('Route updated.');
                }
                setIsFormOpen(false);
                setEditingRouteId(null);
                void refetch();
              } catch (errorCreateUpdate) {
                notification.error('Save failed.', {
                  description: getErrorMessage(errorCreateUpdate),
                });
              }
            }}
          >
            <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-3'>
              <div className='space-y-2'>
                <Label htmlFor='route-code'>Route code</Label>
                <Input
                  id='route-code'
                  value={formValues.routeCode}
                  onChange={(event) =>
                    updateField('routeCode', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='route-name'>Route name</Label>
                <Input
                  id='route-name'
                  value={formValues.routeName}
                  onChange={(event) =>
                    updateField('routeName', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='route-origin-type'>Origin type</Label>
                <TmsCombobox
                  id='route-origin-type'
                  value={formValues.originType}
                  onValueChange={(value) =>
                    updateOriginType(value as SecondMileRouteEndpointType)
                  }
                  options={ROUTE_ENDPOINT_OPTIONS}
                  placeholder='Select origin type'
                  emptyText='No origin types found'
                />
              </div>
              {formValues.originType === 'HUB' ? (
                <div className='space-y-2'>
                  <Label htmlFor='route-origin-hub'>Origin hub</Label>
                  <TmsCombobox
                    id='route-origin-hub'
                    value={formValues.originHubId}
                    onValueChange={updateOriginHub}
                    options={hubComboboxOptions}
                    placeholder='Select origin hub'
                    emptyText='No hubs found'
                  />
                </div>
              ) : (
                <div className='space-y-2'>
                  <Label htmlFor='route-origin-post-office'>
                    Origin post office
                  </Label>
                  <TmsCombobox
                    id='route-origin-post-office'
                    value={formValues.originPostOfficeCode}
                    onValueChange={updateOriginPostOffice}
                    options={postOfficeFilterOptions}
                    placeholder='Select origin post office'
                    emptyText='No post offices found'
                  />
                </div>
              )}
              <div className='space-y-2'>
                <Label htmlFor='route-destination-type'>Destination type</Label>
                <TmsCombobox
                  id='route-destination-type'
                  value={formValues.destinationType}
                  onValueChange={(value) =>
                    updateDestinationType(
                      value as SecondMileRouteDestinationType
                    )
                  }
                  options={formDestinationTypeOptions}
                  placeholder='Select destination type'
                  emptyText='No destination types found'
                />
              </div>
              {formValues.destinationType === 'HUB' ? (
                <div className='space-y-2 md:col-span-2'>
                  <Label htmlFor='route-destination-hub'>Destination hub</Label>
                  <TmsCombobox
                    id='route-destination-hub'
                    value={formValues.destinationHubId}
                    onValueChange={(value) =>
                      updateField('destinationHubId', value)
                    }
                    options={destinationHubComboboxOptions}
                    placeholder='Select destination hub'
                    emptyText={
                      formValues.originType === 'POST_OFFICE' ||
                      formValues.originHubId
                        ? 'No destination hubs found'
                        : 'Select origin hub first'
                    }
                    disabled={
                      formValues.originType === 'HUB' && !formValues.originHubId
                    }
                  />
                </div>
              ) : (
                <div className='space-y-2 md:col-span-2'>
                  <Label htmlFor='route-destination-post-office'>
                    Destination post office
                  </Label>
                  <TmsCombobox
                    id='route-destination-post-office'
                    value={formValues.destinationPostOfficeCode}
                    onValueChange={(value) =>
                      updateField('destinationPostOfficeCode', value)
                    }
                    options={mappedPostOfficeComboboxOptions}
                    placeholder={
                      formValues.originHubId
                        ? 'Select mapped post office'
                        : 'Select origin hub first'
                    }
                    emptyText={
                      formValues.originHubId
                        ? 'No mapped post offices found'
                        : 'Select origin hub first'
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
                    ? 'Vehicle *'
                    : 'Vehicle'}
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
                      ? 'Select vehicle'
                      : 'Optional vehicle'
                  }
                  emptyText={
                    formOperatingHubNumericId
                      ? 'No active driver-backed vehicles found'
                      : formValues.originType === 'POST_OFFICE'
                        ? 'Select destination hub first'
                        : 'Select origin hub first'
                  }
                  disabled={!formOperatingHubNumericId}
                  loading={
                    isFetchingFormVehicles || isFetchingFormDriverAssignments
                  }
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='route-status'>Status</Label>
                <TmsCombobox
                  id='route-status'
                  value={formValues.status}
                  onValueChange={(value) =>
                    updateField('status', value as SecondMileRouteStatus)
                  }
                  options={ROUTE_STATUS_OPTIONS}
                  placeholder='Select status'
                  emptyText='No statuses found'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='distance'>Distance (km)</Label>
                <Input
                  id='distance'
                  type='number'
                  min='0'
                  step='0.1'
                  value={formValues.estimatedDistanceKm}
                  onChange={(event) =>
                    updateField('estimatedDistanceKm', event.target.value)
                  }
                  placeholder='Optional'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='duration'>Duration (minutes)</Label>
                <Input
                  id='duration'
                  type='number'
                  min='0'
                  step='1'
                  value={formValues.estimatedDurationMinutes}
                  onChange={(event) =>
                    updateField('estimatedDurationMinutes', event.target.value)
                  }
                  placeholder='Optional'
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='departure-time'>Fixed departure time</Label>
                <Input
                  id='departure-time'
                  type='time'
                  value={formValues.fixedDepartureTime}
                  onChange={(event) =>
                    updateField('fixedDepartureTime', event.target.value)
                  }
                />
              </div>
              <div className='space-y-2 md:col-span-2 xl:col-span-3'>
                <Label htmlFor='note'>Note</Label>
                <Textarea
                  id='note'
                  value={formValues.note}
                  onChange={(event) => updateField('note', event.target.value)}
                />
              </div>
            </div>

            <div className='flex justify-end gap-2 border-t pt-4'>
              <Button
                type='button'
                variant='outline'
                onClick={() => setIsFormOpen(false)}
              >
                Cancel
              </Button>
              <Button
                type='submit'
                disabled={isSavingRoute || isRouteFormDependencyLoading}
              >
                {isSavingRoute
                  ? 'Saving...'
                  : isRouteFormDependencyLoading
                    ? 'Loading data...'
                    : formMode === 'create'
                      ? 'Create route'
                      : 'Update route'}
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
        title='Delete route'
        description={
          deleteTarget
            ? `Delete route ${deleteTarget.routeCode} - ${deleteTarget.routeName}?`
            : undefined
        }
        confirmText='Delete'
        variant='destructive'
        isLoading={isDeleting}
        onConfirm={async () => {
          if (!deleteTarget) {
            return;
          }
          try {
            await deleteRoute(deleteTarget.id).unwrap();
            notification.success('Route deleted.');
            setDeleteTarget(null);
            void refetch();
          } catch (deleteError) {
            notification.error('Delete failed.', {
              description: getErrorMessage(deleteError),
            });
          }
        }}
      />
    </>
  );
}
