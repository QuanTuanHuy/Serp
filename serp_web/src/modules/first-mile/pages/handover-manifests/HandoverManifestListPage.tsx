/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Post office to hub transport via handover manifests
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
  DialogFooter,
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
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import { Plus, RefreshCw, Truck } from 'lucide-react';
import {
  useCreateHandoverManifestMutation,
  useDriverCheckinHandoverManifestEndMutation,
  useDriverCheckinHandoverManifestStartMutation,
  useGetHandoverManifestsQuery,
  useGetHubPostOfficesQuery,
  useGetHubsQuery,
  useGetSecondMileOrdersQuery,
  useGetSecondMileRoutesQuery,
  useGetSecondMileVehiclesQuery,
} from '../../api';
import type {
  HandoverManifest,
  HandoverManifestStatus,
  Hub,
} from '../../types';

const PAGE_SIZE = 20;

const MANIFEST_STATUS_OPTIONS: Array<{
  value: HandoverManifestStatus;
  label: string;
}> = [
  { value: 'CREATED', label: 'Created' },
  { value: 'OUTBOUND_CONFIRMED', label: 'In transit' },
  { value: 'INBOUND_CONFIRMED', label: 'Received at hub' },
  { value: 'CANCELLED', label: 'Cancelled' },
];

const canManageHandoverManifests = (roles: string[]): boolean =>
  roles.includes('TMS_ADMIN') ||
  roles.includes('TMS_HUB_MANAGER') ||
  roles.includes('TMS_HUB_EMPLOYEE');

function getStatusBadgeVariant(
  status?: HandoverManifestStatus
): 'default' | 'secondary' | 'outline' {
  if (status === 'INBOUND_CONFIRMED') {
    return 'default';
  }
  if (status === 'OUTBOUND_CONFIRMED') {
    return 'outline';
  }
  return 'secondary';
}

export function HandoverManifestListPage() {
  const notification = useNotification();
  const roles = useAppSelector((state) => state.account.user.profile?.roles ?? []);
  const canAccess = canManageHandoverManifests(roles);

  const [page, setPage] = React.useState(0);
  const [filterHubId, setFilterHubId] = React.useState('');
  const [filterStatus, setFilterStatus] = React.useState<
    'ALL' | HandoverManifestStatus
  >('ALL');

  const [isCreateOpen, setIsCreateOpen] = React.useState(false);
  const [createHubId, setCreateHubId] = React.useState('');
  const [createPostOfficeCode, setCreatePostOfficeCode] = React.useState('');
  const [createVehicleId, setCreateVehicleId] = React.useState('');
  const [createRouteId, setCreateRouteId] = React.useState('');
  const [selectedOrderCodes, setSelectedOrderCodes] = React.useState<string[]>(
    []
  );

  const targetHubId = filterHubId ? Number(filterHubId) : undefined;
  const createHubNumericId = createHubId ? Number(createHubId) : undefined;

  const {
    data: manifestsData,
    isFetching,
    refetch,
  } = useGetHandoverManifestsQuery(
    {
      page,
      size: PAGE_SIZE,
      ...(targetHubId ? { targetHubId } : {}),
      ...(filterStatus !== 'ALL' ? { status: filterStatus } : {}),
    },
    { skip: !canAccess }
  );

  const { data: hubsData } = useGetHubsQuery(
    { page: 0, size: 200 },
    { skip: !canAccess }
  );

  const { data: hubPostOfficesData } = useGetHubPostOfficesQuery(
    { hubId: createHubNumericId ?? 0, page: 0, size: 200 },
    { skip: !canAccess || !createHubNumericId }
  );

  const { data: vehiclesData } = useGetSecondMileVehiclesQuery(
    {
      page: 0,
      size: 200,
      ...(createHubNumericId ? { hubId: createHubNumericId } : {}),
      status: 'ACTIVE',
    },
    { skip: !canAccess || !createHubNumericId }
  );

  const { data: routesData } = useGetSecondMileRoutesQuery(
    {
      page: 0,
      size: 200,
      ...(createHubNumericId ? { originHubId: createHubNumericId } : {}),
      destinationType: 'POST_OFFICE',
      status: 'ACTIVE',
      ...(createPostOfficeCode
        ? { destinationPostOfficeCode: createPostOfficeCode }
        : {}),
    },
    { skip: !canAccess || !createHubNumericId || !createPostOfficeCode }
  );

  const { data: readyOrdersData, isFetching: isLoadingReadyOrders } =
    useGetSecondMileOrdersQuery(
      {
        page: 0,
        size: 200,
        originPostOfficeCode: createPostOfficeCode,
        status: 'AT_ORIGIN_POST_OFFICE',
      },
      {
        skip:
          !canAccess || !createPostOfficeCode || !isCreateOpen,
      }
    );

  const [createManifest, { isLoading: isCreating }] =
    useCreateHandoverManifestMutation();
  const [driverCheckinStart, { isLoading: isConfirmingOutbound }] =
    useDriverCheckinHandoverManifestStartMutation();
  const [driverCheckinEnd, { isLoading: isConfirmingInbound }] =
    useDriverCheckinHandoverManifestEndMutation();

  const hubOptions = hubsData?.items ?? [];
  const postOfficeOptions = hubPostOfficesData?.items ?? [];
  const vehicleOptions = vehiclesData?.items ?? [];
  const routeOptions = routesData?.items ?? [];
  const readyOrders = readyOrdersData?.items ?? [];
  const manifests = manifestsData?.items ?? [];

  React.useEffect(() => {
    setCreatePostOfficeCode('');
    setCreateVehicleId('');
    setCreateRouteId('');
    setSelectedOrderCodes([]);
  }, [createHubId]);

  React.useEffect(() => {
    setCreateVehicleId('');
    setCreateRouteId('');
    setSelectedOrderCodes([]);
  }, [createPostOfficeCode]);

  React.useEffect(() => {
    setCreateRouteId('');
  }, [createVehicleId]);

  const filteredRoutes = React.useMemo(() => {
    if (!createVehicleId) {
      return routeOptions;
    }
    const vehicleNumericId = Number(createVehicleId);
    return routeOptions.filter(
      (route) =>
        !route.vehicleId || route.vehicleId === vehicleNumericId
    );
  }, [createVehicleId, routeOptions]);

  const handleToggleOrder = (orderCode: string) => {
    setSelectedOrderCodes((current) =>
      current.includes(orderCode)
        ? current.filter((code) => code !== orderCode)
        : [...current, orderCode]
    );
  };

  const handleCreateManifest = async () => {
    if (!createHubNumericId) {
      notification.error('Target hub is required.');
      return;
    }
    if (!createPostOfficeCode) {
      notification.error('Origin post office is required.');
      return;
    }
    if (!createVehicleId) {
      notification.error('Transport vehicle is required.');
      return;
    }
    if (selectedOrderCodes.length === 0) {
      notification.error('Select at least one order to transport.');
      return;
    }

    try {
      await createManifest({
        target_hub_id: createHubNumericId,
        origin_post_office_code: createPostOfficeCode,
        vehicle_id: Number(createVehicleId),
        ...(createRouteId ? { route_id: Number(createRouteId) } : {}),
        order_codes: selectedOrderCodes,
      }).unwrap();
      notification.success('Transport manifest created successfully.');
      setIsCreateOpen(false);
      setCreateHubId('');
      setCreatePostOfficeCode('');
      setCreateVehicleId('');
      setCreateRouteId('');
      setSelectedOrderCodes([]);
      void refetch();
    } catch (error) {
      notification.error('Failed to create transport manifest.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleConfirmOutbound = async (manifest: HandoverManifest) => {
    if (!manifest.id) {
      return;
    }
    try {
      await driverCheckinStart(manifest.id).unwrap();
      notification.success('Driver check-in at post office completed.');
      void refetch();
    } catch (error) {
      notification.error('Failed to complete departure check-in.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleConfirmInbound = async (manifest: HandoverManifest) => {
    if (!manifest.id) {
      return;
    }
    try {
      await driverCheckinEnd(manifest.id).unwrap();
      notification.success('Driver check-in at hub completed.');
      void refetch();
    } catch (error) {
      notification.error('Failed to complete arrival check-in.', {
        description: getErrorMessage(error),
      });
    }
  };

  const resolveHubLabel = (hubId?: number): string => {
    if (!hubId) {
      return '--';
    }
    const hub = hubOptions.find((item: Hub) => item.id === hubId);
    if (!hub) {
      return `#${hubId}`;
    }
    return `${hub.code} - ${hub.name}`;
  };

  if (!canAccess) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Access denied</CardTitle>
          <CardDescription>
            Only TMS hub staff or TMS admin can manage post office to hub
            transport.
          </CardDescription>
        </CardHeader>
      </Card>
    );
  }

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
        <div>
          <h1 className='text-2xl font-bold tracking-tight'>
            Post Office to Hub Transport
          </h1>
          <p className='text-muted-foreground'>
            Assign hub vehicles to move orders from post offices back to hubs.
          </p>
        </div>
        <div className='flex flex-wrap gap-2'>
          <Button variant='outline' onClick={() => void refetch()}>
            <RefreshCw className='mr-2 h-4 w-4' />
            Refresh
          </Button>
          <Button onClick={() => setIsCreateOpen(true)}>
            <Plus className='mr-2 h-4 w-4' />
            New transport run
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Truck className='h-5 w-5' />
            Transport manifests
          </CardTitle>
          <CardDescription>
            Each manifest links orders, a vehicle, and optional collection route.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-4'>
          <div className='grid gap-4 md:grid-cols-2'>
            <div className='space-y-2'>
              <Label htmlFor='filter-hub'>Target hub</Label>
              <Select
                value={filterHubId || 'ALL'}
                onValueChange={(value) => {
                  setFilterHubId(value === 'ALL' ? '' : value);
                  setPage(0);
                }}
              >
                <SelectTrigger id='filter-hub'>
                  <SelectValue placeholder='All hubs' />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='ALL'>All hubs</SelectItem>
                  {hubOptions.map((hub) => (
                    <SelectItem key={hub.id} value={String(hub.id)}>
                      {hub.code} - {hub.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className='space-y-2'>
              <Label htmlFor='filter-status'>Status</Label>
              <Select
                value={filterStatus}
                onValueChange={(value) => {
                  setFilterStatus(value as 'ALL' | HandoverManifestStatus);
                  setPage(0);
                }}
              >
                <SelectTrigger id='filter-status'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='ALL'>All statuses</SelectItem>
                  {MANIFEST_STATUS_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className='overflow-x-auto rounded-md border'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Manifest</TableHead>
                  <TableHead>Post office</TableHead>
                  <TableHead>Hub</TableHead>
                  <TableHead>Vehicle</TableHead>
                  <TableHead>Route</TableHead>
                  <TableHead>Orders</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {manifests.length === 0 ? (
                  <TableRow>
                    <TableCell
                      colSpan={8}
                      className='py-8 text-center text-muted-foreground'
                    >
                      {isFetching
                        ? 'Loading transport manifests...'
                        : 'No transport manifests found.'}
                    </TableCell>
                  </TableRow>
                ) : (
                  manifests.map((manifest) => (
                    <TableRow key={manifest.id}>
                      <TableCell className='font-medium'>
                        {manifest.manifestCode || `#${manifest.id}`}
                      </TableCell>
                      <TableCell>{manifest.originPostOfficeCode || '--'}</TableCell>
                      <TableCell>{resolveHubLabel(manifest.targetHubId)}</TableCell>
                      <TableCell>
                        {manifest.vehicleLicensePlate ||
                          (manifest.vehicleId ? `#${manifest.vehicleId}` : '--')}
                      </TableCell>
                      <TableCell>{manifest.routeCode || '--'}</TableCell>
                      <TableCell>{manifest.orders?.length ?? 0}</TableCell>
                      <TableCell>
                        <Badge variant={getStatusBadgeVariant(manifest.status)}>
                          {manifest.status || '--'}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className='flex flex-wrap gap-2'>
                          {manifest.status === 'CREATED' ? (
                            <Button
                              size='sm'
                              variant='outline'
                              disabled={isConfirmingOutbound}
                              onClick={() => void handleConfirmOutbound(manifest)}
                            >
                              Driver check-in start
                            </Button>
                          ) : null}
                          {manifest.status === 'CREATED' ||
                          manifest.status === 'OUTBOUND_CONFIRMED' ? (
                            <Button
                              size='sm'
                              disabled={isConfirmingInbound}
                              onClick={() => void handleConfirmInbound(manifest)}
                            >
                              Driver check-in end
                            </Button>
                          ) : null}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>

          <div className='flex items-center justify-between'>
            <p className='text-sm text-muted-foreground'>
              Page {(manifestsData?.currentPage ?? 0) + 1} of{' '}
              {Math.max(manifestsData?.totalPages ?? 1, 1)} ·{' '}
              {manifestsData?.totalItems ?? 0} manifest(s)
            </p>
            <div className='flex gap-2'>
              <Button
                variant='outline'
                size='sm'
                disabled={page <= 0 || isFetching}
                onClick={() => setPage((current) => Math.max(current - 1, 0))}
              >
                Previous
              </Button>
              <Button
                variant='outline'
                size='sm'
                disabled={
                  isFetching ||
                  (manifestsData?.totalPages ?? 1) <= page + 1
                }
                onClick={() => setPage((current) => current + 1)}
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
        <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-2xl'>
          <DialogHeader>
            <DialogTitle>New transport run</DialogTitle>
            <DialogDescription>
              Select hub, post office, vehicle, and orders waiting at the post
              office.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='create-hub'>Target hub *</Label>
                <Select value={createHubId} onValueChange={setCreateHubId}>
                  <SelectTrigger id='create-hub'>
                    <SelectValue placeholder='Select hub' />
                  </SelectTrigger>
                  <SelectContent>
                    {hubOptions.map((hub) => (
                      <SelectItem key={hub.id} value={String(hub.id)}>
                        {hub.code} - {hub.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='create-po'>Origin post office *</Label>
                <Select
                  value={createPostOfficeCode}
                  onValueChange={setCreatePostOfficeCode}
                  disabled={!createHubId}
                >
                  <SelectTrigger id='create-po'>
                    <SelectValue placeholder='Select post office' />
                  </SelectTrigger>
                  <SelectContent>
                    {postOfficeOptions.map((mapping) => (
                      <SelectItem
                        key={mapping.postOfficeCode}
                        value={mapping.postOfficeCode}
                      >
                        {mapping.postOfficeCode}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='create-vehicle'>Vehicle *</Label>
                <Select
                  value={createVehicleId}
                  onValueChange={setCreateVehicleId}
                  disabled={!createHubId}
                >
                  <SelectTrigger id='create-vehicle'>
                    <SelectValue placeholder='Select vehicle' />
                  </SelectTrigger>
                  <SelectContent>
                    {vehicleOptions.map((vehicle) => (
                      <SelectItem key={vehicle.id} value={String(vehicle.id)}>
                        {vehicle.licensePlate} ({vehicle.vehicleType})
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2'>
                <Label htmlFor='create-route'>Route (optional)</Label>
                <Select
                  value={createRouteId || 'NONE'}
                  onValueChange={(value) =>
                    setCreateRouteId(value === 'NONE' ? '' : value)
                  }
                  disabled={!createPostOfficeCode}
                >
                  <SelectTrigger id='create-route'>
                    <SelectValue placeholder='Optional collection route' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='NONE'>No route</SelectItem>
                    {filteredRoutes.map((route) => (
                      <SelectItem key={route.id} value={String(route.id)}>
                        {route.routeCode} - {route.routeName}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className='space-y-2'>
              <Label>Orders at post office *</Label>
              {!createPostOfficeCode ? (
                <p className='text-sm text-muted-foreground'>
                  Select a post office to load ready orders.
                </p>
              ) : isLoadingReadyOrders ? (
                <p className='text-sm text-muted-foreground'>
                  Loading orders at post office...
                </p>
              ) : readyOrders.length === 0 ? (
                <p className='text-sm text-muted-foreground'>
                  No orders with status AT_ORIGIN_POST_OFFICE at this post
                  office.
                </p>
              ) : (
                <div className='max-h-56 space-y-2 overflow-y-auto rounded-md border p-3'>
                  {readyOrders.map((order) => {
                    const orderCode = order.orderCode || '';
                    if (!orderCode) {
                      return null;
                    }
                    const checked = selectedOrderCodes.includes(orderCode);
                    return (
                      <label
                        key={order.id}
                        className='flex cursor-pointer items-center gap-2 text-sm'
                      >
                        <Input
                          type='checkbox'
                          className='h-4 w-4'
                          checked={checked}
                          onChange={() => handleToggleOrder(orderCode)}
                        />
                        <span className='font-medium'>{orderCode}</span>
                        <span className='text-muted-foreground'>
                          {order.status}
                        </span>
                      </label>
                    );
                  })}
                </div>
              )}
            </div>
          </div>

          <DialogFooter>
            <Button variant='outline' onClick={() => setIsCreateOpen(false)}>
              Cancel
            </Button>
            <Button disabled={isCreating} onClick={() => void handleCreateManifest()}>
              {isCreating ? 'Creating...' : 'Create transport run'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
