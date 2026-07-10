/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Last-mile dispatchers page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import {
  AssignmentResultCard,
  CandidateOrdersPanel,
  DispatchNoAccessCard,
  DispatchSetupCard,
  ManualDispatchCard,
  PlanPreviewCard,
  type BusinessDispatchSettings,
  type DispatchCourierOption,
  type DispatchOptimizationGoalOption,
  type DispatchSetupBusinessHandlers,
  type DispatchSetupBusinessValues,
  type DispatcherAccessScope,
  type RoutingVehicleOption,
  type SuggestedCourier,
} from '../first-mile/components';
import {
  useAutoAssignDeliveryPlanMutation,
  useGetActiveCouriersByPostOfficeQuery,
  useGetOrdersQuery,
  useGetPostOfficesQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useManualAssignDeliveryOrdersMutation,
  useOptimizeDeliveryPlanMutation,
} from '../../../api';
import type {
  AutoAssignDeliveryPlanRequest,
  DeliveryAssignmentResponse,
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  ManualAssignDeliveryOrdersRequest,
  PickupOptimizationResponse,
  PickupShift,
  PostOffice,
  PostOfficeStaff,
  Province,
  Ward,
} from '../../../types';
import {
  formatManualAssignRiskLines,
  getManualShiftLabel,
  type ManualAssignRisk,
} from '../first-mile/manualAssignRisks';
import { formatOrderStatusLabel } from '../../../utils/orderStatusLabels';

const POST_OFFICE_PAGE_SIZE = 200;
const MANUAL_ORDER_PAGE_SIZE = 200;

type DispatcherTabValue = 'automatic' | 'manual';

const CANDIDATE_ORDER_STATUSES: FirstMileOrderStatus[] = [
  'READY_FOR_DELIVERY',
  'DELIVERY_FAILED',
];
const DEFAULT_ROUTING_VEHICLE: RoutingVehicleOption = 'BIKE';

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

const formatDateTime = (value?: string): string => {
  if (!value) {
    return '--';
  }

  const parsedDate = new Date(value);
  if (Number.isNaN(parsedDate.getTime())) {
    return value;
  }

  return parsedDate.toLocaleString('vi-VN');
};

const formatNumber = (value?: number): string => {
  if (value === undefined || value === null) {
    return '--';
  }

  return String(value);
};

const isCandidateOrderStatus = (status: FirstMileOrderStatus): boolean => {
  return CANDIDATE_ORDER_STATUSES.includes(status);
};

const hasValidReceiverLocation = (order: FirstMileOrderDetail): boolean => {
  const latitude = order.receiverLatitude;
  const longitude = order.receiverLongitude;

  return (
    latitude !== undefined &&
    latitude !== null &&
    longitude !== undefined &&
    longitude !== null &&
    Number.isFinite(latitude) &&
    Number.isFinite(longitude) &&
    latitude >= -90 &&
    latitude <= 90 &&
    longitude >= -180 &&
    longitude <= 180
  );
};

const getOrderLabel = (order: FirstMileOrderDetail): string => {
  return order.customerOrderCode
    ? `${order.orderCode} (${order.customerOrderCode})`
    : order.orderCode;
};

const pushRisk = (
  risks: ManualAssignRisk[],
  id: string,
  message: string,
  orderCodes?: string[]
) => {
  const existing = risks.find((risk) => risk.id === id);
  if (existing) {
    if (orderCodes?.length) {
      existing.orderCodes = Array.from(
        new Set([...(existing.orderCodes ?? []), ...orderCodes])
      );
    }
    return;
  }

  risks.push({
    id,
    message,
    ...(orderCodes?.length ? { orderCodes } : {}),
  });
};

const evaluateLastMileManualAssignRisks = ({
  postOffice,
  postOfficeCode,
  courierStaffId,
  courierOptions,
  selectedOrderIds,
  ordersById,
}: {
  postOffice?: PostOffice;
  postOfficeCode?: string;
  courierStaffId: number;
  courierOptions: DispatchCourierOption[];
  selectedOrderIds: number[];
  ordersById: Map<number, FirstMileOrderDetail>;
}): ManualAssignRisk[] => {
  const risks: ManualAssignRisk[] = [];
  const normalizedPostOfficeCode = postOfficeCode?.trim().toLowerCase();

  if (!postOffice) {
    pushRisk(risks, 'post-office-missing', 'Không tìm thấy bưu cục đã chọn.');
  } else {
    if (postOffice.status !== 'ACTIVE') {
      pushRisk(
        risks,
        'post-office-inactive',
        `Bưu cục ${postOffice.code} đang ở trạng thái ${postOffice.status}.`
      );
    }

    if (postOffice.latitude == null || postOffice.longitude == null) {
      pushRisk(
        risks,
        'post-office-no-geocode',
        `Bưu cục ${postOffice.code} chưa có tọa độ để lập tuyến giao.`
      );
    }
  }

  if (!courierOptions.some((courier) => courier.id === courierStaffId)) {
    pushRisk(
      risks,
      'courier-inactive',
      'Nhân viên giao nhận đã chọn không còn hoạt động tại bưu cục.'
    );
  }

  const missingOrders: string[] = [];
  const invalidStatusOrders: string[] = [];
  const wrongPostOfficeOrders: string[] = [];
  const missingLocationOrders: string[] = [];

  for (const orderId of selectedOrderIds) {
    const order = ordersById.get(orderId);
    if (!order) {
      missingOrders.push(`#${orderId}`);
      continue;
    }

    const label = getOrderLabel(order);
    if (!CANDIDATE_ORDER_STATUSES.includes(order.status)) {
      invalidStatusOrders.push(
        `${label} (${formatOrderStatusLabel(order.status)})`
      );
    }

    const destinationPostOfficeCode = order.destinationPostOfficeCode
      ?.trim()
      .toLowerCase();
    if (
      normalizedPostOfficeCode &&
      destinationPostOfficeCode &&
      destinationPostOfficeCode !== normalizedPostOfficeCode
    ) {
      wrongPostOfficeOrders.push(
        `${label} (bưu cục đích: ${order.destinationPostOfficeCode})`
      );
    }

    if (!hasValidReceiverLocation(order)) {
      missingLocationOrders.push(label);
    }
  }

  if (missingOrders.length > 0) {
    pushRisk(
      risks,
      'orders-not-loaded',
      `Không tải được ${missingOrders.length} đơn hàng từ danh sách hiện tại.`,
      missingOrders
    );
  }

  if (invalidStatusOrders.length > 0) {
    pushRisk(
      risks,
      'orders-invalid-status',
      'Một số đơn hàng không ở trạng thái sẵn sàng giao hoặc giao thất bại.',
      invalidStatusOrders
    );
  }

  if (wrongPostOfficeOrders.length > 0) {
    pushRisk(
      risks,
      'orders-wrong-post-office',
      'Một số đơn hàng không thuộc bưu cục đích đã chọn.',
      wrongPostOfficeOrders
    );
  }

  if (missingLocationOrders.length > 0) {
    pushRisk(
      risks,
      'orders-missing-location',
      'Một số đơn hàng chưa có tọa độ người nhận hợp lệ.',
      missingLocationOrders
    );
  }

  return risks;
};

const extractSuggestedCouriers = (
  assignmentResult: DeliveryAssignmentResponse | null
): SuggestedCourier[] => {
  const suggestions = new Map<number, SuggestedCourier>();

  for (const trip of assignmentResult?.trips ?? []) {
    if (!trip.courierStaffId) {
      continue;
    }

    const label =
      trip.courierCode || trip.courierName
        ? `${trip.courierCode || trip.courierStaffId}${
            trip.courierName ? ` - ${trip.courierName}` : ''
          }`
        : String(trip.courierStaffId);

    suggestions.set(trip.courierStaffId, {
      id: trip.courierStaffId,
      label,
    });
  }

  return Array.from(suggestions.values()).sort((a, b) => a.id - b.id);
};

export const LastMileDispatcherControlPage: React.FC = () => {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );

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
  const [selectedOrderIds, setSelectedOrderIds] = React.useState<number[]>([]);
  const [orderLimitInput, setOrderLimitInput] = React.useState('');
  const [optimizationGoal, setOptimizationGoal] =
    React.useState<DispatchOptimizationGoalOption>('BALANCED');
  const [optimizationResult, setOptimizationResult] =
    React.useState<PickupOptimizationResponse | null>(null);
  const [assignmentResult, setAssignmentResult] =
    React.useState<DeliveryAssignmentResponse | null>(null);
  const [assignmentResultSource, setAssignmentResultSource] =
    React.useState<DispatcherTabValue | null>(null);
  const [activeDispatcherTab, setActiveDispatcherTab] =
    React.useState<DispatcherTabValue>('automatic');
  const [activeAction, setActiveAction] = React.useState<
    'preview' | 'auto' | 'manual' | null
  >(null);
  const [isManualConfirmOpen, setIsManualConfirmOpen] = React.useState(false);
  const [manualAssignRisks, setManualAssignRisks] = React.useState<
    ManualAssignRisk[]
  >([]);
  const [pendingManualPayload, setPendingManualPayload] =
    React.useState<ManualAssignDeliveryOrdersRequest | null>(null);

  const { data: provincesData, isLoading: isLoadingProvinces } =
    useGetProvincesQuery(
      { page: 0, size: POST_OFFICE_PAGE_SIZE },
      { skip: !canDispatch }
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
      { skip: !canDispatch || !manualProvinceCode }
    );

  const manualWardOptions = React.useMemo<Ward[]>(
    () => manualWardsData?.items ?? [],
    [manualWardsData]
  );

  const { data: setupPostOfficesData, isLoading: isLoadingSetupPostOffices } =
    useGetPostOfficesQuery(
      { page: 0, size: POST_OFFICE_PAGE_SIZE },
      { skip: !canDispatch }
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
      { skip: !canDispatch || !manualProvinceCode }
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

  const setupSelectedPostOffice = React.useMemo(() => {
    return setupPostOfficeOptions.find(
      (postOffice) => String(postOffice.id) === setupSelectedPostOfficeId
    );
  }, [setupPostOfficeOptions, setupSelectedPostOfficeId]);

  const manualSelectedPostOffice = React.useMemo(() => {
    return manualPostOfficeOptions.find(
      (postOffice) => String(postOffice.id) === manualSelectedPostOfficeId
    );
  }, [manualPostOfficeOptions, manualSelectedPostOfficeId]);

  const manualSelectedPostOfficeNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(manualSelectedPostOfficeId),
    [manualSelectedPostOfficeId]
  );

  const setupSelectedPostOfficeCode = setupSelectedPostOffice?.code;
  const manualSelectedPostOfficeCode = manualSelectedPostOffice?.code;

  const { data: setupCouriersData, isLoading: isLoadingSetupCouriers } =
    useGetActiveCouriersByPostOfficeQuery(
      setupSelectedPostOfficeNumericId ?? 0,
      { skip: !canDispatch || !setupSelectedPostOfficeNumericId }
    );

  const { data: manualCouriersData, isLoading: isLoadingManualCouriers } =
    useGetActiveCouriersByPostOfficeQuery(
      manualSelectedPostOfficeNumericId ?? 0,
      { skip: !canDispatch || !manualSelectedPostOfficeNumericId }
    );

  const mapCourierOptions = React.useCallback(
    (couriers: PostOfficeStaff[] = []): DispatchCourierOption[] => {
      return couriers
        .filter(
          (
            courier: PostOfficeStaff
          ): courier is PostOfficeStaff & { id: number } => {
            return Number.isInteger(courier.id) && courier.id > 0;
          }
        )
        .map((courier) => ({
          id: courier.id,
          code: courier.code?.trim() || `COURIER-${courier.id}`,
          fullName: courier.fullName?.trim() || 'Chưa rõ nhân viên',
        }))
        .sort((a, b) => {
          const nameCompare = a.fullName.localeCompare(b.fullName, 'vi-VN', {
            sensitivity: 'base',
          });

          return nameCompare !== 0 ? nameCompare : a.id - b.id;
        });
    },
    []
  );

  const setupCourierOptions = React.useMemo(
    () => mapCourierOptions(setupCouriersData ?? []),
    [mapCourierOptions, setupCouriersData]
  );

  const manualCourierOptions = React.useMemo(
    () => mapCourierOptions(manualCouriersData ?? []),
    [manualCouriersData, mapCourierOptions]
  );

  const setupCourierIdSet = React.useMemo(
    () => new Set(setupCourierOptions.map((courier) => courier.id)),
    [setupCourierOptions]
  );

  const manualCourierIdSet = React.useMemo(
    () => new Set(manualCourierOptions.map((courier) => courier.id)),
    [manualCourierOptions]
  );

  const {
    data: setupOrdersData,
    isLoading: isLoadingSetupOrders,
    refetch: refetchSetupOrders,
  } = useGetOrdersQuery(
    {
      page: 0,
      size: MANUAL_ORDER_PAGE_SIZE,
      statuses: CANDIDATE_ORDER_STATUSES,
      ...(setupSelectedPostOfficeCode
        ? { destinationPostOfficeCode: setupSelectedPostOfficeCode }
        : {}),
    },
    { skip: !canDispatch || !setupSelectedPostOfficeCode }
  );

  const setupCandidateOrders = React.useMemo(() => {
    return (setupOrdersData?.items ?? [])
      .filter((order) => isCandidateOrderStatus(order.status))
      .sort((a, b) => a.id - b.id);
  }, [setupOrdersData]);

  const {
    data: manualOrdersData,
    isLoading: isLoadingManualOrders,
    refetch: refetchManualOrders,
  } = useGetOrdersQuery(
    {
      page: 0,
      size: MANUAL_ORDER_PAGE_SIZE,
      statuses: CANDIDATE_ORDER_STATUSES,
      ...(manualSelectedPostOfficeCode
        ? { destinationPostOfficeCode: manualSelectedPostOfficeCode }
        : {}),
    },
    { skip: !canDispatch || !manualSelectedPostOfficeId }
  );

  const manualCandidateOrders = React.useMemo(() => {
    return (manualOrdersData?.items ?? [])
      .filter((order) => isCandidateOrderStatus(order.status))
      .sort((a, b) => a.id - b.id);
  }, [manualOrdersData]);

  const manualOrdersById = React.useMemo(() => {
    const orderMap = new Map<number, FirstMileOrderDetail>();
    for (const order of manualOrdersData?.items ?? []) {
      orderMap.set(order.id, order);
    }
    return orderMap;
  }, [manualOrdersData]);

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
    setSelectedOrderIds([]);
    setSelectedManualCourierId('');
  }, [manualSelectedPostOfficeId]);

  React.useEffect(() => {
    setManualWardCode('');
    setManualSelectedPostOfficeId('');
  }, [manualProvinceCode]);

  React.useEffect(() => {
    setManualSelectedPostOfficeId('');
  }, [manualWardCode]);

  React.useEffect(() => {
    setSelectedManualCourierId((prev) => {
      if (!prev) {
        return prev;
      }

      const parsedValue = Number(prev);
      return Number.isInteger(parsedValue) &&
        manualCourierIdSet.has(parsedValue)
        ? prev
        : '';
    });
  }, [manualCourierIdSet]);

  const [autoAssignDeliveryPlan, { isLoading: isAutoAssigning }] =
    useAutoAssignDeliveryPlanMutation();
  const [manualAssignDeliveryOrders, { isLoading: isManualAssigning }] =
    useManualAssignDeliveryOrdersMutation();

  const parseOptionalOrderLimit = React.useCallback(() => {
    const parsedOrderLimit = parseOptionalPositiveInteger(orderLimitInput);

    if (orderLimitInput.trim() && parsedOrderLimit === undefined) {
      notification.error('Giới hạn đơn hàng phải là số nguyên dương.');
      return null;
    }

    return parsedOrderLimit;
  }, [notification, orderLimitInput]);

  const buildBusinessDispatchSettings = React.useCallback(() => {
    const settings: BusinessDispatchSettings = {
      vehicle: DEFAULT_ROUTING_VEHICLE,
      optimization_goal: optimizationGoal,
      allow_lateness: true,
    };

    return settings;
  }, [optimizationGoal]);

  const buildSetupDispatchContext = React.useCallback(() => {
    if (!canDispatch) {
      notification.error(
        'Chỉ TMS_ADMIN hoặc TMS_POSTOFFICER_MANAGER được điều phối giao hàng.'
      );
      return null;
    }

    const postOfficeId = parseOptionalPositiveInteger(
      setupSelectedPostOfficeId
    );
    if (!postOfficeId) {
      notification.error('Vui lòng chọn bưu cục để thiết lập điều phối.');
      return null;
    }

    if (!setupTripDate) {
      notification.error('Vui lòng chọn ngày chuyến cho điều phối.');
      return null;
    }

    return {
      postOfficeId,
      shiftWindow: buildShiftWindow(setupTripDate, setupShift),
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
        'Chỉ TMS_ADMIN hoặc TMS_POSTOFFICER_MANAGER được điều phối giao hàng.'
      );
      return null;
    }

    const postOfficeId = parseOptionalPositiveInteger(
      manualSelectedPostOfficeId
    );
    if (!postOfficeId) {
      notification.error('Vui lòng chọn bưu cục để điều phối thủ công.');
      return null;
    }

    if (!manualTripDate) {
      notification.error('Vui lòng chọn ngày chuyến cho điều phối thủ công.');
      return null;
    }

    return {
      postOfficeId,
      shiftWindow: buildShiftWindow(manualTripDate, manualShift),
    };
  }, [
    canDispatch,
    manualSelectedPostOfficeId,
    manualShift,
    manualTripDate,
    notification,
  ]);

  const handleToggleAutoCourier = (courierId: number, checked: boolean) => {
    setSelectedAutoCourierIds((prev) => {
      if (checked) {
        return prev.includes(courierId) ? prev : [...prev, courierId];
      }

      return prev.filter((id) => id !== courierId);
    });
  };

  const handleAutoAssign = async () => {
    const context = buildSetupDispatchContext();
    if (!context) {
      return;
    }

    const parsedOrderLimit = parseOptionalOrderLimit();
    if (parsedOrderLimit === null) {
      return;
    }

    const businessSettings = buildBusinessDispatchSettings();
    const payload: AutoAssignDeliveryPlanRequest = {
      post_office_id: context.postOfficeId,
      shift: setupShift,
      trip_date: setupTripDate,
      planning_start_time: context.shiftWindow.planningStartTime,
      planning_end_time: context.shiftWindow.planningEndTime,
      candidate_statuses: CANDIDATE_ORDER_STATUSES,
      allow_lateness: true,
      ...(selectedAutoCourierIds.length
        ? { courier_ids: selectedAutoCourierIds }
        : {}),
      ...(parsedOrderLimit ? { order_limit: parsedOrderLimit } : {}),
      ...businessSettings,
    };

    setActiveAction('auto');
    try {
      const result = await autoAssignDeliveryPlan(payload).unwrap();
      setAssignmentResult(result);
      setAssignmentResultSource('automatic');
      setSelectedOrderIds([]);

      notification.success('Đã hoàn tất điều phối giao hàng tự động.', {
        description: `Đã phân công ${result.assignedOrders ?? 0}/${
          result.totalRequestedOrders ?? 0
        } đơn hàng.`,
      });

      if (setupSelectedPostOfficeCode) {
        void refetchSetupOrders();
      }

      if (manualSelectedPostOfficeId) {
        void refetchManualOrders();
      }
    } catch (error) {
      notification.error('Không thể tự động phân công đơn giao hàng.', {
        description: getErrorMessage(error),
      });
    } finally {
      setActiveAction(null);
    }
  };

  const executeManualAssign = React.useCallback(
    async (
      payload: ManualAssignDeliveryOrdersRequest,
      forceAssign: boolean
    ) => {
      setActiveAction('manual');
      try {
        const result = await manualAssignDeliveryOrders({
          ...payload,
          force_assign: forceAssign,
        }).unwrap();

        setAssignmentResult(result);
        setAssignmentResultSource('manual');
        setSelectedOrderIds([]);
        setPendingManualPayload(null);
        setManualAssignRisks([]);
        setIsManualConfirmOpen(false);

        const assignedCount = result.assignedOrders ?? 0;
        const requestedCount = result.totalRequestedOrders ?? 0;

        if (assignedCount < requestedCount) {
          const unassignedDetails = result.unassignedOrderDetails ?? [];

          if (!forceAssign && unassignedDetails.length > 0) {
            setPendingManualPayload(payload);
            setManualAssignRisks(
              unassignedDetails.map((item, index) => ({
                id: `optimizer-unassigned-${item.orderId ?? index}`,
                message: `Bộ tối ưu chưa thể xếp đơn này (${
                  item.reason ?? 'NO_FEASIBLE_INSERTION'
                }).`,
                orderCodes: [
                  item.orderCode ??
                    item.customerOrderCode ??
                    (item.orderId ? `#${item.orderId}` : 'N/A'),
                ],
              }))
            );
            setIsManualConfirmOpen(true);
            notification.warning('Cần xác nhận phân công bắt buộc.', {
              description: `Chỉ ${assignedCount}/${requestedCount} đơn hàng phù hợp với ràng buộc tuyến tối ưu.`,
            });
            return;
          }

          notification.warning('Phân công thủ công chưa hoàn tất.', {
            description: `Đã phân công ${assignedCount}/${requestedCount} đơn hàng.`,
          });
        } else {
          notification.success('Đã hoàn tất phân công giao hàng thủ công.', {
            description: `Đã phân công ${assignedCount}/${requestedCount} đơn hàng.`,
          });
        }

        void refetchManualOrders();
      } catch (error) {
        notification.error('Phân công giao hàng thủ công thất bại.', {
          description: getErrorMessage(error),
        });
      } finally {
        setActiveAction(null);
      }
    },
    [manualAssignDeliveryOrders, notification, refetchManualOrders]
  );

  const handleManualAssign = async () => {
    const context = buildManualDispatchContext();
    if (!context) {
      return;
    }

    if (selectedOrderIds.length === 0) {
      notification.error(
        'Vui lòng chọn ít nhất một đơn hàng để phân công thủ công.'
      );
      return;
    }

    const courierStaffId = parseOptionalPositiveInteger(
      selectedManualCourierId
    );
    if (!courierStaffId) {
      notification.error(
        'Vui lòng chọn nhân viên giao nhận để phân công thủ công.'
      );
      return;
    }

    const businessSettings = buildBusinessDispatchSettings();
    const payload: ManualAssignDeliveryOrdersRequest = {
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
    };

    const risks = evaluateLastMileManualAssignRisks({
      postOffice: manualSelectedPostOffice,
      postOfficeCode: manualSelectedPostOfficeCode,
      courierStaffId,
      courierOptions: manualCourierOptions,
      selectedOrderIds,
      ordersById: manualOrdersById,
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

  const suggestedCouriers = React.useMemo(
    () => extractSuggestedCouriers(assignmentResult),
    [assignmentResult]
  );

  const businessValues: DispatchSetupBusinessValues = {
    optimizationGoal,
  };

  const businessHandlers: DispatchSetupBusinessHandlers = {
    onOptimizationGoalChange: setOptimizationGoal,
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>
          Điều phối giao hàng chặng cuối
        </h1>
        <p className='text-muted-foreground'>
          Điều phối đơn sẵn sàng giao theo phạm vi bưu cục đích và nhân viên
          giao nhận.
        </p>
      </div>

      {!canDispatch ? (
        <DispatchNoAccessCard message='Bạn không có quyền truy cập thao tác điều phối giao hàng.' />
      ) : (
        <>
          <Tabs
            value={activeDispatcherTab}
            onValueChange={(value) =>
              setActiveDispatcherTab(value as DispatcherTabValue)
            }
            className='space-y-4'
          >
            <TabsList className='grid w-full grid-cols-2 lg:w-auto'>
              <TabsTrigger value='automatic'>Điều phối tự động</TabsTrigger>
              <TabsTrigger value='manual'>Điều phối thủ công</TabsTrigger>
            </TabsList>

            <TabsContent value='automatic' className='mt-0 space-y-4'>
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
                onClearAutoCourierSelection={() =>
                  setSelectedAutoCourierIds([])
                }
                businessValues={businessValues}
                businessHandlers={businessHandlers}
                onPreviewPlan={() => undefined}
                onAutoAssign={() => void handleAutoAssign()}
                isOptimizing={false}
                isAutoAssigning={isAutoAssigning}
                activeAction={activeAction}
                isTmsAdmin={isTmsAdmin}
                hidePreviewButton
              />

              <CandidateOrdersPanel
                title='Đơn chờ điều phối giao tự động'
                orders={setupCandidateOrders}
                loading={isLoadingSetupOrders}
                emptyText='Không có đơn sẵn sàng giao cho bưu cục này.'
              />

              {assignmentResultSource === 'automatic' && assignmentResult ? (
                <AssignmentResultCard
                  assignmentResult={assignmentResult}
                  formatDateTime={formatDateTime}
                  formatNumber={formatNumber}
                />
              ) : null}
            </TabsContent>

            <TabsContent value='manual' className='mt-0 space-y-4'>
              <ManualDispatchCard
                provinceOptions={provinceOptions}
                wardOptions={manualWardOptions}
                selectedProvinceCode={manualProvinceCode}
                onProvinceChange={setManualProvinceCode}
                selectedWardCode={manualWardCode || 'ALL'}
                onWardChange={(value) =>
                  setManualWardCode(value === 'ALL' ? '' : value)
                }
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
                      'Nhân viên được gợi ý không còn hoạt động tại bưu cục đã chọn.'
                    );
                    return;
                  }

                  setSelectedManualCourierId(String(courierId));
                }}
                isLoadingOrders={isLoadingManualOrders}
                candidateOrders={manualCandidateOrders}
                selectedOrderIds={selectedOrderIds}
                onOrderSelectionChange={setSelectedOrderIds}
                onClearSelectedOrders={() => setSelectedOrderIds([])}
                onManualAssign={() => void handleManualAssign()}
                isManualAssigning={isManualAssigning}
                activeAction={activeAction}
              />

              {assignmentResultSource === 'manual' && assignmentResult ? (
                <AssignmentResultCard
                  assignmentResult={assignmentResult}
                  formatDateTime={formatDateTime}
                  formatNumber={formatNumber}
                />
              ) : null}
            </TabsContent>
          </Tabs>

          <ConfirmDialog
            open={isManualConfirmOpen}
            onOpenChange={setIsManualConfirmOpen}
            title='Kiểm tra rủi ro trước khi phân công'
            description={
              <div className='space-y-3 text-left'>
                <p>
                  Hệ thống phát hiện {manualAssignRisks.length} nhóm rủi ro. Nếu
                  xác nhận, các đơn đã chọn vẫn sẽ được gán trực tiếp cho nhân
                  viên giao nhận và có thể bỏ qua ràng buộc tuyến tối ưu.
                </p>
                <div className='rounded-md border bg-muted/40 p-3 text-xs text-muted-foreground'>
                  <p>
                    Bưu cục: {manualSelectedPostOffice?.code ?? '--'} -{' '}
                    {manualSelectedPostOffice?.name ?? '--'}
                  </p>
                  <p>Nhân viên giao nhận: {pendingCourierLabel}</p>
                  <p>
                    Ca: {getManualShiftLabel(manualShift)} | Ngày:{' '}
                    {manualTripDate}
                  </p>
                  <p>Đơn đã chọn: {selectedOrderIds.length}</p>
                </div>
                <ul className='list-disc space-y-2 pl-5 text-sm'>
                  {manualAssignRiskLines.map((warning, index) => (
                    <li key={`${warning}-${index}`}>{warning}</li>
                  ))}
                </ul>
              </div>
            }
            confirmText='Vẫn phân công'
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
