/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - First-mile dispatchers page
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
  Checkbox,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import {
  CheckCircle2,
  Loader2,
  RefreshCw,
  Route,
  Search,
  ShieldAlert,
} from 'lucide-react';
import {
  useAutoAssignPickupPlanMutation,
  useGetOrdersQuery,
  useGetPostOfficesQuery,
  useManualAssignPickupOrdersMutation,
  useOptimizePickupPlanMutation,
} from '../../api';
import type {
  AutoAssignPickupPlanRequest,
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  ManualAssignPickupOrdersRequest,
  OptimizePickupPlanRequest,
  PickupAssignmentResponse,
  PickupOptimizationResponse,
  PickupShift,
  PostOffice,
} from '../../types';

const POST_OFFICE_PAGE_SIZE = 200;
const ORDER_PAGE_SIZE = 50;

type DispatcherAccessScope = 'ADMIN_ALL' | 'MANAGER_SCOPED' | 'NO_ACCESS';

const SHIFT_OPTIONS: Array<{ value: PickupShift; label: string }> = [
  { value: 'MORNING', label: 'Morning (07:30 - 12:00)' },
  { value: 'AFTERNOON', label: 'Afternoon (13:30 - 18:00)' },
  { value: 'EVENING', label: 'Evening (18:30 - 22:00)' },
];

const CANDIDATE_ORDER_STATUSES: FirstMileOrderStatus[] = [
  'CREATED',
  'PICKUP_FAILED',
];

const resolveDispatcherAccessScope = (
  roles: string[]
): DispatcherAccessScope => {
  if (roles.includes('TMS_ADMIN')) {
    return 'ADMIN_ALL';
  }

  if (roles.includes('TMS_POSTOFFICER_MANAGER')) {
    return 'MANAGER_SCOPED';
  }

  return 'NO_ACCESS';
};

const getScopeBadgeLabel = (scope: DispatcherAccessScope): string => {
  if (scope === 'ADMIN_ALL') {
    return 'TMS admin';
  }

  if (scope === 'MANAGER_SCOPED') {
    return 'Post office manager';
  }

  return 'No access';
};

const getScopeDescription = (scope: DispatcherAccessScope): string => {
  if (scope === 'ADMIN_ALL') {
    return 'You can dispatch first-mile orders for every post office.';
  }

  if (scope === 'MANAGER_SCOPED') {
    return 'You can dispatch first-mile orders for post offices you manage.';
  }

  return 'Your account does not have permission to dispatch first-mile orders.';
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
    return '--';
  }

  return String(value);
};

const getTodayDateInputValue = (): string => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const buildShiftWindow = (
  tripDate: string,
  shift: PickupShift
): { planningStartTime: string; planningEndTime: string } => {
  if (shift === 'MORNING') {
    return {
      planningStartTime: `${tripDate}T07:30:00`,
      planningEndTime: `${tripDate}T12:00:00`,
    };
  }

  if (shift === 'AFTERNOON') {
    return {
      planningStartTime: `${tripDate}T13:30:00`,
      planningEndTime: `${tripDate}T18:00:00`,
    };
  }

  return {
    planningStartTime: `${tripDate}T18:30:00`,
    planningEndTime: `${tripDate}T22:00:00`,
  };
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

const parsePositiveIntegerList = (value: string): number[] | null => {
  const trimmedValue = value.trim();
  if (!trimmedValue) {
    return [];
  }

  const parsedValues = trimmedValue
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => Number(item));

  if (
    parsedValues.some(
      (parsedValue) => !Number.isInteger(parsedValue) || parsedValue <= 0
    )
  ) {
    return null;
  }

  return Array.from(new Set(parsedValues));
};

const isCandidateOrderStatus = (status: FirstMileOrderStatus): boolean => {
  return CANDIDATE_ORDER_STATUSES.includes(status);
};

interface SuggestedCourier {
  id: number;
  label: string;
}

const extractSuggestedCouriers = (
  optimizationResult: PickupOptimizationResponse | null,
  assignmentResult: PickupAssignmentResponse | null
): SuggestedCourier[] => {
  const suggestions = new Map<number, SuggestedCourier>();

  for (const route of optimizationResult?.routes ?? []) {
    if (!route.courierStaffId) {
      continue;
    }

    const label =
      route.courierCode || route.courierName
        ? `${route.courierCode || route.courierStaffId}${route.courierName ? ` - ${route.courierName}` : ''}`
        : String(route.courierStaffId);

    suggestions.set(route.courierStaffId, {
      id: route.courierStaffId,
      label,
    });
  }

  for (const trip of assignmentResult?.trips ?? []) {
    if (!trip.courierStaffId) {
      continue;
    }

    const label =
      trip.courierCode || trip.courierName
        ? `${trip.courierCode || trip.courierStaffId}${trip.courierName ? ` - ${trip.courierName}` : ''}`
        : String(trip.courierStaffId);

    suggestions.set(trip.courierStaffId, {
      id: trip.courierStaffId,
      label,
    });
  }

  return Array.from(suggestions.values()).sort((a, b) => a.id - b.id);
};

export const DispatchersPage: React.FC = () => {
  const notification = useNotification();
  const profile = useAppSelector((state) => state.account.user.profile);
  const roles = profile?.roles ?? [];

  const accessScope = React.useMemo(
    () => resolveDispatcherAccessScope(roles),
    [roles]
  );
  const canDispatch = accessScope !== 'NO_ACCESS';
  const isTmsAdmin = accessScope === 'ADMIN_ALL';

  const [selectedPostOfficeId, setSelectedPostOfficeId] = React.useState('');
  const [shift, setShift] = React.useState<PickupShift>('MORNING');
  const [tripDate, setTripDate] = React.useState(getTodayDateInputValue);

  const [orderPage, setOrderPage] = React.useState(0);
  const [orderKeywordInput, setOrderKeywordInput] = React.useState('');
  const [orderKeyword, setOrderKeyword] = React.useState<string | undefined>(
    undefined
  );

  const [autoCourierIdsInput, setAutoCourierIdsInput] = React.useState('');
  const [manualCourierStaffIdInput, setManualCourierStaffIdInput] =
    React.useState('');
  const [orderLimitInput, setOrderLimitInput] = React.useState('');

  const [selectedOrderIds, setSelectedOrderIds] = React.useState<number[]>([]);
  const [optimizationResult, setOptimizationResult] =
    React.useState<PickupOptimizationResponse | null>(null);
  const [assignmentResult, setAssignmentResult] =
    React.useState<PickupAssignmentResponse | null>(null);

  const [activeAction, setActiveAction] = React.useState<
    'preview' | 'auto' | 'manual' | null
  >(null);

  const { data: postOfficesData, isLoading: isLoadingPostOffices } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
      },
      {
        skip: !canDispatch,
      }
    );

  const postOfficeOptions = React.useMemo<PostOffice[]>(
    () => postOfficesData?.items ?? [],
    [postOfficesData]
  );

  React.useEffect(() => {
    if (!postOfficeOptions.length) {
      return;
    }

    const hasSelectedPostOffice = postOfficeOptions.some(
      (postOffice) => String(postOffice.id) === selectedPostOfficeId
    );

    if (!hasSelectedPostOffice) {
      setSelectedPostOfficeId(String(postOfficeOptions[0].id));
    }
  }, [postOfficeOptions, selectedPostOfficeId]);

  const selectedPostOffice = React.useMemo(() => {
    return postOfficeOptions.find(
      (postOffice) => String(postOffice.id) === selectedPostOfficeId
    );
  }, [postOfficeOptions, selectedPostOfficeId]);

  const selectedPostOfficeCode = selectedPostOffice?.code;

  const {
    data: ordersData,
    isLoading: isLoadingOrders,
    isFetching: isFetchingOrders,
    refetch: refetchOrders,
  } = useGetOrdersQuery(
    {
      page: orderPage,
      size: ORDER_PAGE_SIZE,
      keyword: orderKeyword,
      ...(selectedPostOfficeCode
        ? { originPostOfficeCode: selectedPostOfficeCode }
        : {}),
    },
    {
      skip: !canDispatch || !selectedPostOfficeId,
    }
  );

  const candidateOrders = React.useMemo(() => {
    return (ordersData?.items ?? []).filter((order) =>
      isCandidateOrderStatus(order.status)
    );
  }, [ordersData]);

  const selectedOrderIdSet = React.useMemo(
    () => new Set(selectedOrderIds),
    [selectedOrderIds]
  );

  const suggestedCouriers = React.useMemo(
    () => extractSuggestedCouriers(optimizationResult, assignmentResult),
    [optimizationResult, assignmentResult]
  );

  React.useEffect(() => {
    setSelectedOrderIds([]);
    setOptimizationResult(null);
    setAssignmentResult(null);
  }, [selectedPostOfficeId]);

  const [optimizePickupPlan, { isLoading: isOptimizing }] =
    useOptimizePickupPlanMutation();
  const [autoAssignPickupPlan, { isLoading: isAutoAssigning }] =
    useAutoAssignPickupPlanMutation();
  const [manualAssignPickupOrders, { isLoading: isManualAssigning }] =
    useManualAssignPickupOrdersMutation();

  const buildDispatchContext = React.useCallback(() => {
    if (!canDispatch) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can dispatch first-mile orders.'
      );
      return null;
    }

    const postOfficeId = parseOptionalPositiveInteger(selectedPostOfficeId);
    if (!postOfficeId) {
      notification.error('Please select a post office.');
      return null;
    }

    if (!tripDate) {
      notification.error('Please select trip date.');
      return null;
    }

    const shiftWindow = buildShiftWindow(tripDate, shift);

    return {
      postOfficeId,
      shiftWindow,
    };
  }, [canDispatch, notification, selectedPostOfficeId, shift, tripDate]);

  const parseOptionalCourierIds = React.useCallback(() => {
    const parsedCourierIds = parsePositiveIntegerList(autoCourierIdsInput);

    if (parsedCourierIds === null) {
      notification.error(
        'Courier IDs must be comma-separated positive integers.'
      );
      return null;
    }

    return parsedCourierIds;
  }, [autoCourierIdsInput, notification]);

  const parseOptionalOrderLimit = React.useCallback(() => {
    const parsedOrderLimit = parseOptionalPositiveInteger(orderLimitInput);

    if (orderLimitInput.trim() && parsedOrderLimit === undefined) {
      notification.error('Order limit must be a positive integer.');
      return null;
    }

    return parsedOrderLimit;
  }, [notification, orderLimitInput]);

  const handleApplyOrderFilters = (event: React.FormEvent) => {
    event.preventDefault();
    setOrderPage(0);
    setOrderKeyword(orderKeywordInput.trim() || undefined);
  };

  const handleToggleOrder = (orderId: number, checked: boolean) => {
    setSelectedOrderIds((prev) => {
      if (checked) {
        if (prev.includes(orderId)) {
          return prev;
        }

        return [...prev, orderId];
      }

      return prev.filter((id) => id !== orderId);
    });
  };

  const handleSelectAllCurrentOrders = () => {
    setSelectedOrderIds(candidateOrders.map((order) => order.id));
  };

  const handleClearSelectedOrders = () => {
    setSelectedOrderIds([]);
  };

  const handlePreviewPlan = async () => {
    const context = buildDispatchContext();
    if (!context) {
      return;
    }

    const parsedCourierIds = parseOptionalCourierIds();
    if (parsedCourierIds === null) {
      return;
    }

    const parsedOrderLimit = parseOptionalOrderLimit();
    if (parsedOrderLimit === null) {
      return;
    }

    const payload: OptimizePickupPlanRequest = {
      post_office_id: context.postOfficeId,
      planning_start_time: context.shiftWindow.planningStartTime,
      planning_end_time: context.shiftWindow.planningEndTime,
      candidate_statuses: CANDIDATE_ORDER_STATUSES,
      ...(parsedCourierIds.length ? { courier_ids: parsedCourierIds } : {}),
      ...(parsedOrderLimit ? { order_limit: parsedOrderLimit } : {}),
    };

    setActiveAction('preview');
    try {
      const result = await optimizePickupPlan(payload).unwrap();
      setOptimizationResult(result);

      notification.success('Pickup plan optimized successfully.', {
        description: `Assigned ${result.assignedOrders ?? 0}/${result.totalOrders ?? 0} order(s).`,
      });
    } catch (error) {
      notification.error('Failed to optimize pickup plan.', {
        description: getErrorMessage(error),
      });
    } finally {
      setActiveAction(null);
    }
  };

  const handleAutoAssign = async () => {
    const context = buildDispatchContext();
    if (!context) {
      return;
    }

    const parsedCourierIds = parseOptionalCourierIds();
    if (parsedCourierIds === null) {
      return;
    }

    const parsedOrderLimit = parseOptionalOrderLimit();
    if (parsedOrderLimit === null) {
      return;
    }

    const payload: AutoAssignPickupPlanRequest = {
      post_office_id: context.postOfficeId,
      shift,
      trip_date: tripDate,
      planning_start_time: context.shiftWindow.planningStartTime,
      planning_end_time: context.shiftWindow.planningEndTime,
      candidate_statuses: CANDIDATE_ORDER_STATUSES,
      ...(parsedCourierIds.length ? { courier_ids: parsedCourierIds } : {}),
      ...(parsedOrderLimit ? { order_limit: parsedOrderLimit } : {}),
    };

    setActiveAction('auto');
    try {
      const result = await autoAssignPickupPlan(payload).unwrap();
      setAssignmentResult(result);
      setSelectedOrderIds([]);

      notification.success('Auto dispatch completed.', {
        description: `Assigned ${result.assignedOrders ?? 0}/${result.totalRequestedOrders ?? 0} order(s).`,
      });

      void refetchOrders();
    } catch (error) {
      notification.error('Failed to auto assign pickup orders.', {
        description: getErrorMessage(error),
      });
    } finally {
      setActiveAction(null);
    }
  };

  const handleManualAssign = async () => {
    const context = buildDispatchContext();
    if (!context) {
      return;
    }

    if (selectedOrderIds.length === 0) {
      notification.error(
        'Please select at least one order for manual assignment.'
      );
      return;
    }

    const courierStaffId = parseOptionalPositiveInteger(
      manualCourierStaffIdInput
    );
    if (!courierStaffId) {
      notification.error('Courier staff ID must be a positive integer.');
      return;
    }

    const payload: ManualAssignPickupOrdersRequest = {
      post_office_id: context.postOfficeId,
      courier_staff_id: courierStaffId,
      order_ids: selectedOrderIds,
      shift,
      trip_date: tripDate,
      planning_start_time: context.shiftWindow.planningStartTime,
      planning_end_time: context.shiftWindow.planningEndTime,
    };

    setActiveAction('manual');
    try {
      const result = await manualAssignPickupOrders(payload).unwrap();
      setAssignmentResult(result);
      setSelectedOrderIds([]);

      notification.success('Manual dispatch completed.', {
        description: `Assigned ${result.assignedOrders ?? 0}/${result.totalRequestedOrders ?? 0} order(s).`,
      });

      void refetchOrders();
    } catch (error) {
      notification.error('Failed to manually assign pickup orders.', {
        description: getErrorMessage(error),
      });
    } finally {
      setActiveAction(null);
    }
  };

  const renderOrderItem = (order: FirstMileOrderDetail) => {
    const checked = selectedOrderIdSet.has(order.id);

    return (
      <div key={order.id} className='rounded-md border p-3'>
        <div className='flex items-start gap-3'>
          <Checkbox
            checked={checked}
            onCheckedChange={(value) =>
              handleToggleOrder(order.id, Boolean(value))
            }
            aria-label={`Select order ${order.orderCode}`}
          />
          <div className='flex-1 min-w-0 space-y-1'>
            <div className='flex flex-wrap items-center gap-2'>
              <p className='font-medium'>{order.orderCode}</p>
              <Badge variant='outline'>{order.status}</Badge>
              <Badge variant='secondary'>
                {order.isConfirm ? 'Confirmed' : 'Pending confirm'}
              </Badge>
            </div>
            <p className='text-xs text-muted-foreground'>
              Customer order: {order.customerOrderCode || '--'}
            </p>
            <p className='text-xs text-muted-foreground'>
              Sender: {order.senderName || '--'} ({order.senderPhone || '--'})
              {' | '}Receiver: {order.receiverName || '--'} (
              {order.receiverPhone || '--'})
            </p>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Dispatchers</h1>
        <p className='text-muted-foreground'>
          Dispatch first-mile pickup orders by post office and courier scope.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className='flex items-center gap-2'>
            <Route className='h-5 w-5' />
            Access Scope
          </CardTitle>
          <CardDescription>
            Dispatch permissions are validated by your role and backend scope.
          </CardDescription>
        </CardHeader>
        <CardContent className='space-y-2'>
          <Badge
            variant={canDispatch ? 'secondary' : 'destructive'}
            className='w-fit'
          >
            {getScopeBadgeLabel(accessScope)}
          </Badge>
          <p className='text-sm text-muted-foreground'>
            {getScopeDescription(accessScope)}
          </p>
        </CardContent>
      </Card>

      {!canDispatch ? (
        <Card>
          <CardContent className='pt-6'>
            <div className='flex items-center gap-2 text-muted-foreground'>
              <ShieldAlert className='h-4 w-4' />
              You do not have permission to access dispatch operations.
            </div>
          </CardContent>
        </Card>
      ) : (
        <>
          <Card>
            <CardHeader>
              <CardTitle>Dispatch Setup</CardTitle>
              <CardDescription>
                Configure post office, shift, and optimizer options before
                dispatching.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='grid gap-3 md:grid-cols-2 xl:grid-cols-4'>
                <div className='space-y-2'>
                  <Label htmlFor='dispatch-post-office'>Post office</Label>
                  <Select
                    value={selectedPostOfficeId || undefined}
                    onValueChange={(value) => {
                      setOrderPage(0);
                      setSelectedPostOfficeId(value);
                    }}
                    disabled={isLoadingPostOffices}
                  >
                    <SelectTrigger id='dispatch-post-office'>
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
                  <Label htmlFor='dispatch-shift'>Shift</Label>
                  <Select
                    value={shift}
                    onValueChange={(value) => setShift(value as PickupShift)}
                  >
                    <SelectTrigger id='dispatch-shift'>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {SHIFT_OPTIONS.map((option) => (
                        <SelectItem key={option.value} value={option.value}>
                          {option.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='dispatch-trip-date'>Trip date</Label>
                  <Input
                    id='dispatch-trip-date'
                    type='date'
                    value={tripDate}
                    onChange={(event) => setTripDate(event.target.value)}
                  />
                </div>

                <div className='space-y-2'>
                  <Label htmlFor='dispatch-order-limit'>Order limit</Label>
                  <Input
                    id='dispatch-order-limit'
                    value={orderLimitInput}
                    onChange={(event) => setOrderLimitInput(event.target.value)}
                    placeholder='Optional, e.g. 100'
                  />
                </div>

                <div className='space-y-2 md:col-span-2 xl:col-span-4'>
                  <Label htmlFor='dispatch-auto-courier-ids'>
                    Courier IDs for optimizer (optional)
                  </Label>
                  <Input
                    id='dispatch-auto-courier-ids'
                    value={autoCourierIdsInput}
                    onChange={(event) =>
                      setAutoCourierIdsInput(event.target.value)
                    }
                    placeholder='Comma-separated IDs, e.g. 101, 102, 205'
                  />
                  <p className='text-xs text-muted-foreground'>
                    Leave empty to let backend use eligible couriers of the
                    selected post office.
                  </p>
                </div>
              </div>

              <div className='flex flex-wrap gap-2'>
                <Button
                  type='button'
                  variant='outline'
                  onClick={() => void handlePreviewPlan()}
                  disabled={isOptimizing || !selectedPostOfficeId}
                >
                  {activeAction === 'preview' ? (
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  ) : null}
                  Preview plan
                </Button>
                <Button
                  type='button'
                  onClick={() => void handleAutoAssign()}
                  disabled={isAutoAssigning || !selectedPostOfficeId}
                >
                  {activeAction === 'auto' ? (
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  ) : (
                    <CheckCircle2 className='mr-2 h-4 w-4' />
                  )}
                  Auto assign
                </Button>
                <Button
                  type='button'
                  variant='outline'
                  onClick={() => void refetchOrders()}
                  disabled={isFetchingOrders}
                >
                  <RefreshCw className='mr-2 h-4 w-4' />
                  Refresh candidate orders
                </Button>
              </div>

              {!isTmsAdmin ? (
                <p className='text-xs text-muted-foreground'>
                  Manager scope is enforced by backend. You can dispatch only
                  for post offices assigned to you.
                </p>
              ) : null}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Manual Dispatch</CardTitle>
              <CardDescription>
                Select candidate orders and assign them to a courier.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <form
                onSubmit={handleApplyOrderFilters}
                className='flex flex-col gap-3 sm:flex-row'
              >
                <div className='relative flex-1'>
                  <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
                  <Input
                    className='pl-10'
                    value={orderKeywordInput}
                    onChange={(event) =>
                      setOrderKeywordInput(event.target.value)
                    }
                    placeholder='Search candidate orders...'
                    disabled={!selectedPostOfficeId}
                  />
                </div>
                <Button type='submit' disabled={!selectedPostOfficeId}>
                  Apply
                </Button>
              </form>

              <div className='grid gap-3 md:grid-cols-2'>
                <div className='space-y-2'>
                  <Label htmlFor='manual-courier-staff-id'>
                    Courier staff ID
                  </Label>
                  <Input
                    id='manual-courier-staff-id'
                    value={manualCourierStaffIdInput}
                    onChange={(event) =>
                      setManualCourierStaffIdInput(event.target.value)
                    }
                    placeholder='Required for manual assignment'
                  />
                </div>
                <div className='space-y-2'>
                  <Label>Quick pick courier</Label>
                  {suggestedCouriers.length > 0 ? (
                    <div className='flex flex-wrap gap-2'>
                      {suggestedCouriers.map((courier) => (
                        <Button
                          key={courier.id}
                          type='button'
                          variant='outline'
                          size='sm'
                          onClick={() =>
                            setManualCourierStaffIdInput(String(courier.id))
                          }
                        >
                          {courier.label}
                        </Button>
                      ))}
                    </div>
                  ) : (
                    <p className='text-xs text-muted-foreground'>
                      No suggested couriers yet. Run preview or auto assign to
                      get suggestions.
                    </p>
                  )}
                </div>
              </div>

              {isLoadingOrders ? (
                <div className='flex items-center gap-2 text-muted-foreground'>
                  <Loader2 className='h-4 w-4 animate-spin' />
                  Loading candidate orders...
                </div>
              ) : candidateOrders.length > 0 ? (
                <div className='space-y-3'>
                  <div className='flex flex-wrap items-center gap-2'>
                    <Button
                      type='button'
                      variant='outline'
                      size='sm'
                      onClick={handleSelectAllCurrentOrders}
                    >
                      Select all current page
                    </Button>
                    <Button
                      type='button'
                      variant='outline'
                      size='sm'
                      onClick={handleClearSelectedOrders}
                    >
                      Clear selection
                    </Button>
                    <Badge variant='secondary'>
                      Selected: {selectedOrderIds.length}
                    </Badge>
                  </div>

                  <div className='space-y-2'>
                    {candidateOrders.map((order) => renderOrderItem(order))}
                  </div>

                  <div className='flex items-center justify-between'>
                    <Button
                      type='button'
                      variant='outline'
                      onClick={() =>
                        setOrderPage((prev) => Math.max(prev - 1, 0))
                      }
                      disabled={!ordersData?.hasPrevious || isFetchingOrders}
                    >
                      Previous
                    </Button>
                    <span className='text-sm text-muted-foreground'>
                      Page {(ordersData?.currentPage ?? 0) + 1} /{' '}
                      {Math.max(ordersData?.totalPages ?? 1, 1)}
                    </span>
                    <Button
                      type='button'
                      variant='outline'
                      onClick={() => setOrderPage((prev) => prev + 1)}
                      disabled={!ordersData?.hasNext || isFetchingOrders}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              ) : (
                <p className='text-muted-foreground'>
                  No candidate orders found with CREATED/PICKUP_FAILED status on
                  this page.
                </p>
              )}

              <div className='pt-1'>
                <Button
                  type='button'
                  onClick={() => void handleManualAssign()}
                  disabled={isManualAssigning || selectedOrderIds.length === 0}
                >
                  {activeAction === 'manual' ? (
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  ) : (
                    <CheckCircle2 className='mr-2 h-4 w-4' />
                  )}
                  Manual assign selected orders
                </Button>
              </div>
            </CardContent>
          </Card>

          {optimizationResult ? (
            <Card>
              <CardHeader>
                <CardTitle>Latest Plan Preview</CardTitle>
                <CardDescription>
                  Post office {optimizationResult.postOfficeCode || '--'} -{' '}
                  {optimizationResult.postOfficeName || '--'}
                </CardDescription>
              </CardHeader>
              <CardContent className='space-y-3 text-sm'>
                <div className='grid gap-3 md:grid-cols-3'>
                  <div>
                    <p className='text-muted-foreground'>Total orders</p>
                    <p className='font-medium'>
                      {formatNumber(optimizationResult.totalOrders)}
                    </p>
                  </div>
                  <div>
                    <p className='text-muted-foreground'>Assigned orders</p>
                    <p className='font-medium'>
                      {formatNumber(optimizationResult.assignedOrders)}
                    </p>
                  </div>
                  <div>
                    <p className='text-muted-foreground'>Unassigned orders</p>
                    <p className='font-medium'>
                      {formatNumber(optimizationResult.unassignedOrders)}
                    </p>
                  </div>
                  <div>
                    <p className='text-muted-foreground'>Total routes</p>
                    <p className='font-medium'>
                      {formatNumber(optimizationResult.totalRoutes)}
                    </p>
                  </div>
                  <div>
                    <p className='text-muted-foreground'>Used routes</p>
                    <p className='font-medium'>
                      {formatNumber(optimizationResult.usedRoutes)}
                    </p>
                  </div>
                  <div>
                    <p className='text-muted-foreground'>Objective score</p>
                    <p className='font-medium'>
                      {formatNumber(optimizationResult.objectiveScore)}
                    </p>
                  </div>
                </div>

                {optimizationResult.routes &&
                optimizationResult.routes.length > 0 ? (
                  <div className='space-y-2'>
                    {optimizationResult.routes.map((routeItem, index) => (
                      <div
                        key={`${routeItem.courierStaffId ?? 'route'}-${index}`}
                        className='rounded-md border p-3'
                      >
                        <p className='font-medium'>
                          Courier: {routeItem.courierCode || '--'}
                          {routeItem.courierName
                            ? ` - ${routeItem.courierName}`
                            : ''}{' '}
                          (ID: {routeItem.courierStaffId ?? '--'})
                        </p>
                        <p className='text-xs text-muted-foreground'>
                          Stops: {formatNumber(routeItem.totalStops)} | Distance
                          (km): {formatNumber(routeItem.totalDistanceKm)} |{' '}
                          Lateness (min):{' '}
                          {formatNumber(routeItem.totalLatenessMinutes)}
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className='text-muted-foreground'>No routes in preview.</p>
                )}
              </CardContent>
            </Card>
          ) : null}

          {assignmentResult ? (
            <Card>
              <CardHeader>
                <CardTitle>Latest Assignment Result</CardTitle>
                <CardDescription>
                  Shift {assignmentResult.shift} on{' '}
                  {assignmentResult.tripDate || '--'}
                </CardDescription>
              </CardHeader>
              <CardContent className='space-y-3 text-sm'>
                <div className='grid gap-3 md:grid-cols-3'>
                  <div>
                    <p className='text-muted-foreground'>Requested orders</p>
                    <p className='font-medium'>
                      {formatNumber(assignmentResult.totalRequestedOrders)}
                    </p>
                  </div>
                  <div>
                    <p className='text-muted-foreground'>Assigned orders</p>
                    <p className='font-medium'>
                      {formatNumber(assignmentResult.assignedOrders)}
                    </p>
                  </div>
                  <div>
                    <p className='text-muted-foreground'>Unassigned orders</p>
                    <p className='font-medium'>
                      {formatNumber(assignmentResult.unassignedOrders)}
                    </p>
                  </div>
                </div>

                {assignmentResult.trips && assignmentResult.trips.length > 0 ? (
                  <div className='space-y-2'>
                    {assignmentResult.trips.map((trip, index) => (
                      <div
                        key={`${trip.tripId ?? 'trip'}-${index}`}
                        className='rounded-md border p-3'
                      >
                        <p className='font-medium'>
                          Trip: {trip.tripCode || '--'} | Courier:{' '}
                          {trip.courierCode || '--'}
                          {trip.courierName ? ` - ${trip.courierName}` : ''}
                        </p>
                        <p className='text-xs text-muted-foreground'>
                          Planned: {formatDateTime(trip.plannedStartTime)} -{' '}
                          {formatDateTime(trip.plannedEndTime)} | Stops:{' '}
                          {formatNumber(trip.totalStops)}
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className='text-muted-foreground'>No trips created.</p>
                )}
              </CardContent>
            </Card>
          ) : null}
        </>
      )}
    </div>
  );
};
