/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - First-mile pickup tracking page
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
} from '@/shared/components';
import { useNotification } from '@/shared/hooks';
import {
  useGetActiveCouriersByPostOfficeQuery,
  useGetPickupTrackingOverviewQuery,
  useGetPostOfficesQuery,
  usePickupCheckinOrderMutation,
} from '../../api';
import type {
  PickupTrackingOrder,
  PickupTrackingOverviewResponse,
  PickupTrackingTrip,
  PostOffice,
  PostOfficeStaff,
} from '../../types';
import { PickupOrdersMap } from './components/PickupOrdersMap';

type PickupPageAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_SCOPED'
  | 'COURIER_SELF'
  | 'NO_ACCESS';

const POST_OFFICE_PAGE_SIZE = 200;

const resolvePickupPageAccessScope = (
  roles: string[]
): PickupPageAccessScope => {
  if (roles.includes('TMS_ADMIN')) {
    return 'ADMIN_ALL';
  }

  if (roles.includes('TMS_POSTOFFICER_MANAGER')) {
    return 'MANAGER_SCOPED';
  }

  if (roles.includes('TMS_POSTOFFICER')) {
    return 'COURIER_SELF';
  }

  return 'NO_ACCESS';
};

const getTodayDateInputValue = (): string => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const parseOptionalPositiveInteger = (value: string): number | undefined => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return undefined;
  }

  const parsedValue = Number(trimmedValue);
  if (!Number.isInteger(parsedValue) || parsedValue <= 0) {
    return undefined;
  }

  return parsedValue;
};

const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('en-US');
};

const formatNumber = (value?: number): string => {
  if (value === undefined || value === null) {
    return '0';
  }

  return String(value);
};

const getScopeLabel = (scope: PickupPageAccessScope): string => {
  if (scope === 'ADMIN_ALL') {
    return 'TMS admin';
  }

  if (scope === 'MANAGER_SCOPED') {
    return 'Post office manager';
  }

  if (scope === 'COURIER_SELF') {
    return 'Courier';
  }

  return 'No access';
};

const getScopeDescription = (scope: PickupPageAccessScope): string => {
  if (scope === 'ADMIN_ALL') {
    return 'Track pickup operations across post offices and couriers.';
  }

  if (scope === 'MANAGER_SCOPED') {
    return 'Track pickup operations for couriers in your managed post offices.';
  }

  if (scope === 'COURIER_SELF') {
    return 'Track and check in your assigned pickup orders.';
  }

  return 'Your account does not have permission to access pickup tracking.';
};

const canCheckinOrder = (
  order: PickupTrackingOrder,
  isCourier: boolean
): boolean => {
  if (!isCourier || order.checkedIn) {
    return false;
  }

  return (
    order.orderStatus === 'ASSIGNED_TO_PICKUP' ||
    order.orderStatus === 'PICKING_UP'
  );
};

const resolveOrders = (
  overview?: PickupTrackingOverviewResponse
): PickupTrackingOrder[] => {
  return overview?.orders ?? [];
};

const resolveTrips = (
  overview?: PickupTrackingOverviewResponse
): PickupTrackingTrip[] => {
  return overview?.trips ?? [];
};

export const PickupPage: React.FC = () => {
  const notification = useNotification();
  const profile = useAppSelector((state) => state.account.user.profile);
  const roles = profile?.roles ?? [];

  const accessScope = React.useMemo(
    () => resolvePickupPageAccessScope(roles),
    [roles]
  );

  const canAccess = accessScope !== 'NO_ACCESS';
  const isCourierScope = accessScope === 'COURIER_SELF';

  const [tripDate, setTripDate] = React.useState(getTodayDateInputValue);
  const [selectedPostOfficeId, setSelectedPostOfficeId] = React.useState('');
  const [selectedCourierStaffId, setSelectedCourierStaffId] =
    React.useState('');
  const [selectedOrderId, setSelectedOrderId] = React.useState<
    number | undefined
  >(undefined);

  const [isCheckinDialogOpen, setIsCheckinDialogOpen] = React.useState(false);
  const [checkinOrder, setCheckinOrder] =
    React.useState<PickupTrackingOrder | null>(null);
  const [checkinPhoto, setCheckinPhoto] = React.useState<File | null>(null);
  const [checkinLatitude, setCheckinLatitude] = React.useState('');
  const [checkinLongitude, setCheckinLongitude] = React.useState('');
  const [isResolvingLocation, setIsResolvingLocation] = React.useState(false);

  const shouldLoadPostOffices = canAccess && !isCourierScope;

  const { data: postOfficesData, isLoading: isLoadingPostOffices } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
      },
      {
        skip: !shouldLoadPostOffices,
      }
    );

  const postOfficeOptions = React.useMemo<PostOffice[]>(
    () => postOfficesData?.items ?? [],
    [postOfficesData]
  );

  React.useEffect(() => {
    if (isCourierScope || !postOfficeOptions.length) {
      return;
    }

    const hasSelectedPostOffice = postOfficeOptions.some(
      (postOffice) => String(postOffice.id) === selectedPostOfficeId
    );

    if (!hasSelectedPostOffice) {
      setSelectedPostOfficeId(String(postOfficeOptions[0].id));
    }
  }, [isCourierScope, postOfficeOptions, selectedPostOfficeId]);

  const selectedPostOfficeNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(selectedPostOfficeId),
    [selectedPostOfficeId]
  );

  const shouldLoadCouriers =
    canAccess && !isCourierScope && Boolean(selectedPostOfficeNumericId);

  const {
    data: couriersData,
    isLoading: isLoadingCouriers,
    error: couriersError,
  } = useGetActiveCouriersByPostOfficeQuery(selectedPostOfficeNumericId ?? 0, {
    skip: !shouldLoadCouriers,
  });

  React.useEffect(() => {
    if (!couriersError) {
      return;
    }

    notification.error('Failed to load couriers for selected post office.', {
      description: getErrorMessage(couriersError),
    });
  }, [couriersError, notification]);

  const courierOptions = React.useMemo(() => {
    return (couriersData ?? [])
      .filter(
        (
          courier: PostOfficeStaff
        ): courier is PostOfficeStaff & { id: number } =>
          Number.isInteger(courier.id) && courier.id > 0
      )
      .map((courier) => ({
        id: courier.id,
        code: courier.code?.trim() || `COURIER-${courier.id}`,
        fullName: courier.fullName?.trim() || 'Unknown courier',
      }));
  }, [couriersData]);

  React.useEffect(() => {
    setSelectedCourierStaffId('');
  }, [selectedPostOfficeId]);

  const selectedCourierNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(selectedCourierStaffId),
    [selectedCourierStaffId]
  );

  const shouldLoadOverview =
    canAccess && (isCourierScope || Boolean(selectedPostOfficeNumericId));

  const overviewQueryArgs = React.useMemo(
    () => ({
      tripDate,
      ...(isCourierScope
        ? {}
        : {
            ...(selectedPostOfficeNumericId
              ? { postOfficeId: selectedPostOfficeNumericId }
              : {}),
            ...(selectedCourierNumericId
              ? { courierStaffId: selectedCourierNumericId }
              : {}),
          }),
    }),
    [
      isCourierScope,
      selectedCourierNumericId,
      selectedPostOfficeNumericId,
      tripDate,
    ]
  );

  const {
    data: pickupOverview,
    isLoading: isLoadingOverview,
    isFetching: isFetchingOverview,
    refetch: refetchOverview,
    error: pickupOverviewError,
  } = useGetPickupTrackingOverviewQuery(overviewQueryArgs, {
    skip: !shouldLoadOverview,
  });

  React.useEffect(() => {
    if (!pickupOverviewError) {
      return;
    }

    notification.error('Failed to load pickup tracking data.', {
      description: getErrorMessage(pickupOverviewError),
    });
  }, [notification, pickupOverviewError]);

  const orders = React.useMemo(
    () => resolveOrders(pickupOverview),
    [pickupOverview]
  );
  const trips = React.useMemo(
    () => resolveTrips(pickupOverview),
    [pickupOverview]
  );

  const ordersWithCoordinates = React.useMemo(
    () =>
      orders.filter(
        (order) =>
          order.senderLatitude !== undefined &&
          order.senderLongitude !== undefined
      ),
    [orders]
  );

  React.useEffect(() => {
    if (!orders.length) {
      setSelectedOrderId(undefined);
      return;
    }

    const hasSelectedOrder =
      selectedOrderId !== undefined &&
      orders.some((order) => order.orderId === selectedOrderId);

    if (!hasSelectedOrder) {
      setSelectedOrderId(orders[0].orderId);
    }
  }, [orders, selectedOrderId]);

  const selectedOrder = React.useMemo(
    () => orders.find((order) => order.orderId === selectedOrderId) || null,
    [orders, selectedOrderId]
  );

  const [pickupCheckinOrder, { isLoading: isSubmittingCheckin }] =
    usePickupCheckinOrderMutation();

  const handleOpenCheckinDialog = (order: PickupTrackingOrder) => {
    setCheckinOrder(order);
    setCheckinPhoto(null);
    setCheckinLatitude('');
    setCheckinLongitude('');
    setIsCheckinDialogOpen(true);
  };

  const handleResolveCurrentLocation = async () => {
    if (typeof window === 'undefined' || !('geolocation' in navigator)) {
      notification.error('Geolocation is not supported in your browser.');
      return;
    }

    setIsResolvingLocation(true);

    try {
      const position = await new Promise<GeolocationPosition>(
        (resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, {
            enableHighAccuracy: true,
            timeout: 15000,
          });
        }
      );

      setCheckinLatitude(position.coords.latitude.toFixed(6));
      setCheckinLongitude(position.coords.longitude.toFixed(6));
    } catch (error) {
      notification.error('Failed to resolve current location.', {
        description: getErrorMessage(error),
      });
    } finally {
      setIsResolvingLocation(false);
    }
  };

  const handleSubmitCheckin = async () => {
    if (!checkinOrder) {
      notification.error('Please select an order to check in.');
      return;
    }

    if (!checkinPhoto) {
      notification.error('Please select a photo before check in.');
      return;
    }

    const latitude = Number(checkinLatitude);
    const longitude = Number(checkinLongitude);

    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      notification.error('Please provide a valid check-in location.');
      return;
    }

    const formData = new FormData();
    formData.append('latitude', String(latitude));
    formData.append('longitude', String(longitude));
    formData.append('photo', checkinPhoto);

    try {
      await pickupCheckinOrder({
        orderId: checkinOrder.orderId,
        formData,
      }).unwrap();

      notification.success('Pickup check-in completed successfully.');
      setIsCheckinDialogOpen(false);
      setCheckinOrder(null);
      setCheckinPhoto(null);
      setCheckinLatitude('');
      setCheckinLongitude('');
      void refetchOverview();
    } catch (error) {
      notification.error('Pickup check-in failed.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Pickup Tracking</h1>
        <p className='text-muted-foreground'>
          Monitor assigned pickup orders on map, check in orders, and review
          pickup statistics.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-3'>
            <span>Access Scope</span>
            <Badge variant={canAccess ? 'default' : 'secondary'}>
              {getScopeLabel(accessScope)}
            </Badge>
          </CardTitle>
          <CardDescription>{getScopeDescription(accessScope)}</CardDescription>
        </CardHeader>
      </Card>

      {!canAccess ? (
        <Card>
          <CardHeader>
            <CardTitle>Access denied</CardTitle>
            <CardDescription>
              Your account does not have permission to access pickup tracking.
            </CardDescription>
          </CardHeader>
        </Card>
      ) : (
        <>
          <Card>
            <CardHeader>
              <CardTitle>Tracking Filters</CardTitle>
              <CardDescription>
                Select trip date and tracking scope.
              </CardDescription>
            </CardHeader>
            <CardContent className='grid gap-4 md:grid-cols-3'>
              <div className='space-y-2'>
                <Label htmlFor='trip-date'>Trip date</Label>
                <Input
                  id='trip-date'
                  type='date'
                  value={tripDate}
                  onChange={(event) => setTripDate(event.target.value)}
                />
              </div>

              {!isCourierScope ? (
                <>
                  <div className='space-y-2'>
                    <Label>Post office</Label>
                    <Select
                      value={selectedPostOfficeId}
                      onValueChange={setSelectedPostOfficeId}
                      disabled={
                        isLoadingPostOffices || !postOfficeOptions.length
                      }
                    >
                      <SelectTrigger>
                        <SelectValue placeholder='Select post office' />
                      </SelectTrigger>
                      <SelectContent>
                        {postOfficeOptions.map((postOffice) => (
                          <SelectItem
                            key={postOffice.id}
                            value={String(postOffice.id)}
                          >
                            {postOffice.code} - {postOffice.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>

                  <div className='space-y-2'>
                    <Label>Courier</Label>
                    <Select
                      value={selectedCourierStaffId}
                      onValueChange={setSelectedCourierStaffId}
                      disabled={!selectedPostOfficeId || isLoadingCouriers}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder='All couriers in post office' />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value=''>All couriers</SelectItem>
                        {courierOptions.map((courier) => (
                          <SelectItem
                            key={courier.id}
                            value={String(courier.id)}
                          >
                            {courier.code} - {courier.fullName}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </>
              ) : (
                <div className='md:col-span-2 rounded-md border bg-muted/30 p-4 text-sm text-muted-foreground'>
                  You are viewing your own assigned pickup orders.
                </div>
              )}
            </CardContent>
          </Card>

          <div className='grid gap-4 sm:grid-cols-2 lg:grid-cols-3'>
            <Card>
              <CardHeader className='pb-2'>
                <CardDescription>Total trips</CardDescription>
                <CardTitle className='text-2xl'>
                  {formatNumber(pickupOverview?.totalTrips)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card>
              <CardHeader className='pb-2'>
                <CardDescription>Total orders</CardDescription>
                <CardTitle className='text-2xl'>
                  {formatNumber(pickupOverview?.totalOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card>
              <CardHeader className='pb-2'>
                <CardDescription>Checked-in orders</CardDescription>
                <CardTitle className='text-2xl'>
                  {formatNumber(pickupOverview?.checkedInOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card>
              <CardHeader className='pb-2'>
                <CardDescription>Pending check-in</CardDescription>
                <CardTitle className='text-2xl'>
                  {formatNumber(pickupOverview?.pendingCheckinOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card>
              <CardHeader className='pb-2'>
                <CardDescription>PICKING_UP orders</CardDescription>
                <CardTitle className='text-2xl'>
                  {formatNumber(pickupOverview?.pickingUpOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card>
              <CardHeader className='pb-2'>
                <CardDescription>PICKED_UP orders</CardDescription>
                <CardTitle className='text-2xl'>
                  {formatNumber(pickupOverview?.pickedUpOrders)}
                </CardTitle>
              </CardHeader>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Pickup map</CardTitle>
              <CardDescription>
                {ordersWithCoordinates.length} assigned order(s) with location.
              </CardDescription>
            </CardHeader>
            <CardContent>
              {ordersWithCoordinates.length === 0 ? (
                <div className='rounded-md border border-dashed p-8 text-center text-sm text-muted-foreground'>
                  No assigned pickup orders with coordinates for selected
                  filters.
                </div>
              ) : (
                <PickupOrdersMap
                  orders={ordersWithCoordinates}
                  selectedOrderId={selectedOrderId}
                  onSelectOrder={setSelectedOrderId}
                />
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Assigned orders</CardTitle>
              <CardDescription>
                {isLoadingOverview
                  ? 'Loading assigned pickup orders...'
                  : `${orders.length} order(s) found in ${trips.length} trip(s).`}
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='flex justify-end'>
                <Button
                  variant='outline'
                  onClick={() => void refetchOverview()}
                  disabled={isFetchingOverview}
                >
                  {isFetchingOverview ? 'Refreshing...' : 'Refresh'}
                </Button>
              </div>

              {orders.length === 0 ? (
                <div className='rounded-md border border-dashed p-8 text-center text-sm text-muted-foreground'>
                  No assigned pickup orders for selected filters.
                </div>
              ) : (
                <div className='overflow-x-auto rounded-md border'>
                  <table className='w-full min-w-[900px] text-sm'>
                    <thead className='bg-muted/40 text-left'>
                      <tr>
                        <th className='px-3 py-2 font-medium'>Order</th>
                        <th className='px-3 py-2 font-medium'>Courier</th>
                        <th className='px-3 py-2 font-medium'>Trip</th>
                        <th className='px-3 py-2 font-medium'>Pickup window</th>
                        <th className='px-3 py-2 font-medium'>Check-in</th>
                        <th className='px-3 py-2 font-medium'>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {orders.map((order) => {
                        const isSelected = selectedOrderId === order.orderId;

                        return (
                          <tr
                            key={`${order.tripOrderId}-${order.orderId}`}
                            className={`border-t ${isSelected ? 'bg-accent/30' : ''}`}
                            onClick={() => setSelectedOrderId(order.orderId)}
                          >
                            <td className='px-3 py-2'>
                              <div className='font-medium'>
                                {order.orderCode || `#${order.orderId}`}
                              </div>
                              <div className='text-xs text-muted-foreground'>
                                {order.orderStatus || '--'}
                              </div>
                            </td>
                            <td className='px-3 py-2'>
                              <div>{order.courierName || '--'}</div>
                              <div className='text-xs text-muted-foreground'>
                                {order.courierCode || '--'}
                              </div>
                            </td>
                            <td className='px-3 py-2'>
                              <div>{order.tripCode || '--'}</div>
                              <div className='text-xs text-muted-foreground'>
                                {order.postOfficeCode || '--'}
                              </div>
                            </td>
                            <td className='px-3 py-2'>
                              <div>{formatDateTime(order.pickupTimeStart)}</div>
                              <div className='text-xs text-muted-foreground'>
                                {formatDateTime(order.pickupTimeEnd)}
                              </div>
                            </td>
                            <td className='px-3 py-2'>
                              <Badge
                                variant={
                                  order.checkedIn ? 'default' : 'secondary'
                                }
                              >
                                {order.checkedIn
                                  ? `Checked in ${formatDateTime(order.checkinTime)}`
                                  : 'Pending'}
                              </Badge>
                            </td>
                            <td className='px-3 py-2'>
                              {canCheckinOrder(order, isCourierScope) ? (
                                <Button
                                  size='sm'
                                  onClick={(event) => {
                                    event.stopPropagation();
                                    handleOpenCheckinDialog(order);
                                  }}
                                >
                                  Check in
                                </Button>
                              ) : (
                                <Button
                                  variant='outline'
                                  size='sm'
                                  onClick={(event) => {
                                    event.stopPropagation();
                                    setSelectedOrderId(order.orderId);
                                  }}
                                >
                                  View
                                </Button>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>

          {selectedOrder ? (
            <Card>
              <CardHeader>
                <CardTitle>
                  Order detail:{' '}
                  {selectedOrder.orderCode || `#${selectedOrder.orderId}`}
                </CardTitle>
                <CardDescription>
                  Trip {selectedOrder.tripCode || '--'} · Courier{' '}
                  {selectedOrder.courierName || '--'}
                </CardDescription>
              </CardHeader>
              <CardContent className='grid gap-3 sm:grid-cols-2'>
                <div>
                  <div className='text-xs text-muted-foreground'>Sender</div>
                  <div className='font-medium'>
                    {selectedOrder.senderName || '--'}
                  </div>
                  <div>{selectedOrder.senderPhone || '--'}</div>
                  <div className='text-sm text-muted-foreground'>
                    {selectedOrder.senderAddressDetail || '--'}
                  </div>
                </div>

                <div>
                  <div className='text-xs text-muted-foreground'>
                    Planned service
                  </div>
                  <div>
                    Arrival: {formatDateTime(selectedOrder.plannedArrivalTime)}
                  </div>
                  <div>
                    Start:{' '}
                    {formatDateTime(selectedOrder.plannedStartServiceTime)}
                  </div>
                  <div>
                    Departure:{' '}
                    {formatDateTime(selectedOrder.plannedDepartureTime)}
                  </div>
                </div>
              </CardContent>
            </Card>
          ) : null}
        </>
      )}

      <Dialog open={isCheckinDialogOpen} onOpenChange={setIsCheckinDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              Pickup check-in {checkinOrder?.orderCode || ''}
            </DialogTitle>
            <DialogDescription>
              Upload pickup photo and share your current location.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            <div className='space-y-2'>
              <Label htmlFor='checkin-photo'>Photo</Label>
              <Input
                id='checkin-photo'
                type='file'
                accept='image/*'
                onChange={(event) => {
                  const file = event.target.files?.[0] || null;
                  setCheckinPhoto(file);
                }}
              />
            </div>

            <div className='grid gap-3 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='checkin-latitude'>Latitude</Label>
                <Input
                  id='checkin-latitude'
                  value={checkinLatitude}
                  readOnly
                  placeholder='Resolve current location'
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='checkin-longitude'>Longitude</Label>
                <Input
                  id='checkin-longitude'
                  value={checkinLongitude}
                  readOnly
                  placeholder='Resolve current location'
                />
              </div>
            </div>

            <Button
              type='button'
              variant='outline'
              onClick={() => void handleResolveCurrentLocation()}
              disabled={isResolvingLocation}
            >
              {isResolvingLocation
                ? 'Resolving location...'
                : 'Use current location'}
            </Button>
          </div>

          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsCheckinDialogOpen(false)}
              disabled={isSubmittingCheckin}
            >
              Cancel
            </Button>
            <Button
              onClick={() => void handleSubmitCheckin()}
              disabled={isSubmittingCheckin}
            >
              {isSubmittingCheckin ? 'Submitting...' : 'Confirm check-in'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};
