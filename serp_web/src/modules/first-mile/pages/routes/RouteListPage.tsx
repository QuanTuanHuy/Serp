/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile routes management page
 */

'use client';

import React from 'react';
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
  Textarea,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import { Plus, RefreshCw, Search, ShieldAlert, Trash2 } from 'lucide-react';
import {
  useCreateSecondMileRouteMutation,
  useDeleteSecondMileRouteMutation,
  useGetHubsQuery,
  useGetPostOfficesQuery,
  useGetSecondMileRoutesQuery,
  useGetSecondMileVehiclesQuery,
  useUpdateSecondMileRouteMutation,
} from '../../api';
import type {
  Hub,
  PostOffice,
  SecondMileCreateRouteRequest,
  SecondMileRoute,
  SecondMileRouteDestinationType,
  SecondMileRouteStatus,
  SecondMileUpdateRouteRequest,
} from '../../types';
import { SecondMileRoutesMap, type RouteMapLine } from './components/SecondMileRoutesMap';

type RouteFormMode = 'create' | 'edit';

interface RouteFormState {
  routeCode: string;
  routeName: string;
  originHubId: string;
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

const DEFAULT_FORM: RouteFormState = {
  routeCode: '',
  routeName: '',
  originHubId: '',
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

function toFormState(route: SecondMileRoute): RouteFormState {
  return {
    routeCode: route.routeCode ?? '',
    routeName: route.routeName ?? '',
    originHubId: route.originHubId ? String(route.originHubId) : '',
    destinationType: route.destinationType,
    destinationHubId: route.destinationHubId ? String(route.destinationHubId) : '',
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
    'ALL' | SecondMileRouteStatus
  >('ALL');
  const [selectedRouteId, setSelectedRouteId] = React.useState<number | undefined>();

  const [isFormOpen, setIsFormOpen] = React.useState(false);
  const [formMode, setFormMode] = React.useState<RouteFormMode>('create');
  const [editingRouteId, setEditingRouteId] = React.useState<number | null>(null);
  const [formValues, setFormValues] = React.useState<RouteFormState>(DEFAULT_FORM);
  const [deleteTarget, setDeleteTarget] = React.useState<SecondMileRoute | null>(
    null
  );

  const { data: routesData, isFetching, refetch } = useGetSecondMileRoutesQuery(
    {
      page,
      size: PAGE_SIZE,
      keyword,
      status: selectedStatus === 'ALL' ? undefined : selectedStatus,
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

  const [createRoute, { isLoading: isCreating }] =
    useCreateSecondMileRouteMutation();
  const [updateRoute, { isLoading: isUpdating }] =
    useUpdateSecondMileRouteMutation();
  const [deleteRoute, { isLoading: isDeleting }] =
    useDeleteSecondMileRouteMutation();

  const hubs = hubsData?.items ?? [];
  const postOffices = postOfficesData?.items ?? [];
  const routes = routesData?.items ?? [];

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

  const mapLines = React.useMemo<RouteMapLine[]>(() => {
    const lines: RouteMapLine[] = [];

    for (const route of routes) {
      const originHub = hubById[route.originHubId];
      if (
        !originHub ||
        originHub.latitude === undefined ||
        originHub.longitude === undefined
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
            name: `${originHub.code} - ${originHub.name}`,
            latitude: originHub.latitude,
            longitude: originHub.longitude,
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
          name: `${originHub.code} - ${originHub.name}`,
          latitude: originHub.latitude,
          longitude: originHub.longitude,
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
    if (!values.originHubId) return 'Origin hub is required.';
    if (values.destinationType === 'HUB' && !values.destinationHubId) {
      return 'Destination hub is required.';
    }
    if (
      values.destinationType === 'POST_OFFICE' &&
      !values.destinationPostOfficeCode.trim()
    ) {
      return 'Destination post office is required.';
    }
    return null;
  };

  const buildRequestBody = (
    values: RouteFormState
  ): SecondMileCreateRouteRequest | SecondMileUpdateRouteRequest => {
    const body: SecondMileCreateRouteRequest = {
      route_code: values.routeCode.trim(),
      route_name: values.routeName.trim(),
      origin_hub_id: Number(values.originHubId),
      destination_type: values.destinationType,
      vehicle_id: values.vehicleId ? Number(values.vehicleId) : undefined,
      estimated_distance_km: parseOptionalNumber(values.estimatedDistanceKm),
      estimated_duration_minutes: parseOptionalNumber(
        values.estimatedDurationMinutes
      ),
      fixed_departure_time: values.fixedDepartureTime || undefined,
      note: values.note.trim() || undefined,
      status: values.status,
    };

    if (values.destinationType === 'HUB') {
      body.destination_hub_id = Number(values.destinationHubId);
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
              Fixed transport routes between hub-hub and hub-post office.
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
            <CardTitle>Search</CardTitle>
          </CardHeader>
          <CardContent>
            <form
              className='flex flex-col gap-2 md:flex-row'
              onSubmit={(event) => {
                event.preventDefault();
                setPage(0);
                setKeyword(searchInput.trim() || undefined);
              }}
            >
              <div className='relative flex-1'>
                <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                <Input
                  className='pl-10'
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                  placeholder='Route code or name...'
                />
              </div>
              <Select
                value={selectedStatus}
                onValueChange={(value) => {
                  setSelectedStatus(value as 'ALL' | SecondMileRouteStatus);
                  setPage(0);
                }}
              >
                <SelectTrigger className='w-full md:w-[180px]'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='ALL'>All statuses</SelectItem>
                  {ROUTE_STATUS_OPTIONS.map((statusOption) => (
                    <SelectItem key={statusOption.value} value={statusOption.value}>
                      {statusOption.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button type='submit'>Search</Button>
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
                    <TableHead>Destination</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className='text-right'>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {routes.length === 0 ? (
                    <TableRow>
                      <TableCell
                        colSpan={6}
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
                        <TableCell className='font-medium'>{route.routeCode}</TableCell>
                        <TableCell>{route.routeName}</TableCell>
                        <TableCell>
                          {hubById[route.originHubId]?.code ?? route.originHubId}
                        </TableCell>
                        <TableCell>
                          {route.destinationType === 'HUB'
                            ? hubById[route.destinationHubId ?? -1]?.code ??
                              route.destinationHubId
                            : route.destinationPostOfficeCode}
                        </TableCell>
                        <TableCell>
                          <Badge variant={getStatusBadgeVariant(route.status)}>
                            {route.status}
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
          if (!isCreating && !isUpdating) {
            setIsFormOpen(open);
            if (!open) {
              setEditingRouteId(null);
            }
          }
        }}
      >
        <DialogContent className='max-w-2xl'>
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
                  await createRoute(body as SecondMileCreateRouteRequest).unwrap();
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
            <div className='grid gap-4 md:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='route-code'>Route code</Label>
                <Input
                  id='route-code'
                  value={formValues.routeCode}
                  onChange={(event) => updateField('routeCode', event.target.value)}
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='route-name'>Route name</Label>
                <Input
                  id='route-name'
                  value={formValues.routeName}
                  onChange={(event) => updateField('routeName', event.target.value)}
                />
              </div>
              <div className='space-y-2'>
                <Label>Origin hub</Label>
                <Select
                  value={formValues.originHubId || undefined}
                  onValueChange={(value) => updateField('originHubId', value)}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select origin hub' />
                  </SelectTrigger>
                  <SelectContent>
                    {hubs.map((hub) => (
                      <SelectItem key={hub.id} value={String(hub.id)}>
                        {hub.code} - {hub.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2'>
                <Label>Destination type</Label>
                <Select
                  value={formValues.destinationType}
                  onValueChange={(value) =>
                    updateField('destinationType', value as SecondMileRouteDestinationType)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {ROUTE_DESTINATION_OPTIONS.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              {formValues.destinationType === 'HUB' ? (
                <div className='space-y-2 md:col-span-2'>
                  <Label>Destination hub</Label>
                  <Select
                    value={formValues.destinationHubId || undefined}
                    onValueChange={(value) => updateField('destinationHubId', value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select destination hub' />
                    </SelectTrigger>
                    <SelectContent>
                      {hubs.map((hub) => (
                        <SelectItem key={hub.id} value={String(hub.id)}>
                          {hub.code} - {hub.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              ) : (
                <div className='space-y-2 md:col-span-2'>
                  <Label>Destination post office</Label>
                  <Select
                    value={formValues.destinationPostOfficeCode || undefined}
                    onValueChange={(value) =>
                      updateField('destinationPostOfficeCode', value)
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder='Select destination post office' />
                    </SelectTrigger>
                    <SelectContent>
                      {postOffices.map((postOffice) => (
                        <SelectItem key={postOffice.code} value={postOffice.code}>
                          {postOffice.code} - {postOffice.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              )}
              <div className='space-y-2'>
                <Label>Vehicle</Label>
                <Select
                  value={formValues.vehicleId || '__none__'}
                  onValueChange={(value) =>
                    updateField('vehicleId', value === '__none__' ? '' : value)
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Optional vehicle' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='__none__'>No vehicle</SelectItem>
                    {vehiclesData?.items.map((vehicle) => (
                      <SelectItem key={vehicle.id} value={String(vehicle.id)}>
                        {vehicle.licensePlate}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2'>
                <Label>Status</Label>
                <Select
                  value={formValues.status}
                  onValueChange={(value) =>
                    updateField('status', value as SecondMileRouteStatus)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {ROUTE_STATUS_OPTIONS.map((statusOption) => (
                      <SelectItem key={statusOption.value} value={statusOption.value}>
                        {statusOption.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2'>
                <Label htmlFor='distance'>Distance (km)</Label>
                <Input
                  id='distance'
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
                  value={formValues.estimatedDurationMinutes}
                  onChange={(event) =>
                    updateField('estimatedDurationMinutes', event.target.value)
                  }
                  placeholder='Optional'
                />
              </div>
              <div className='space-y-2 md:col-span-2'>
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
              <div className='space-y-2 md:col-span-2'>
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
              <Button type='submit' disabled={isCreating || isUpdating}>
                {isCreating || isUpdating
                  ? 'Saving...'
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
