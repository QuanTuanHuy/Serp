/**
 * Author: GitHub Copilot
 * Description: Part of Serp Project - First-mile dispatchers page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import { useNotification } from '@/shared/hooks';
import {
  AssignmentResultCard,
  DispatchAccessScopeCard,
  DispatchNoAccessCard,
  DispatchSetupCard,
  ManualDispatchCard,
  PlanPreviewCard,
  type BusinessDispatchSettings,
  type DispatchCourierOption,
  type DispatchOptimizationEffortOption,
  type DispatchOptimizationGoalOption,
  type DispatchSetupBusinessHandlers,
  type DispatchSetupBusinessValues,
  type DispatcherAccessScope,
  type RoutingVehicleOption,
  type SuggestedCourier,
} from './components';
import {
  useAutoAssignPickupPlanMutation,
  useGetActiveCouriersByPostOfficeQuery,
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
  PostOfficeStaff,
} from '../../types';

const POST_OFFICE_PAGE_SIZE = 200;
const ORDER_PAGE_SIZE = 50;

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

const extractDispatchErrorDescription = (error: unknown): string => {
  const baseMessage = getErrorMessage(error);
  const detail = (error as { data?: { detail?: string } })?.data?.detail;

  if (!detail) {
    return baseMessage;
  }

  if (baseMessage.includes(detail)) {
    return baseMessage;
  }

  return `${baseMessage} | ${detail}`;
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

const isCandidateOrderStatus = (status: FirstMileOrderStatus): boolean => {
  return CANDIDATE_ORDER_STATUSES.includes(status);
};

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

  const [selectedAutoCourierIds, setSelectedAutoCourierIds] = React.useState<
    number[]
  >([]);
  const [selectedManualCourierId, setSelectedManualCourierId] =
    React.useState('');
  const [orderLimitInput, setOrderLimitInput] = React.useState('');
  const [vehicleOption, setVehicleOption] =
    React.useState<RoutingVehicleOption>('DEFAULT');
  const [optimizationGoal, setOptimizationGoal] =
    React.useState<DispatchOptimizationGoalOption>('BALANCED');
  const [optimizationEffort, setOptimizationEffort] =
    React.useState<DispatchOptimizationEffortOption>('STANDARD');

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

  const selectedPostOfficeNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(selectedPostOfficeId),
    [selectedPostOfficeId]
  );

  const selectedPostOfficeCode = selectedPostOffice?.code;

  const { data: dispatchCouriersData, isLoading: isLoadingDispatchCouriers } =
    useGetActiveCouriersByPostOfficeQuery(selectedPostOfficeNumericId ?? 0, {
      skip: !canDispatch || !selectedPostOfficeNumericId,
    });

  const dispatchCourierOptions = React.useMemo<DispatchCourierOption[]>(() => {
    const couriers = dispatchCouriersData ?? [];

    return couriers
      .filter(
        (
          courier: PostOfficeStaff
        ): courier is PostOfficeStaff & { id: number } => {
          return Number.isInteger(courier.id) && courier.id > 0;
        }
      )
      .map((courier) => {
        const code = courier.code?.trim() || `COURIER-${courier.id}`;
        const fullName = courier.fullName?.trim() || 'Unknown courier';

        return {
          id: courier.id,
          code,
          fullName,
        };
      })
      .sort((a, b) => {
        const nameCompare = a.fullName.localeCompare(b.fullName, 'en-US', {
          sensitivity: 'base',
        });

        if (nameCompare !== 0) {
          return nameCompare;
        }

        return a.id - b.id;
      });
  }, [dispatchCouriersData]);

  const dispatchCourierIdSet = React.useMemo(
    () => new Set(dispatchCourierOptions.map((courier) => courier.id)),
    [dispatchCourierOptions]
  );

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
    setSelectedAutoCourierIds([]);
    setSelectedManualCourierId('');
    setOptimizationResult(null);
    setAssignmentResult(null);
  }, [selectedPostOfficeId]);

  React.useEffect(() => {
    setSelectedAutoCourierIds((prev) => {
      const next = prev.filter((courierId) =>
        dispatchCourierIdSet.has(courierId)
      );
      const isSame =
        next.length === prev.length &&
        next.every((courierId, index) => courierId === prev[index]);

      return isSame ? prev : next;
    });

    setSelectedManualCourierId((prev) => {
      if (!prev) {
        return prev;
      }

      const parsedValue = Number(prev);
      if (
        !Number.isInteger(parsedValue) ||
        !dispatchCourierIdSet.has(parsedValue)
      ) {
        return '';
      }

      return prev;
    });
  }, [dispatchCourierIdSet]);

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

  const parseSelectedCourierIds = React.useCallback(
    () => selectedAutoCourierIds,
    [selectedAutoCourierIds]
  );

  const parseOptionalOrderLimit = React.useCallback(() => {
    const parsedOrderLimit = parseOptionalPositiveInteger(orderLimitInput);

    if (orderLimitInput.trim() && parsedOrderLimit === undefined) {
      notification.error('Order limit must be a positive integer.');
      return null;
    }

    return parsedOrderLimit;
  }, [notification, orderLimitInput]);

  const buildBusinessDispatchSettings = React.useCallback(() => {
    const settings: BusinessDispatchSettings = {
      optimization_goal: optimizationGoal,
      optimization_effort: optimizationEffort,
    };

    if (vehicleOption !== 'DEFAULT') {
      settings.vehicle = vehicleOption;
    }

    return settings;
  }, [optimizationEffort, optimizationGoal, vehicleOption]);

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

  const handleToggleAutoCourier = (courierId: number, checked: boolean) => {
    setSelectedAutoCourierIds((prev) => {
      if (checked) {
        if (prev.includes(courierId)) {
          return prev;
        }

        return [...prev, courierId];
      }

      return prev.filter((id) => id !== courierId);
    });
  };

  const handlePreviewPlan = async () => {
    const context = buildDispatchContext();
    if (!context) {
      return;
    }

    const parsedCourierIds = parseSelectedCourierIds();

    const parsedOrderLimit = parseOptionalOrderLimit();
    if (parsedOrderLimit === null) {
      return;
    }

    const businessSettings = buildBusinessDispatchSettings();

    const payload: OptimizePickupPlanRequest = {
      post_office_id: context.postOfficeId,
      planning_start_time: context.shiftWindow.planningStartTime,
      planning_end_time: context.shiftWindow.planningEndTime,
      candidate_statuses: CANDIDATE_ORDER_STATUSES,
      ...(parsedCourierIds.length ? { courier_ids: parsedCourierIds } : {}),
      ...(parsedOrderLimit ? { order_limit: parsedOrderLimit } : {}),
      ...businessSettings,
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
        description: extractDispatchErrorDescription(error),
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

    const parsedCourierIds = parseSelectedCourierIds();

    const parsedOrderLimit = parseOptionalOrderLimit();
    if (parsedOrderLimit === null) {
      return;
    }

    const businessSettings = buildBusinessDispatchSettings();

    const payload: AutoAssignPickupPlanRequest = {
      post_office_id: context.postOfficeId,
      shift,
      trip_date: tripDate,
      planning_start_time: context.shiftWindow.planningStartTime,
      planning_end_time: context.shiftWindow.planningEndTime,
      candidate_statuses: CANDIDATE_ORDER_STATUSES,
      ...(parsedCourierIds.length ? { courier_ids: parsedCourierIds } : {}),
      ...(parsedOrderLimit ? { order_limit: parsedOrderLimit } : {}),
      ...businessSettings,
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
        description: extractDispatchErrorDescription(error),
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
      selectedManualCourierId
    );
    if (!courierStaffId) {
      notification.error('Please select a courier for manual assignment.');
      return;
    }

    if (!dispatchCourierIdSet.has(courierStaffId)) {
      notification.error(
        'Selected courier is not active for the selected post office.'
      );
      return;
    }

    const businessSettings = buildBusinessDispatchSettings();

    const payload: ManualAssignPickupOrdersRequest = {
      post_office_id: context.postOfficeId,
      courier_staff_id: courierStaffId,
      order_ids: selectedOrderIds,
      shift,
      trip_date: tripDate,
      planning_start_time: context.shiftWindow.planningStartTime,
      planning_end_time: context.shiftWindow.planningEndTime,
      ...(businessSettings.vehicle
        ? { vehicle: businessSettings.vehicle }
        : {}),
      optimization_goal: businessSettings.optimization_goal,
      optimization_effort: businessSettings.optimization_effort,
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
        description: extractDispatchErrorDescription(error),
      });
    } finally {
      setActiveAction(null);
    }
  };

  const businessValues: DispatchSetupBusinessValues = {
    vehicleOption,
    optimizationGoal,
    optimizationEffort,
  };

  const businessHandlers: DispatchSetupBusinessHandlers = {
    onVehicleOptionChange: setVehicleOption,
    onOptimizationGoalChange: setOptimizationGoal,
    onOptimizationEffortChange: setOptimizationEffort,
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>Dispatchers</h1>
        <p className='text-muted-foreground'>
          Dispatch first-mile pickup orders by post office and courier scope.
        </p>
      </div>

      <DispatchAccessScopeCard
        canDispatch={canDispatch}
        scopeBadgeLabel={getScopeBadgeLabel(accessScope)}
        scopeDescription={getScopeDescription(accessScope)}
      />

      {!canDispatch ? (
        <DispatchNoAccessCard message='You do not have permission to access dispatch operations.' />
      ) : (
        <>
          <DispatchSetupCard
            postOfficeOptions={postOfficeOptions}
            courierOptions={dispatchCourierOptions}
            selectedPostOfficeId={selectedPostOfficeId}
            onPostOfficeChange={(value) => {
              setOrderPage(0);
              setSelectedPostOfficeId(value);
            }}
            isLoadingPostOffices={isLoadingPostOffices}
            isLoadingCouriers={isLoadingDispatchCouriers}
            shift={shift}
            onShiftChange={setShift}
            tripDate={tripDate}
            onTripDateChange={setTripDate}
            orderLimitInput={orderLimitInput}
            onOrderLimitInputChange={setOrderLimitInput}
            selectedAutoCourierIds={selectedAutoCourierIds}
            onToggleAutoCourier={handleToggleAutoCourier}
            onClearAutoCourierSelection={() => setSelectedAutoCourierIds([])}
            businessValues={businessValues}
            businessHandlers={businessHandlers}
            onPreviewPlan={() => void handlePreviewPlan()}
            onAutoAssign={() => void handleAutoAssign()}
            onRefreshCandidateOrders={() => void refetchOrders()}
            isOptimizing={isOptimizing}
            isAutoAssigning={isAutoAssigning}
            isFetchingOrders={isFetchingOrders}
            activeAction={activeAction}
            isTmsAdmin={isTmsAdmin}
          />

          <ManualDispatchCard
            selectedPostOfficeId={selectedPostOfficeId}
            orderKeywordInput={orderKeywordInput}
            onOrderKeywordInputChange={setOrderKeywordInput}
            onApplyOrderFilters={handleApplyOrderFilters}
            courierOptions={dispatchCourierOptions}
            selectedManualCourierId={selectedManualCourierId}
            onManualCourierIdChange={setSelectedManualCourierId}
            isLoadingCouriers={isLoadingDispatchCouriers}
            suggestedCouriers={suggestedCouriers}
            onQuickPickCourier={(courierId) => {
              if (!dispatchCourierIdSet.has(courierId)) {
                notification.error(
                  'Suggested courier is not active for the selected post office.'
                );
                return;
              }

              setSelectedManualCourierId(String(courierId));
            }}
            isLoadingOrders={isLoadingOrders}
            candidateOrders={candidateOrders}
            selectedOrderIds={selectedOrderIds}
            selectedOrderIdSet={selectedOrderIdSet}
            onToggleOrder={handleToggleOrder}
            onSelectAllCurrentOrders={handleSelectAllCurrentOrders}
            onClearSelectedOrders={handleClearSelectedOrders}
            currentPage={ordersData?.currentPage ?? 0}
            totalPages={ordersData?.totalPages ?? 1}
            hasPrevious={Boolean(ordersData?.hasPrevious)}
            hasNext={Boolean(ordersData?.hasNext)}
            isFetchingOrders={isFetchingOrders}
            onPreviousPage={() => setOrderPage((prev) => Math.max(prev - 1, 0))}
            onNextPage={() => setOrderPage((prev) => prev + 1)}
            onManualAssign={() => void handleManualAssign()}
            isManualAssigning={isManualAssigning}
            activeAction={activeAction}
          />

          {optimizationResult ? (
            <PlanPreviewCard
              optimizationResult={optimizationResult}
              formatNumber={formatNumber}
            />
          ) : null}

          {assignmentResult ? (
            <AssignmentResultCard
              assignmentResult={assignmentResult}
              formatDateTime={formatDateTime}
              formatNumber={formatNumber}
            />
          ) : null}
        </>
      )}
    </div>
  );
};
