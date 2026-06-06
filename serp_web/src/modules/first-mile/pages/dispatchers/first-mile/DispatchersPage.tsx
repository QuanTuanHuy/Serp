/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile dispatchers page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
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
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useManualAssignPickupOrdersMutation,
  useOptimizePickupPlanMutation,
} from '../../../api';
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
  Province,
  Ward,
} from '../../../types';
import {
  evaluateManualAssignRisks,
  formatManualAssignRiskLines,
  getManualShiftLabel,
  type ManualAssignRisk,
} from './manualAssignRisks';

const POST_OFFICE_PAGE_SIZE = 200;
const MANUAL_ORDER_PAGE_SIZE = 200;

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

  const [setupSelectedPostOfficeId, setSetupSelectedPostOfficeId] =
    React.useState('');
  const [setupShift, setSetupShift] = React.useState<PickupShift>('MORNING');
  const [setupTripDate, setSetupTripDate] = React.useState(
    getTodayDateInputValue
  );

  const [manualProvinceCode, setManualProvinceCode] = React.useState('');
  const [manualWardCode, setManualWardCode] = React.useState('');
  const [manualSelectedPostOfficeId, setManualSelectedPostOfficeId] =
    React.useState('');
  const [manualShift, setManualShift] = React.useState<PickupShift>('MORNING');
  const [manualTripDate, setManualTripDate] = React.useState(
    getTodayDateInputValue
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
  const [isManualConfirmOpen, setIsManualConfirmOpen] = React.useState(false);
  const [manualAssignRisks, setManualAssignRisks] = React.useState<
    ManualAssignRisk[]
  >([]);
  const [pendingManualPayload, setPendingManualPayload] =
    React.useState<ManualAssignPickupOrdersRequest | null>(null);

  const [activeAction, setActiveAction] = React.useState<
    'preview' | 'auto' | 'manual' | null
  >(null);

  const { data: provincesData, isLoading: isLoadingProvinces } =
    useGetProvincesQuery(
      {
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
      },
      {
        skip: !canDispatch,
      }
    );

  const provinceOptions = React.useMemo<Province[]>(
    () => provincesData?.items ?? [],
    [provincesData]
  );

  const { data: manualWardsData, isLoading: isLoadingManualWards } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: manualProvinceCode,
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
      },
      {
        skip: !canDispatch || !manualProvinceCode,
      }
    );

  const manualWardOptions = React.useMemo<Ward[]>(() => {
    return manualWardsData?.items ?? [];
  }, [manualWardsData]);

  const { data: setupPostOfficesData, isLoading: isLoadingSetupPostOffices } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
      },
      {
        skip: !canDispatch,
      }
    );

  const setupPostOfficeOptions = React.useMemo<PostOffice[]>(
    () => setupPostOfficesData?.items ?? [],
    [setupPostOfficesData]
  );

  const { data: manualPostOfficesData, isLoading: isLoadingManualPostOffices } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
        provinceCode: manualProvinceCode || undefined,
        ...(manualWardCode ? { wardCode: manualWardCode } : {}),
      },
      {
        skip: !canDispatch || !manualProvinceCode,
      }
    );

  const manualPostOfficeOptions = React.useMemo<PostOffice[]>(
    () => manualPostOfficesData?.items ?? [],
    [manualPostOfficesData]
  );

  React.useEffect(() => {
    if (!setupPostOfficeOptions.length) {
      return;
    }

    const hasSelectedPostOffice = setupPostOfficeOptions.some(
      (postOffice) => String(postOffice.id) === setupSelectedPostOfficeId
    );

    if (!hasSelectedPostOffice) {
      setSetupSelectedPostOfficeId(String(setupPostOfficeOptions[0].id));
    }
  }, [setupPostOfficeOptions, setupSelectedPostOfficeId]);

  React.useEffect(() => {
    if (!provinceOptions.length) {
      return;
    }

    const hasSelectedProvince = provinceOptions.some(
      (province) => province.provinceCode === manualProvinceCode
    );

    if (!hasSelectedProvince) {
      setManualProvinceCode(provinceOptions[0].provinceCode);
    }
  }, [manualProvinceCode, provinceOptions]);

  React.useEffect(() => {
    if (!manualProvinceCode || !manualPostOfficeOptions.length) {
      setManualSelectedPostOfficeId('');
      return;
    }

    const hasSelectedPostOffice = manualPostOfficeOptions.some(
      (postOffice) => String(postOffice.id) === manualSelectedPostOfficeId
    );

    if (!hasSelectedPostOffice) {
      setManualSelectedPostOfficeId(String(manualPostOfficeOptions[0].id));
    }
  }, [manualPostOfficeOptions, manualProvinceCode, manualSelectedPostOfficeId]);

  const setupSelectedPostOfficeNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(setupSelectedPostOfficeId),
    [setupSelectedPostOfficeId]
  );

  const manualSelectedPostOffice = React.useMemo(() => {
    return manualPostOfficeOptions.find(
      (postOffice) => String(postOffice.id) === manualSelectedPostOfficeId
    );
  }, [manualPostOfficeOptions, manualSelectedPostOfficeId]);

  const manualSelectedPostOfficeNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(manualSelectedPostOfficeId),
    [manualSelectedPostOfficeId]
  );

  const manualSelectedPostOfficeCode = manualSelectedPostOffice?.code;

  const { data: setupCouriersData, isLoading: isLoadingSetupCouriers } =
    useGetActiveCouriersByPostOfficeQuery(setupSelectedPostOfficeNumericId ?? 0, {
      skip: !canDispatch || !setupSelectedPostOfficeNumericId,
    });

  const { data: manualCouriersData, isLoading: isLoadingManualCouriers } =
    useGetActiveCouriersByPostOfficeQuery(
      manualSelectedPostOfficeNumericId ?? 0,
      {
        skip: !canDispatch || !manualSelectedPostOfficeNumericId,
      }
    );

  const setupCourierOptions = React.useMemo<DispatchCourierOption[]>(() => {
    const couriers = setupCouriersData ?? [];

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
  }, [setupCouriersData]);

  const manualCourierOptions = React.useMemo<DispatchCourierOption[]>(() => {
    const couriers = manualCouriersData ?? [];

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
  }, [manualCouriersData]);

  const setupCourierIdSet = React.useMemo(
    () => new Set(setupCourierOptions.map((courier) => courier.id)),
    [setupCourierOptions]
  );

  const manualCourierIdSet = React.useMemo(
    () => new Set(manualCourierOptions.map((courier) => courier.id)),
    [manualCourierOptions]
  );

  const {
    data: manualOrdersData,
    isLoading: isLoadingManualOrders,
    refetch: refetchManualOrders,
  } = useGetOrdersQuery(
    {
      page: 0,
      size: MANUAL_ORDER_PAGE_SIZE,
      ...(manualSelectedPostOfficeCode
        ? { originPostOfficeCode: manualSelectedPostOfficeCode }
        : {}),
    },
    {
      skip: !canDispatch || !manualSelectedPostOfficeId,
    }
  );

  const manualCandidateOrders = React.useMemo(() => {
    return (manualOrdersData?.items ?? []).filter((order) =>
      isCandidateOrderStatus(order.status)
    );
  }, [manualOrdersData]);

  const manualOrdersById = React.useMemo(() => {
    const orderMap = new Map<number, FirstMileOrderDetail>();
    for (const order of manualOrdersData?.items ?? []) {
      orderMap.set(order.id, order);
    }
    return orderMap;
  }, [manualOrdersData]);

  const suggestedCouriers = React.useMemo(
    () => extractSuggestedCouriers(optimizationResult, assignmentResult),
    [optimizationResult, assignmentResult]
  );

  React.useEffect(() => {
    setSelectedAutoCourierIds([]);
    setOptimizationResult(null);
    setAssignmentResult(null);
  }, [setupSelectedPostOfficeId]);

  React.useEffect(() => {
    setManualWardCode('');
    setManualSelectedPostOfficeId('');
    setSelectedOrderIds([]);
    setSelectedManualCourierId('');
  }, [manualProvinceCode]);

  React.useEffect(() => {
    setManualSelectedPostOfficeId('');
    setSelectedOrderIds([]);
    setSelectedManualCourierId('');
  }, [manualWardCode]);

  React.useEffect(() => {
    setSelectedOrderIds([]);
    setSelectedManualCourierId('');
  }, [manualSelectedPostOfficeId]);

  React.useEffect(() => {
    setSelectedAutoCourierIds((prev) => {
      const next = prev.filter((courierId) => setupCourierIdSet.has(courierId));
      const isSame =
        next.length === prev.length &&
        next.every((courierId, index) => courierId === prev[index]);

      return isSame ? prev : next;
    });
  }, [setupCourierIdSet]);

  React.useEffect(() => {
    setSelectedManualCourierId((prev) => {
      if (!prev) {
        return prev;
      }

      const parsedValue = Number(prev);
      if (
        !Number.isInteger(parsedValue) ||
        !manualCourierIdSet.has(parsedValue)
      ) {
        return '';
      }

      return prev;
    });
  }, [manualCourierIdSet]);

  const [optimizePickupPlan, { isLoading: isOptimizing }] =
    useOptimizePickupPlanMutation();
  const [autoAssignPickupPlan, { isLoading: isAutoAssigning }] =
    useAutoAssignPickupPlanMutation();
  const [manualAssignPickupOrders, { isLoading: isManualAssigning }] =
    useManualAssignPickupOrdersMutation();

  const buildSetupDispatchContext = React.useCallback(() => {
    if (!canDispatch) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can dispatch first-mile orders.'
      );
      return null;
    }

    const postOfficeId = parseOptionalPositiveInteger(
      setupSelectedPostOfficeId
    );
    if (!postOfficeId) {
      notification.error('Please select a post office for dispatch setup.');
      return null;
    }

    if (!setupTripDate) {
      notification.error('Please select trip date for dispatch setup.');
      return null;
    }

    const shiftWindow = buildShiftWindow(setupTripDate, setupShift);

    return {
      postOfficeId,
      shiftWindow,
    };
  }, [
    canDispatch,
    notification,
    setupSelectedPostOfficeId,
    setupShift,
    setupTripDate,
  ]);

  const buildManualDispatchContext = React.useCallback(() => {
    if (!canDispatch) {
      notification.error(
        'Only TMS_ADMIN or TMS_POSTOFFICER_MANAGER can dispatch first-mile orders.'
      );
      return null;
    }

    const postOfficeId = parseOptionalPositiveInteger(
      manualSelectedPostOfficeId
    );
    if (!postOfficeId) {
      notification.error('Please select a post office for manual dispatch.');
      return null;
    }

    if (!manualTripDate) {
      notification.error('Please select trip date for manual dispatch.');
      return null;
    }

    const shiftWindow = buildShiftWindow(manualTripDate, manualShift);

    return {
      postOfficeId,
      shiftWindow,
    };
  }, [
    canDispatch,
    manualSelectedPostOfficeId,
    manualShift,
    manualTripDate,
    notification,
  ]);

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

  const handleClearSelectedOrders = () => {
    setSelectedOrderIds([]);
  };

  const handleManualProvinceChange = (value: string) => {
    setManualProvinceCode(value);
  };

  const handleManualWardChange = (value: string) => {
    setManualWardCode(value === 'ALL' ? '' : value);
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
    const context = buildSetupDispatchContext();
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
        description: getErrorMessage(error),
      });
    } finally {
      setActiveAction(null);
    }
  };

  const handleAutoAssign = async () => {
    const context = buildSetupDispatchContext();
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
      shift: setupShift,
      trip_date: setupTripDate,
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

      if (manualSelectedPostOfficeId) {
        void refetchManualOrders();
      }
    } catch (error) {
      notification.error('Failed to auto assign pickup orders.', {
        description: getErrorMessage(error),
      });
    } finally {
      setActiveAction(null);
    }
  };

  const executeManualAssign = React.useCallback(
    async (payload: ManualAssignPickupOrdersRequest, forceAssign: boolean) => {
      setActiveAction('manual');
      try {
        const result = await manualAssignPickupOrders({
          ...payload,
          force_assign: forceAssign,
        }).unwrap();
        setAssignmentResult(result);
        setSelectedOrderIds([]);
        setPendingManualPayload(null);
        setManualAssignRisks([]);
        setIsManualConfirmOpen(false);

        const assignedCount = result.assignedOrders ?? 0;
        const requestedCount = result.totalRequestedOrders ?? 0;

        if (assignedCount < requestedCount) {
          const unassignedDetails = result.unassignedOrderDetails ?? [];

          if (!forceAssign && unassignedDetails.length > 0) {
            const optimizerRisks: ManualAssignRisk[] = unassignedDetails.map(
              (item, index) => ({
                id: `optimizer-unassigned-${item.orderId ?? index}`,
                message: `Không thể xếp lộ trình tối ưu (${item.reason ?? 'NO_FEASIBLE_INSERTION'}).`,
                orderCodes: [
                  item.orderCode ??
                    item.customerOrderCode ??
                    (item.orderId ? `#${item.orderId}` : 'N/A'),
                ],
              })
            );

            setPendingManualPayload(payload);
            setManualAssignRisks(optimizerRisks);
            setIsManualConfirmOpen(true);

            notification.warning('Cần xác nhận gán cưỡng bức.', {
              description: `Chỉ gán được ${assignedCount}/${requestedCount} đơn theo tối ưu lộ trình.`,
            });
            return;
          }

          const reasonSummary = unassignedDetails
            .slice(0, 3)
            .map(
              (item) =>
                `${item.orderCode ?? item.orderId}: ${item.reason ?? 'UNKNOWN'}`
            )
            .join(' | ');

          notification.warning('Gán thủ công chưa đủ số đơn.', {
            description: `Đã gán ${assignedCount}/${requestedCount} đơn.${reasonSummary ? ` ${reasonSummary}` : ''}`,
          });
        } else {
          notification.success('Gán thủ công thành công.', {
            description: `Đã gán ${assignedCount}/${requestedCount} đơn.`,
          });
        }

        void refetchManualOrders();
      } catch (error) {
        notification.error('Gán thủ công thất bại.', {
          description: getErrorMessage(error),
        });
      } finally {
        setActiveAction(null);
      }
    },
    [manualAssignPickupOrders, notification, refetchManualOrders]
  );

  const handleManualAssign = async () => {
    const context = buildManualDispatchContext();
    if (!context) {
      return;
    }

    if (selectedOrderIds.length === 0) {
      notification.error('Vui lòng chọn ít nhất một đơn để gán thủ công.');
      return;
    }

    const courierStaffId = parseOptionalPositiveInteger(
      selectedManualCourierId
    );
    if (!courierStaffId) {
      notification.error('Vui lòng chọn bưu tá để gán thủ công.');
      return;
    }

    const businessSettings = buildBusinessDispatchSettings();

    const payload: ManualAssignPickupOrdersRequest = {
      post_office_id: context.postOfficeId,
      courier_staff_id: courierStaffId,
      order_ids: selectedOrderIds,
      shift: manualShift,
      trip_date: manualTripDate,
      planning_start_time: context.shiftWindow.planningStartTime,
      planning_end_time: context.shiftWindow.planningEndTime,
      ...(businessSettings.vehicle
        ? { vehicle: businessSettings.vehicle }
        : {}),
      optimization_goal: businessSettings.optimization_goal,
      optimization_effort: businessSettings.optimization_effort,
    };

    const risks = evaluateManualAssignRisks({
      postOffice: manualSelectedPostOffice,
      postOfficeCode: manualSelectedPostOfficeCode,
      courierStaffId,
      courierOptions: manualCourierOptions,
      selectedOrderIds,
      ordersById: manualOrdersById,
      planningStartTime: context.shiftWindow.planningStartTime,
      planningEndTime: context.shiftWindow.planningEndTime,
    });

    if (risks.length === 0) {
      await executeManualAssign(payload, false);
      return;
    }

    setPendingManualPayload(payload);
    setManualAssignRisks(risks);
    setIsManualConfirmOpen(true);
  };

  const handleConfirmManualAssign = async () => {
    if (!pendingManualPayload) {
      return;
    }

    await executeManualAssign(pendingManualPayload, true);
  };

  const manualAssignRiskLines = React.useMemo(
    () => formatManualAssignRiskLines(manualAssignRisks),
    [manualAssignRisks]
  );

  const pendingCourierLabel = React.useMemo(() => {
    const courierId = pendingManualPayload?.courier_staff_id;
    if (!courierId) {
      return '--';
    }

    const courier = manualCourierOptions.find((item) => item.id === courierId);
    if (!courier) {
      return String(courierId);
    }

    return `${courier.code} - ${courier.fullName}`;
  }, [manualCourierOptions, pendingManualPayload]);

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
            postOfficeOptions={setupPostOfficeOptions}
            courierOptions={setupCourierOptions}
            selectedPostOfficeId={setupSelectedPostOfficeId}
            onPostOfficeChange={setSetupSelectedPostOfficeId}
            isLoadingPostOffices={isLoadingSetupPostOffices}
            isLoadingCouriers={isLoadingSetupCouriers}
            shift={setupShift}
            onShiftChange={setSetupShift}
            tripDate={setupTripDate}
            onTripDateChange={setSetupTripDate}
            orderLimitInput={orderLimitInput}
            onOrderLimitInputChange={setOrderLimitInput}
            selectedAutoCourierIds={selectedAutoCourierIds}
            onToggleAutoCourier={handleToggleAutoCourier}
            onClearAutoCourierSelection={() => setSelectedAutoCourierIds([])}
            businessValues={businessValues}
            businessHandlers={businessHandlers}
            onPreviewPlan={() => void handlePreviewPlan()}
            onAutoAssign={() => void handleAutoAssign()}
            isOptimizing={isOptimizing}
            isAutoAssigning={isAutoAssigning}
            activeAction={activeAction}
            isTmsAdmin={isTmsAdmin}
          />

          <ManualDispatchCard
            provinceOptions={provinceOptions}
            wardOptions={manualWardOptions}
            selectedProvinceCode={manualProvinceCode}
            onProvinceChange={handleManualProvinceChange}
            selectedWardCode={manualWardCode || 'ALL'}
            onWardChange={handleManualWardChange}
            isLoadingProvinces={isLoadingProvinces}
            isLoadingWards={isLoadingManualWards}
            postOfficeOptions={manualPostOfficeOptions}
            selectedPostOfficeId={manualSelectedPostOfficeId}
            onPostOfficeChange={setManualSelectedPostOfficeId}
            isLoadingPostOffices={isLoadingManualPostOffices}
            shift={manualShift}
            onShiftChange={setManualShift}
            tripDate={manualTripDate}
            onTripDateChange={setManualTripDate}
            courierOptions={manualCourierOptions}
            selectedManualCourierId={selectedManualCourierId}
            onManualCourierIdChange={setSelectedManualCourierId}
            isLoadingCouriers={isLoadingManualCouriers}
            suggestedCouriers={suggestedCouriers}
            onQuickPickCourier={(courierId) => {
              if (!manualCourierIdSet.has(courierId)) {
                notification.error(
                  'Suggested courier is not active for the selected post office.'
                );
                return;
              }

              setSelectedManualCourierId(String(courierId));
            }}
            isLoadingOrders={isLoadingManualOrders}
            candidateOrders={manualCandidateOrders}
            selectedOrderIds={selectedOrderIds}
            onOrderSelectionChange={setSelectedOrderIds}
            onClearSelectedOrders={handleClearSelectedOrders}
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

          <ConfirmDialog
            open={isManualConfirmOpen}
            onOpenChange={setIsManualConfirmOpen}
            title='Cảnh báo trước khi gán đơn'
            description={
              <div className='space-y-3 text-left'>
                <p>
                  Hệ thống phát hiện {manualAssignRisks.length} nhóm rủi ro. Nếu
                  bạn xác nhận, đơn vẫn được gán trực tiếp cho bưu tá (bỏ qua
                  ràng buộc tối ưu lộ trình).
                </p>
                <div className='rounded-md border bg-muted/40 p-3 text-xs text-muted-foreground'>
                  <p>
                    Bưu cục: {manualSelectedPostOffice?.code ?? '--'} -{' '}
                    {manualSelectedPostOffice?.name ?? '--'}
                  </p>
                  <p>Bưu tá: {pendingCourierLabel}</p>
                  <p>
                    Ca: {getManualShiftLabel(manualShift)} | Ngày:{' '}
                    {manualTripDate}
                  </p>
                  <p>Số đơn chọn: {selectedOrderIds.length}</p>
                </div>
                <ul className='list-disc space-y-2 pl-5 text-sm'>
                  {manualAssignRiskLines.map((warning, index) => (
                    <li key={`${warning}-${index}`}>{warning}</li>
                  ))}
                </ul>
              </div>
            }
            confirmText='Vẫn gán đơn'
            cancelText='Hủy'
            onConfirm={() => void handleConfirmManualAssign()}
            onCancel={() => {
              setIsManualConfirmOpen(false);
              setPendingManualPayload(null);
              setManualAssignRisks([]);
            }}
            isLoading={isManualAssigning}
            variant='destructive'
          />
        </>
      )}
    </div>
  );
};
