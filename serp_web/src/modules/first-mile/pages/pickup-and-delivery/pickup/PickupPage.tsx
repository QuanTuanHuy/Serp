/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile pickup tracking page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppDispatch, useAppSelector } from '@/lib/store';
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
  Switch,
} from '@/shared/components';
import { TmsCombobox } from '@/modules/first-mile/components';
import { useNotification } from '@/shared/hooks';
import {
  firstMileApi,
  useCompletePickupTripMutation,
  useConfirmPickupTripPostOfficeInboundMutation,
  useGetActiveCouriersByPostOfficeQuery,
  useGetPickupTrackingOverviewQuery,
  useGetPostOfficesQuery,
  useGetProvincesQuery,
  useReturnPickupTripToPostOfficeMutation,
  useGetWardsByProvinceCodeQuery,
  usePickupCheckinOrderMutation,
} from '../../../api';
import type {
  FirstMileOrderStatus,
  PickupTripLifecycleResponse,
  PickupTrackingOrder,
  PickupTrackingOverviewResponse,
  PickupTrackingTrip,
  PostOffice,
  PostOfficeStaff,
  Province,
  Ward,
} from '../../../types';
import { PickupCheckinDetailDialog } from './components/PickupCheckinDetailDialog';
import { PickupOrdersMap } from './components/PickupOrdersMap';
import { PickupPostOfficeInboundDialog } from './components/PickupPostOfficeInboundDialog';
import {
  isPickupCheckinDevFeatureAvailable,
  readDevCheckinModePreference,
  resolveValidCheckinCoordinates,
  writeDevCheckinModePreference,
} from './pickupCheckinDev';

type PickupPageAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_SCOPED'
  | 'COURIER_SELF'
  | 'NO_ACCESS';

const POST_OFFICE_PAGE_SIZE = 200;

const PICKUP_STATUS_LABELS: Record<string, string> = {
  PLANNED: 'Đã lập kế hoạch',
  IN_PROGRESS: 'Đang thực hiện',
  COMPLETED: 'Hoàn tất',
  CANCELLED: 'Đã hủy',
  ASSIGNED_TO_PICKUP: 'Đã phân công lấy',
  PICKING_UP: 'Đang lấy hàng',
  PICKED_UP: 'Đã lấy hàng',
  PENDING_ORIGIN_POST_OFFICE_INBOUND: 'Chờ nhập bưu cục gốc',
};

const formatPickupStatus = (status?: string): string =>
  status ? (PICKUP_STATUS_LABELS[status] ?? status) : '--';

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

  return parsedDate.toLocaleString('vi-VN');
};

const formatNumber = (value?: number): string => {
  if (value === undefined || value === null) {
    return '0';
  }

  return String(value);
};

const normalizeOrderCode = (value?: string): string =>
  value?.trim().toUpperCase() ?? '';

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

const canViewCheckinDetail = (order: PickupTrackingOrder): boolean =>
  Boolean(order.checkedIn && order.orderId);

const canCompleteTrip = (trip: PickupTrackingTrip): boolean =>
  trip.tripId !== undefined &&
  (trip.tripStatus === 'PLANNED' || trip.tripStatus === 'IN_PROGRESS');

const RETURNABLE_TO_POST_OFFICE_ORDER_STATUSES: FirstMileOrderStatus[] = [
  'PICKING_UP',
  'PICKED_UP',
];

const canReturnTripToPostOffice = (
  trip: PickupTrackingTrip,
  orders: PickupTrackingOrder[]
): boolean => {
  if (trip.tripId === undefined || trip.tripStatus !== 'COMPLETED') {
    return false;
  }

  if (trip.returnableToPostOfficeOrders !== undefined) {
    return trip.returnableToPostOfficeOrders > 0;
  }

  return orders.some(
    (order) =>
      order.tripId === trip.tripId &&
      order.orderStatus !== undefined &&
      RETURNABLE_TO_POST_OFFICE_ORDER_STATUSES.includes(order.orderStatus)
  );
};

const canConfirmPostOfficeInbound = (
  trip: PickupTrackingTrip,
  orders: PickupTrackingOrder[]
): boolean => {
  if (trip.tripId === undefined || trip.tripStatus !== 'COMPLETED') {
    return false;
  }

  if (trip.pendingPostOfficeInboundOrders !== undefined) {
    return trip.pendingPostOfficeInboundOrders > 0;
  }

  return orders.some(
    (order) =>
      order.tripId === trip.tripId &&
      order.orderStatus === 'PENDING_ORIGIN_POST_OFFICE_INBOUND'
  );
};

const applyReturnTripToPostOfficeResult = (
  overview: PickupTrackingOverviewResponse,
  trip: PickupTrackingTrip,
  result: PickupTripLifecycleResponse
) => {
  const tripId = result.tripId ?? trip.tripId;
  const trackingTrip = overview.trips?.find((item) => item.tripId === tripId);
  const returnedOrders =
    overview.orders?.filter(
      (order) =>
        order.tripId === tripId &&
        order.orderStatus !== undefined &&
        RETURNABLE_TO_POST_OFFICE_ORDER_STATUSES.includes(order.orderStatus)
    ) ?? [];
  const returnedOrderCount =
    result.pendingPostOfficeInboundOrders ??
    result.returnedToPostOfficeOrders ??
    returnedOrders.length;
  const pickingUpReturnedOrderCount = returnedOrders.filter(
    (order) => order.orderStatus === 'PICKING_UP'
  ).length;
  const pickedUpReturnedOrderCount = returnedOrders.filter(
    (order) => order.orderStatus === 'PICKED_UP'
  ).length;

  if (trackingTrip) {
    trackingTrip.tripStatus = result.tripStatus ?? trackingTrip.tripStatus;
    trackingTrip.totalOrders = result.totalOrders ?? trackingTrip.totalOrders;
    trackingTrip.checkedInOrders =
      result.checkedInOrders ?? trackingTrip.checkedInOrders;
    trackingTrip.pendingCheckinOrders =
      result.pendingCheckinOrders ?? trackingTrip.pendingCheckinOrders;
    trackingTrip.returnableToPostOfficeOrders = 0;
    trackingTrip.pendingPostOfficeInboundOrders = returnedOrderCount;
  }

  returnedOrders.forEach((order) => {
    order.orderStatus = 'PENDING_ORIGIN_POST_OFFICE_INBOUND';
  });

  if (overview.pendingPostOfficeInboundOrders !== undefined) {
    overview.pendingPostOfficeInboundOrders += returnedOrders.length;
  }
  if (overview.pickedUpOrders !== undefined) {
    overview.pickedUpOrders = Math.max(
      0,
      overview.pickedUpOrders - pickedUpReturnedOrderCount
    );
  }
  if (overview.pickingUpOrders !== undefined) {
    overview.pickingUpOrders = Math.max(
      0,
      overview.pickingUpOrders - pickingUpReturnedOrderCount
    );
  }
};

const applyConfirmPostOfficeInboundResult = (
  overview: PickupTrackingOverviewResponse,
  trip: PickupTrackingTrip,
  orderCodes: string[],
  result: PickupTripLifecycleResponse
) => {
  const tripId = result.tripId ?? trip.tripId;
  const confirmedOrderCodeSet = new Set(orderCodes.map(normalizeOrderCode));
  const confirmedOrders =
    overview.orders?.filter(
      (order) =>
        order.tripId === tripId &&
        order.orderStatus === 'PENDING_ORIGIN_POST_OFFICE_INBOUND' &&
        confirmedOrderCodeSet.has(normalizeOrderCode(order.orderCode))
    ) ?? [];
  const confirmedOrderCount = confirmedOrders.length;
  const trackingTrip = overview.trips?.find((item) => item.tripId === tripId);

  if (trackingTrip) {
    trackingTrip.tripStatus = result.tripStatus ?? trackingTrip.tripStatus;
    trackingTrip.totalOrders = result.totalOrders ?? trackingTrip.totalOrders;
    trackingTrip.checkedInOrders =
      result.checkedInOrders ?? trackingTrip.checkedInOrders;
    trackingTrip.pendingCheckinOrders =
      result.pendingCheckinOrders ?? trackingTrip.pendingCheckinOrders;
    trackingTrip.returnableToPostOfficeOrders = 0;
    trackingTrip.pendingPostOfficeInboundOrders = Math.max(
      0,
      (trackingTrip.pendingPostOfficeInboundOrders ?? 0) - confirmedOrderCount
    );
  }

  confirmedOrders.forEach((order) => {
    order.orderStatus = 'AT_ORIGIN_POST_OFFICE';
  });

  if (overview.pendingPostOfficeInboundOrders !== undefined) {
    overview.pendingPostOfficeInboundOrders = Math.max(
      0,
      overview.pendingPostOfficeInboundOrders - confirmedOrderCount
    );
  }
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
  const dispatch = useAppDispatch();
  const notification = useNotification();
  const profile = useAppSelector((state) => state.account.user.profile);
  const roles = profile?.roles ?? [];

  const accessScope = React.useMemo(
    () => resolvePickupPageAccessScope(roles),
    [roles]
  );

  const canAccess = accessScope !== 'NO_ACCESS';
  const isCourierScope = accessScope === 'COURIER_SELF';
  const canManagePostOfficeInbound =
    accessScope === 'ADMIN_ALL' || accessScope === 'MANAGER_SCOPED';

  const [tripDate, setTripDate] = React.useState(getTodayDateInputValue);
  const [selectedProvinceCode, setSelectedProvinceCode] = React.useState('');
  const [selectedWardCode, setSelectedWardCode] = React.useState('');
  const [selectedPostOfficeId, setSelectedPostOfficeId] = React.useState('');
  const [selectedCourierStaffId, setSelectedCourierStaffId] =
    React.useState('all');
  const [selectedOrderId, setSelectedOrderId] = React.useState<
    number | undefined
  >(undefined);

  const [isCheckinDialogOpen, setIsCheckinDialogOpen] = React.useState(false);
  const [isCheckinDetailDialogOpen, setIsCheckinDetailDialogOpen] =
    React.useState(false);
  const [checkinDetailOrder, setCheckinDetailOrder] =
    React.useState<PickupTrackingOrder | null>(null);
  const [checkinOrder, setCheckinOrder] =
    React.useState<PickupTrackingOrder | null>(null);
  const [checkinPhoto, setCheckinPhoto] = React.useState<File | null>(null);
  const [checkinLatitude, setCheckinLatitude] = React.useState('');
  const [checkinLongitude, setCheckinLongitude] = React.useState('');
  const [isResolvingLocation, setIsResolvingLocation] = React.useState(false);
  const isDevCheckinFeatureAvailable = isPickupCheckinDevFeatureAvailable();
  const [devCheckinMode, setDevCheckinMode] = React.useState(false);

  React.useEffect(() => {
    if (isDevCheckinFeatureAvailable) {
      setDevCheckinMode(readDevCheckinModePreference());
    }
  }, [isDevCheckinFeatureAvailable]);

  const shouldLoadLocationFilters = canAccess && !isCourierScope;

  const { data: provincesData, isLoading: isLoadingProvinces } =
    useGetProvincesQuery(
      {
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
      },
      {
        skip: !shouldLoadLocationFilters,
      }
    );

  const provinceOptions = React.useMemo<Province[]>(
    () => provincesData?.items ?? [],
    [provincesData]
  );
  const provinceComboboxOptions = React.useMemo(
    () =>
      provinceOptions.flatMap((province) =>
        province.provinceCode
          ? [
              {
                value: province.provinceCode,
                label: `${province.name} (${province.provinceCode})`,
              },
            ]
          : []
      ),
    [provinceOptions]
  );

  const { data: wardsData, isLoading: isLoadingWards } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: selectedProvinceCode,
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
      },
      {
        skip: !shouldLoadLocationFilters || !selectedProvinceCode,
      }
    );

  const wardOptions = React.useMemo<Ward[]>(
    () => wardsData?.items ?? [],
    [wardsData]
  );
  const wardComboboxOptions = React.useMemo(
    () => [
      { value: 'ALL', label: 'Tất cả phường/xã trong tỉnh' },
      ...wardOptions.flatMap((ward) =>
        ward.wardCode
          ? [
              {
                value: ward.wardCode,
                label: `${ward.name} (${ward.wardCode})`,
              },
            ]
          : []
      ),
    ],
    [wardOptions]
  );

  const { data: postOfficesData, isLoading: isLoadingPostOffices } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: POST_OFFICE_PAGE_SIZE,
        provinceCode: selectedProvinceCode || undefined,
        ...(selectedWardCode ? { wardCode: selectedWardCode } : {}),
      },
      {
        skip: !shouldLoadLocationFilters || !selectedProvinceCode,
      }
    );

  const postOfficeOptions = React.useMemo<PostOffice[]>(
    () => postOfficesData?.items ?? [],
    [postOfficesData]
  );
  const postOfficeComboboxOptions = React.useMemo(
    () =>
      postOfficeOptions.map((postOffice) => ({
        value: String(postOffice.id),
        label: `${postOffice.code} - ${postOffice.name}`,
      })),
    [postOfficeOptions]
  );

  React.useEffect(() => {
    if (!provinceOptions.length) {
      return;
    }

    const hasSelectedProvince = provinceOptions.some(
      (province) => province.provinceCode === selectedProvinceCode
    );

    if (!hasSelectedProvince) {
      setSelectedProvinceCode(provinceOptions[0].provinceCode);
    }
  }, [provinceOptions, selectedProvinceCode]);

  React.useEffect(() => {
    setSelectedWardCode('');
    setSelectedPostOfficeId('');
    setSelectedCourierStaffId('all');
  }, [selectedProvinceCode]);

  React.useEffect(() => {
    setSelectedPostOfficeId('');
    setSelectedCourierStaffId('all');
  }, [selectedWardCode]);

  React.useEffect(() => {
    if (isCourierScope || !selectedProvinceCode || !postOfficeOptions.length) {
      return;
    }

    const hasSelectedPostOffice = postOfficeOptions.some(
      (postOffice) => String(postOffice.id) === selectedPostOfficeId
    );

    if (!hasSelectedPostOffice) {
      setSelectedPostOfficeId(String(postOfficeOptions[0].id));
    }
  }, [
    isCourierScope,
    postOfficeOptions,
    selectedPostOfficeId,
    selectedProvinceCode,
  ]);

  const handleProvinceChange = (value: string) => {
    setSelectedProvinceCode(value);
  };

  const handleWardChange = (value: string) => {
    setSelectedWardCode(value === 'ALL' ? '' : value);
  };

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

    notification.error(
      'Không thể tải nhân viên giao nhận của bưu cục đã chọn.',
      {
        description: getErrorMessage(couriersError),
      }
    );
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
        fullName: courier.fullName?.trim() || 'Nhân viên giao nhận chưa rõ',
      }));
  }, [couriersData]);
  const courierComboboxOptions = React.useMemo(
    () => [
      { value: 'all', label: 'Tất cả nhân viên giao nhận' },
      ...courierOptions.map((courier) => ({
        value: String(courier.id),
        label: `${courier.code} - ${courier.fullName}`,
      })),
    ],
    [courierOptions]
  );

  React.useEffect(() => {
    setSelectedCourierStaffId('all');
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

    notification.error('Không thể tải dữ liệu theo dõi lấy hàng.', {
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
  const [completePickupTrip, { isLoading: isCompletingTrip }] =
    useCompletePickupTripMutation();
  const [returnPickupTripToPostOffice, { isLoading: isReturningTrip }] =
    useReturnPickupTripToPostOfficeMutation();
  const [
    confirmPostOfficeInbound,
    { isLoading: isConfirmingPostOfficeInbound },
  ] = useConfirmPickupTripPostOfficeInboundMutation();
  const [inboundTrip, setInboundTrip] =
    React.useState<PickupTrackingTrip | null>(null);

  const applyValidCheckinCoordinates = React.useCallback(
    (order: PickupTrackingOrder) => {
      const coordinates = resolveValidCheckinCoordinates(order);
      if (!coordinates) {
        notification.error('Đơn hàng chưa có vị trí lấy hàng.', {
          description:
            'Vui lòng đảm bảo đơn có tọa độ người gửi trước khi check-in.',
        });
        return false;
      }

      setCheckinLatitude(coordinates.latitude.toFixed(6));
      setCheckinLongitude(coordinates.longitude.toFixed(6));
      return true;
    },
    [notification]
  );

  const handleDevCheckinModeChange = (enabled: boolean) => {
    setDevCheckinMode(enabled);
    writeDevCheckinModePreference(enabled);

    if (enabled && checkinOrder) {
      applyValidCheckinCoordinates(checkinOrder);
    }
  };

  const handleOpenCheckinDetailDialog = (order: PickupTrackingOrder) => {
    setCheckinDetailOrder(order);
    setIsCheckinDetailDialogOpen(true);
  };

  const handleOpenCheckinDialog = (order: PickupTrackingOrder) => {
    setCheckinOrder(order);
    setCheckinPhoto(null);
    setCheckinLatitude('');
    setCheckinLongitude('');
    setIsCheckinDialogOpen(true);

    if (devCheckinMode) {
      applyValidCheckinCoordinates(order);
    }
  };

  const handleResolveCheckinLocation = async () => {
    if (devCheckinMode) {
      if (!checkinOrder) {
        notification.error('Vui lòng chọn đơn để check-in.');
        return;
      }

      applyValidCheckinCoordinates(checkinOrder);
      return;
    }

    await handleResolveCurrentLocation();
  };

  const handleResolveCurrentLocation = async () => {
    if (typeof window === 'undefined' || !('geolocation' in navigator)) {
      notification.error('Trình duyệt không hỗ trợ định vị.');
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
      notification.error('Không thể lấy vị trí hiện tại.', {
        description: getErrorMessage(error),
      });
    } finally {
      setIsResolvingLocation(false);
    }
  };

  const handleSubmitCheckin = async () => {
    if (!checkinOrder) {
      notification.error('Vui lòng chọn đơn để check-in.');
      return;
    }

    if (!checkinPhoto) {
      notification.error('Vui lòng chọn ảnh trước khi check-in.');
      return;
    }

    const latitude = Number(checkinLatitude);
    const longitude = Number(checkinLongitude);

    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      notification.error('Vui lòng cung cấp vị trí check-in hợp lệ.');
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

      notification.success('Đã check-in lấy hàng thành công.');
      setIsCheckinDialogOpen(false);
      setCheckinOrder(null);
      setCheckinPhoto(null);
      setCheckinLatitude('');
      setCheckinLongitude('');
      void refetchOverview();
    } catch (error) {
      notification.error('Check-in lấy hàng thất bại.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleCompleteTrip = async (trip: PickupTrackingTrip) => {
    if (!trip.tripId) {
      notification.error('Thiếu mã chuyến.');
      return;
    }

    try {
      await completePickupTrip(trip.tripId).unwrap();
      notification.success(
        `Đã hoàn tất chuyến ${trip.tripCode || `#${trip.tripId}`}.`
      );
      void refetchOverview();
    } catch (error) {
      notification.error('Không thể hoàn tất chuyến.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleReturnTripToPostOffice = async (trip: PickupTrackingTrip) => {
    if (!trip.tripId) {
      notification.error('Thiếu mã chuyến.');
      return;
    }

    try {
      const result = await returnPickupTripToPostOffice(trip.tripId).unwrap();
      dispatch(
        firstMileApi.util.updateQueryData(
          'getPickupTrackingOverview',
          overviewQueryArgs,
          (draft) => {
            applyReturnTripToPostOfficeResult(draft, trip, result);
          }
        )
      );
      notification.success(
        `Đã chuyển chuyến ${trip.tripCode || `#${trip.tripId}`} về bưu cục. Đơn đang chờ quét nhập bưu cục.`
      );
      void refetchOverview();
    } catch (error) {
      notification.error('Không thể chuyển chuyến về bưu cục.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleOpenPostOfficeInbound = (trip: PickupTrackingTrip) => {
    setInboundTrip(trip);
  };

  const handleConfirmPostOfficeInbound = async (orderCodes: string[]) => {
    if (!inboundTrip?.tripId) {
      return;
    }

    if (orderCodes.length === 0) {
      notification.error(
        'Vui lòng quét ít nhất một đơn trước khi xác nhận nhập bưu cục.'
      );
      return;
    }

    try {
      const result = await confirmPostOfficeInbound({
        tripId: inboundTrip.tripId,
        body: { orderCodes },
      }).unwrap();
      dispatch(
        firstMileApi.util.updateQueryData(
          'getPickupTrackingOverview',
          overviewQueryArgs,
          (draft) => {
            applyConfirmPostOfficeInboundResult(
              draft,
              inboundTrip,
              orderCodes,
              result
            );
          }
        )
      );
      notification.success('Đã xác nhận nhập bưu cục thành công.');
      setInboundTrip(null);
      void refetchOverview();
    } catch (error) {
      notification.error('Không thể xác nhận nhập bưu cục.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-4'>
      <div className='flex flex-col gap-1'>
        <h1 className='text-2xl font-bold tracking-tight'>Theo dõi lấy hàng</h1>
      </div>

      {!canAccess ? (
        <Card>
          <CardHeader>
            <CardTitle>Không có quyền truy cập</CardTitle>
            <CardDescription>
              Tài khoản của bạn không có quyền truy cập theo dõi lấy hàng.
            </CardDescription>
          </CardHeader>
        </Card>
      ) : (
        <>
          <Card className='gap-3 py-5'>
            <CardHeader className='px-5 py-0'>
              <CardTitle className='text-base'>Bộ lọc theo dõi</CardTitle>
            </CardHeader>
            <CardContent className='grid gap-3 px-5 py-0 md:grid-cols-2 xl:grid-cols-5'>
              <div className='space-y-2'>
                <Label htmlFor='trip-date'>Ngày chuyến</Label>
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
                    <Label htmlFor='pickup-province'>Tỉnh/Thành phố</Label>
                    <TmsCombobox
                      id='pickup-province'
                      value={selectedProvinceCode}
                      onValueChange={handleProvinceChange}
                      options={provinceComboboxOptions}
                      placeholder='Chọn tỉnh/thành phố'
                      emptyText='Không tìm thấy tỉnh/thành phố'
                      disabled={isLoadingProvinces || !provinceOptions.length}
                      loading={isLoadingProvinces}
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='pickup-ward'>Phường/Xã</Label>
                    <TmsCombobox
                      id='pickup-ward'
                      value={selectedWardCode || 'ALL'}
                      onValueChange={handleWardChange}
                      options={wardComboboxOptions}
                      placeholder={
                        selectedProvinceCode
                          ? 'Chọn phường/xã (không bắt buộc)'
                          : 'Chọn tỉnh/thành phố trước'
                      }
                      emptyText={
                        isLoadingWards
                          ? 'Đang tải phường/xã...'
                          : 'Không tìm thấy phường/xã'
                      }
                      disabled={!selectedProvinceCode || isLoadingWards}
                      loading={isLoadingWards}
                    />
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='pickup-post-office'>Bưu cục</Label>
                    <TmsCombobox
                      id='pickup-post-office'
                      value={selectedPostOfficeId}
                      onValueChange={setSelectedPostOfficeId}
                      options={postOfficeComboboxOptions}
                      placeholder={
                        selectedProvinceCode
                          ? 'Chọn bưu cục'
                          : 'Chọn tỉnh/thành phố trước'
                      }
                      emptyText='Không tìm thấy bưu cục'
                      disabled={
                        !selectedProvinceCode ||
                        isLoadingPostOffices ||
                        !postOfficeOptions.length
                      }
                      loading={isLoadingPostOffices}
                    />
                    {selectedProvinceCode &&
                    !isLoadingPostOffices &&
                    !postOfficeOptions.length ? (
                      <p className='text-xs text-muted-foreground'>
                        Không tìm thấy bưu cục ở khu vực đã chọn.
                      </p>
                    ) : null}
                  </div>

                  <div className='space-y-2'>
                    <Label htmlFor='pickup-courier'>Nhân viên giao nhận</Label>
                    <TmsCombobox
                      id='pickup-courier'
                      value={selectedCourierStaffId}
                      onValueChange={setSelectedCourierStaffId}
                      options={courierComboboxOptions}
                      placeholder='Tất cả nhân viên giao nhận trong bưu cục'
                      emptyText='Không tìm thấy nhân viên giao nhận'
                      disabled={!selectedPostOfficeId || isLoadingCouriers}
                      loading={isLoadingCouriers}
                    />
                  </div>
                </>
              ) : (
                <div className='rounded-md border bg-muted/30 p-3 text-sm text-muted-foreground md:col-span-2'>
                  Bạn đang xem các đơn lấy hàng được giao cho chính mình.
                </div>
              )}
            </CardContent>
          </Card>

          <div className='grid gap-3 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-7'>
            <Card className='gap-2 py-3'>
              <CardHeader className='px-4 py-0'>
                <CardDescription>Tổng chuyến</CardDescription>
                <CardTitle className='text-xl'>
                  {formatNumber(pickupOverview?.totalTrips)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card className='gap-2 py-3'>
              <CardHeader className='px-4 py-0'>
                <CardDescription>Tổng đơn</CardDescription>
                <CardTitle className='text-xl'>
                  {formatNumber(pickupOverview?.totalOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card className='gap-2 py-3'>
              <CardHeader className='px-4 py-0'>
                <CardDescription>Đơn đã check-in</CardDescription>
                <CardTitle className='text-xl'>
                  {formatNumber(pickupOverview?.checkedInOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card className='gap-2 py-3'>
              <CardHeader className='px-4 py-0'>
                <CardDescription>Chờ check-in</CardDescription>
                <CardTitle className='text-xl'>
                  {formatNumber(pickupOverview?.pendingCheckinOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card className='gap-2 py-3'>
              <CardHeader className='px-4 py-0'>
                <CardDescription>Đơn đang lấy</CardDescription>
                <CardTitle className='text-xl'>
                  {formatNumber(pickupOverview?.pickingUpOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            <Card className='gap-2 py-3'>
              <CardHeader className='px-4 py-0'>
                <CardDescription>Đơn đã lấy</CardDescription>
                <CardTitle className='text-xl'>
                  {formatNumber(pickupOverview?.pickedUpOrders)}
                </CardTitle>
              </CardHeader>
            </Card>

            {canManagePostOfficeInbound ? (
              <Card className='gap-2 py-3'>
                <CardHeader className='px-4 py-0'>
                  <CardDescription>Chờ nhập bưu cục</CardDescription>
                  <CardTitle className='text-xl'>
                    {formatNumber(
                      pickupOverview?.pendingPostOfficeInboundOrders
                    )}
                  </CardTitle>
                </CardHeader>
              </Card>
            ) : null}
          </div>

          <Card className='gap-3 py-5'>
            <CardHeader className='px-5 py-0'>
              <CardTitle className='text-base'>Chuyến được phân công</CardTitle>
            </CardHeader>
            <CardContent className='px-5 py-0'>
              {trips.length === 0 ? (
                <div className='rounded-md border border-dashed p-4 text-center text-sm text-muted-foreground'>
                  Không tìm thấy chuyến theo bộ lọc đã chọn.
                </div>
              ) : (
                <div className='overflow-x-auto rounded-md border'>
                  <table className='w-full min-w-[820px] text-sm'>
                    <thead className='bg-muted/40 text-left'>
                      <tr>
                        <th className='px-3 py-2 font-medium'>Chuyến</th>
                        <th className='px-3 py-2 font-medium'>
                          Nhân viên giao nhận
                        </th>
                        <th className='px-3 py-2 font-medium'>Trạng thái</th>
                        <th className='px-3 py-2 font-medium'>
                          Tiến độ check-in
                        </th>
                        <th className='px-3 py-2 font-medium'>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      {trips.map((trip) => (
                        <tr key={trip.tripId} className='border-t'>
                          <td className='px-3 py-2'>
                            <div className='font-medium'>
                              {trip.tripCode || `#${trip.tripId}`}
                            </div>
                            <div className='text-xs text-muted-foreground'>
                              {formatDateTime(trip.plannedStartTime)} -{' '}
                              {formatDateTime(trip.plannedEndTime)}
                            </div>
                          </td>
                          <td className='px-3 py-2'>
                            <div>{trip.courierName || '--'}</div>
                            <div className='text-xs text-muted-foreground'>
                              {trip.courierCode || '--'}
                            </div>
                          </td>
                          <td className='px-3 py-2'>
                            <Badge
                              variant={
                                trip.tripStatus === 'COMPLETED'
                                  ? 'default'
                                  : 'secondary'
                              }
                            >
                              {formatPickupStatus(trip.tripStatus)}
                            </Badge>
                          </td>
                          <td className='px-3 py-2'>
                            {formatNumber(trip.checkedInOrders)} /{' '}
                            {formatNumber(trip.totalOrders)}
                          </td>
                          <td className='px-3 py-2'>
                            <div className='flex flex-wrap gap-2'>
                              {canCompleteTrip(trip) ? (
                                <Button
                                  size='sm'
                                  variant='outline'
                                  disabled={isCompletingTrip || isReturningTrip}
                                  onClick={() => void handleCompleteTrip(trip)}
                                >
                                  Hoàn tất chuyến
                                </Button>
                              ) : null}
                              {canReturnTripToPostOffice(trip, orders) ? (
                                <Button
                                  size='sm'
                                  disabled={isCompletingTrip || isReturningTrip}
                                  onClick={() =>
                                    void handleReturnTripToPostOffice(trip)
                                  }
                                >
                                  Chuyển về bưu cục
                                </Button>
                              ) : null}
                              {canManagePostOfficeInbound &&
                              canConfirmPostOfficeInbound(trip, orders) ? (
                                <Button
                                  size='sm'
                                  variant='outline'
                                  disabled={
                                    isCompletingTrip ||
                                    isReturningTrip ||
                                    isConfirmingPostOfficeInbound
                                  }
                                  onClick={() =>
                                    handleOpenPostOfficeInbound(trip)
                                  }
                                >
                                  Nhập bưu cục
                                </Button>
                              ) : null}
                              {!canCompleteTrip(trip) &&
                              !canReturnTripToPostOffice(trip, orders) &&
                              !(
                                canManagePostOfficeInbound &&
                                canConfirmPostOfficeInbound(trip, orders)
                              ) ? (
                                <span className='text-xs text-muted-foreground'>
                                  Không có thao tác
                                </span>
                              ) : null}
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>

          <Card className='gap-3 py-5'>
            <CardHeader className='px-5 py-0'>
              <CardTitle className='text-base'>Bản đồ lấy hàng</CardTitle>
              <CardDescription>
                {ordersWithCoordinates.length} đơn được phân công có vị trí.
              </CardDescription>
            </CardHeader>
            <CardContent className='px-5 py-0'>
              {ordersWithCoordinates.length === 0 ? (
                <div className='rounded-md border border-dashed p-4 text-center text-sm text-muted-foreground'>
                  Không có đơn lấy hàng được phân công có tọa độ theo bộ lọc đã
                  chọn.
                </div>
              ) : (
                <PickupOrdersMap
                  orders={ordersWithCoordinates}
                  selectedOrderId={selectedOrderId}
                  onSelectOrder={setSelectedOrderId}
                  className='h-[320px]'
                />
              )}
            </CardContent>
          </Card>

          <Card className='gap-3 py-5'>
            <CardHeader className='flex flex-col gap-2 px-5 py-0 sm:flex-row sm:items-center sm:justify-between'>
              <div>
                <CardTitle className='text-base'>Đơn được phân công</CardTitle>
                <CardDescription>
                  {isLoadingOverview
                    ? 'Đang tải đơn lấy hàng được phân công...'
                    : `${orders.length} đơn trong ${trips.length} chuyến.`}
                </CardDescription>
              </div>
              <Button
                variant='outline'
                size='sm'
                onClick={() => void refetchOverview()}
                disabled={isFetchingOverview}
              >
                {isFetchingOverview ? 'Đang làm mới...' : 'Làm mới'}
              </Button>
            </CardHeader>
            <CardContent className='px-5 py-0'>
              {orders.length === 0 ? (
                <div className='rounded-md border border-dashed p-4 text-center text-sm text-muted-foreground'>
                  Không có đơn lấy hàng được phân công theo bộ lọc đã chọn.
                </div>
              ) : (
                <div className='overflow-x-auto rounded-md border'>
                  <table className='w-full min-w-[900px] text-sm'>
                    <thead className='bg-muted/40 text-left'>
                      <tr>
                        <th className='px-3 py-2 font-medium'>Đơn</th>
                        <th className='px-3 py-2 font-medium'>
                          Nhân viên giao nhận
                        </th>
                        <th className='px-3 py-2 font-medium'>Chuyến</th>
                        <th className='px-3 py-2 font-medium'>
                          Khung giờ lấy hàng
                        </th>
                        <th className='px-3 py-2 font-medium'>Check-in</th>
                        <th className='px-3 py-2 font-medium'>Thao tác</th>
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
                                {formatPickupStatus(order.orderStatus)}
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
                              {canViewCheckinDetail(order) ? (
                                <button
                                  type='button'
                                  className='inline-flex'
                                  onClick={(event) => {
                                    event.stopPropagation();
                                    handleOpenCheckinDetailDialog(order);
                                  }}
                                >
                                  <Badge variant='default'>
                                    Đã check-in{' '}
                                    {formatDateTime(order.checkinTime)}
                                  </Badge>
                                </button>
                              ) : (
                                <Badge variant='secondary'>Đang chờ</Badge>
                              )}
                            </td>
                            <td className='px-3 py-2'>
                              <div className='flex flex-wrap gap-2'>
                                {canCheckinOrder(order, isCourierScope) ? (
                                  <Button
                                    size='sm'
                                    onClick={(event) => {
                                      event.stopPropagation();
                                      handleOpenCheckinDialog(order);
                                    }}
                                  >
                                    Check-in
                                  </Button>
                                ) : null}
                                {canViewCheckinDetail(order) ? (
                                  <Button
                                    variant='outline'
                                    size='sm'
                                    onClick={(event) => {
                                      event.stopPropagation();
                                      handleOpenCheckinDetailDialog(order);
                                    }}
                                  >
                                    Xem check-in
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
                                    Xem
                                  </Button>
                                )}
                              </div>
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
            <Card className='gap-3 py-5'>
              <CardHeader className='px-5 py-0'>
                <CardTitle className='text-base'>
                  Chi tiết đơn:{' '}
                  {selectedOrder.orderCode || `#${selectedOrder.orderId}`}
                </CardTitle>
                <CardDescription>
                  Chuyến {selectedOrder.tripCode || '--'} | Nhân viên{' '}
                  {selectedOrder.courierName || '--'}
                </CardDescription>
              </CardHeader>
              <CardContent className='grid gap-3 px-5 py-0 sm:grid-cols-3'>
                <div>
                  <div className='text-xs text-muted-foreground'>Người gửi</div>
                  <div className='font-medium'>
                    {selectedOrder.senderName || '--'}
                  </div>
                  <div>{selectedOrder.senderPhone || '--'}</div>
                  <div className='text-sm text-muted-foreground'>
                    {selectedOrder.senderAddressDetail || '--'}
                  </div>
                </div>

                <div className='space-y-2'>
                  <div className='text-xs text-muted-foreground'>Check-in</div>
                  {canViewCheckinDetail(selectedOrder) ? (
                    <>
                      <Badge variant='default'>
                        Đã check-in {formatDateTime(selectedOrder.checkinTime)}
                      </Badge>
                      <div>
                        <Button
                          variant='outline'
                          size='sm'
                          onClick={() =>
                            handleOpenCheckinDetailDialog(selectedOrder)
                          }
                        >
                          Xem chi tiết check-in
                        </Button>
                      </div>
                    </>
                  ) : (
                    <Badge variant='secondary'>Chờ check-in</Badge>
                  )}
                </div>

                <div>
                  <div className='text-xs text-muted-foreground'>
                    Kế hoạch phục vụ
                  </div>
                  <div>
                    Đến nơi: {formatDateTime(selectedOrder.plannedArrivalTime)}
                  </div>
                  <div>
                    Bắt đầu:{' '}
                    {formatDateTime(selectedOrder.plannedStartServiceTime)}
                  </div>
                  <div>
                    Rời đi: {formatDateTime(selectedOrder.plannedDepartureTime)}
                  </div>
                </div>
              </CardContent>
            </Card>
          ) : null}
        </>
      )}

      <PickupPostOfficeInboundDialog
        open={Boolean(inboundTrip)}
        trip={inboundTrip}
        orders={orders}
        isConfirming={isConfirmingPostOfficeInbound}
        onOpenChange={(open) => {
          if (!open) {
            setInboundTrip(null);
          }
        }}
        onConfirm={handleConfirmPostOfficeInbound}
      />

      <PickupCheckinDetailDialog
        open={isCheckinDetailDialogOpen}
        orderId={checkinDetailOrder?.orderId}
        orderCode={checkinDetailOrder?.orderCode}
        onOpenChange={(open) => {
          setIsCheckinDetailDialogOpen(open);
          if (!open) {
            setCheckinDetailOrder(null);
          }
        }}
      />

      <Dialog open={isCheckinDialogOpen} onOpenChange={setIsCheckinDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              Check-in lấy hàng {checkinOrder?.orderCode || ''}
            </DialogTitle>
            <DialogDescription>
              {devCheckinMode
                ? 'Tải ảnh lấy hàng lên. Chế độ phát triển sẽ điền tọa độ theo vị trí lấy hàng của đơn.'
                : 'Tải ảnh lấy hàng lên và chia sẻ vị trí hiện tại của bạn.'}
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-4'>
            {isDevCheckinFeatureAvailable ? (
              <div className='flex items-center justify-between gap-4 rounded-md border border-dashed border-amber-500/50 bg-amber-500/5 px-3 py-2'>
                <div className='space-y-0.5'>
                  <Label htmlFor='dev-checkin-mode' className='text-sm'>
                    Chế độ phát triển
                  </Label>
                  <p className='text-xs text-muted-foreground'>
                    Điền tọa độ check-in theo vị trí lấy hàng hợp lệ thay vì vị
                    trí GPS hiện tại.
                  </p>
                </div>
                <Switch
                  id='dev-checkin-mode'
                  checked={devCheckinMode}
                  onCheckedChange={handleDevCheckinModeChange}
                />
              </div>
            ) : null}
            <div className='space-y-2'>
              <Label htmlFor='checkin-photo'>Ảnh</Label>
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
                <Label htmlFor='checkin-latitude'>Vĩ độ</Label>
                <Input
                  id='checkin-latitude'
                  value={checkinLatitude}
                  readOnly
                  placeholder={
                    devCheckinMode
                      ? 'Tự điền từ vị trí lấy hàng'
                      : 'Lấy vị trí hiện tại'
                  }
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='checkin-longitude'>Kinh độ</Label>
                <Input
                  id='checkin-longitude'
                  value={checkinLongitude}
                  readOnly
                  placeholder={
                    devCheckinMode
                      ? 'Tự điền từ vị trí lấy hàng'
                      : 'Lấy vị trí hiện tại'
                  }
                />
              </div>
            </div>

            <Button
              type='button'
              variant='outline'
              onClick={() => void handleResolveCheckinLocation()}
              disabled={isResolvingLocation}
            >
              {isResolvingLocation
                ? 'Đang lấy vị trí...'
                : devCheckinMode
                  ? 'Điền vị trí lấy hàng hợp lệ'
                  : 'Dùng vị trí hiện tại'}
            </Button>
          </div>

          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsCheckinDialogOpen(false)}
              disabled={isSubmittingCheckin}
            >
              Hủy
            </Button>
            <Button
              onClick={() => void handleSubmitCheckin()}
              disabled={isSubmittingCheckin}
            >
              {isSubmittingCheckin ? 'Đang gửi...' : 'Xác nhận check-in'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};
