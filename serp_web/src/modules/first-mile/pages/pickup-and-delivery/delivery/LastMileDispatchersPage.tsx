/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Last-mile delivery trip check-in page
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
  Switch,
} from '@/shared/components';
import { TmsCombobox } from '@/modules/first-mile/components';
import { useNotification } from '@/shared/hooks';
import {
  useGetActiveCouriersByPostOfficeQuery,
  useGetDeliveryDispatchTripsQuery,
  useGetOrdersQuery,
  useGetPostOfficesQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useConfirmTripDeliveredMutation,
} from '../../../api';
import type {
  DeliveryAssignedStop,
  DeliveryAssignedTrip,
  DeliveryAssignmentResponse,
  DeliveryOrderStatus,
  FirstMileFeePayer,
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  PickupShift,
  PostOffice,
  PostOfficeStaff,
  Province,
  Ward,
} from '../../../types';
import {
  isDeliveryCheckinDevFeatureAvailable,
  readDeliveryDevCheckinModePreference,
  resolveValidDeliveryCheckinCoordinates,
  writeDeliveryDevCheckinModePreference,
} from './deliveryCheckinDev';
import { formatOrderStatusLabel } from '../../../utils/orderStatusLabels';

type DeliveryPageAccessScope =
  | 'ADMIN_ALL'
  | 'MANAGER_SCOPED'
  | 'COURIER_SELF'
  | 'NO_ACCESS';

interface DeliveryTrackingOrder extends DeliveryAssignedStop {
  tripId?: number;
  tripCode?: string;
  courierStaffId?: number;
  courierCode?: string;
  courierName?: string;
  postOfficeCode?: string;
  orderStatus?: FirstMileOrderStatus;
  receiverAddressDetail?: string;
  feePayer?: FirstMileFeePayer;
  paymentStatus?: string;
  codAmount?: number;
  totalShippingFee?: number;
}

const POST_OFFICE_PAGE_SIZE = 200;
const RECEIVER_FEE_PAYER = 'RECEIVER';

const SHIFT_LABELS: Record<PickupShift, string> = {
  MORNING: 'Ca sáng',
  AFTERNOON: 'Ca chiều',
  EVENING: 'Ca tối',
};

const DELIVERY_STATUS_LABELS: Record<DeliveryOrderStatus, string> = {
  PENDING: 'Chưa giao',
  OUT_FOR_DELIVERY: 'Đang giao',
  DELIVERED: 'Đã giao',
  FAILED: 'Giao thất bại',
  RETURNED: 'Đã hoàn',
};

const resolveDeliveryPageAccessScope = (
  roles: string[]
): DeliveryPageAccessScope => {
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

const formatNumber = (value?: number): string =>
  value === undefined || value === null ? '0' : String(value);

const formatCurrency = (value?: number | null): string => {
  if (value === undefined || value === null || !Number.isFinite(value)) {
    return '0 VND';
  }

  return `${value.toLocaleString('vi-VN')} VND`;
};

const normalizeOrderCode = (value?: string): string =>
  value?.trim().toUpperCase() ?? '';

const formatOrderStatus = (status?: string): string =>
  formatOrderStatusLabel(status);

const formatDeliveryStatus = (status?: DeliveryOrderStatus): string =>
  status ? DELIVERY_STATUS_LABELS[status] : '--';

const formatTrackingStatus = (order: DeliveryTrackingOrder): string => {
  if (order.deliveryStatus && order.deliveryStatus !== 'PENDING') {
    return formatDeliveryStatus(order.deliveryStatus);
  }

  return formatOrderStatus(order.orderStatus);
};

const getScopeLabel = (scope: DeliveryPageAccessScope): string => {
  if (scope === 'ADMIN_ALL') {
    return 'Quản trị TMS';
  }

  if (scope === 'MANAGER_SCOPED') {
    return 'Quản lý bưu cục';
  }

  if (scope === 'COURIER_SELF') {
    return 'Bưu tá';
  }

  return 'Không có quyền';
};

const getRequiredReceiverPayment = (
  order: DeliveryTrackingOrder | null
): number => {
  if (!order) {
    return 0;
  }

  const codAmount = Math.max(0, order.codAmount ?? 0);
  const receiverShippingFee =
    order.feePayer === RECEIVER_FEE_PAYER
      ? Math.max(0, order.totalShippingFee ?? 0)
      : 0;

  return codAmount + receiverShippingFee;
};

const canCheckinDeliveryOrder = (
  order: DeliveryTrackingOrder,
  isCourierScope: boolean
): boolean => {
  return (
    isCourierScope &&
    Boolean(order.tripId && order.orderCode) &&
    !order.deliveryCheckinTime &&
    order.deliveryStatus !== 'DELIVERED' &&
    (order.orderStatus === undefined ||
      order.orderStatus === 'READY_FOR_DELIVERY' ||
      order.orderStatus === 'DELIVERY_FAILED' ||
      order.orderStatus === 'OUT_FOR_DELIVERY')
  );
};

const buildOrderLookup = (
  orders: FirstMileOrderDetail[] = []
): Map<string, FirstMileOrderDetail> => {
  const lookup = new Map<string, FirstMileOrderDetail>();
  orders.forEach((order) => {
    lookup.set(normalizeOrderCode(order.orderCode), order);
    if (order.customerOrderCode) {
      lookup.set(normalizeOrderCode(order.customerOrderCode), order);
    }
  });
  return lookup;
};

const resolveTrips = (
  overview?: DeliveryAssignmentResponse
): DeliveryAssignedTrip[] => overview?.trips ?? [];

const flattenOrders = (
  trips: DeliveryAssignedTrip[],
  orderLookup: Map<string, FirstMileOrderDetail>
): DeliveryTrackingOrder[] => {
  return trips.flatMap((trip) =>
    (trip.stops ?? []).map((stop) => {
      const detail = orderLookup.get(normalizeOrderCode(stop.orderCode));

      return {
        ...stop,
        tripId: trip.tripId,
        tripCode: trip.tripCode,
        courierStaffId: trip.courierStaffId,
        courierCode: trip.courierCode,
        courierName: trip.courierName,
        orderStatus: detail?.status,
        receiverAddressDetail: detail?.receiverAddressDetail,
        feePayer: detail?.feePayer,
        paymentStatus: detail?.paymentStatus,
        codAmount: detail?.codAmount,
        totalShippingFee: detail?.totalShippingFee,
      };
    })
  );
};

export const LastMileDispatchersPage: React.FC = () => {
  const notification = useNotification();
  const profile = useAppSelector((state) => state.account.user.profile);
  const roles = profile?.roles ?? [];

  const accessScope = React.useMemo(
    () => resolveDeliveryPageAccessScope(roles),
    [roles]
  );

  const canAccess = accessScope !== 'NO_ACCESS';
  const isCourierScope = accessScope === 'COURIER_SELF';
  const shouldLoadLocationFilters = canAccess && !isCourierScope;

  const [tripDate, setTripDate] = React.useState(getTodayDateInputValue);
  const [selectedShift, setSelectedShift] = React.useState<PickupShift | 'all'>(
    'all'
  );
  const [selectedProvinceCode, setSelectedProvinceCode] = React.useState('');
  const [selectedWardCode, setSelectedWardCode] = React.useState('');
  const [selectedPostOfficeId, setSelectedPostOfficeId] = React.useState('');
  const [selectedCourierStaffId, setSelectedCourierStaffId] =
    React.useState('all');
  const [selectedOrderId, setSelectedOrderId] = React.useState<
    number | undefined
  >(undefined);
  const [checkinOrder, setCheckinOrder] =
    React.useState<DeliveryTrackingOrder | null>(null);
  const [checkinPhoto, setCheckinPhoto] = React.useState<File | null>(null);
  const [checkinLatitude, setCheckinLatitude] = React.useState('');
  const [checkinLongitude, setCheckinLongitude] = React.useState('');
  const [checkinNote, setCheckinNote] = React.useState('');
  const [isResolvingLocation, setIsResolvingLocation] = React.useState(false);
  const isDevCheckinFeatureAvailable = isDeliveryCheckinDevFeatureAvailable();
  const [devCheckinMode, setDevCheckinMode] = React.useState(false);

  React.useEffect(() => {
    if (isDevCheckinFeatureAvailable) {
      setDevCheckinMode(readDeliveryDevCheckinModePreference());
    }
  }, [isDevCheckinFeatureAvailable]);

  const { data: provincesData, isLoading: isLoadingProvinces } =
    useGetProvincesQuery(
      { page: 0, size: POST_OFFICE_PAGE_SIZE },
      { skip: !shouldLoadLocationFilters }
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
      { skip: !shouldLoadLocationFilters || !selectedProvinceCode }
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
      { skip: !shouldLoadLocationFilters || !selectedProvinceCode }
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

  const selectedPostOfficeNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(selectedPostOfficeId),
    [selectedPostOfficeId]
  );

  const selectedPostOffice = React.useMemo(
    () =>
      postOfficeOptions.find(
        (postOffice) => String(postOffice.id) === selectedPostOfficeId
      ),
    [postOfficeOptions, selectedPostOfficeId]
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

    notification.error('Không thể tải bưu tá của bưu cục đã chọn.', {
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
        fullName: courier.fullName?.trim() || 'Bưu tá chưa rõ',
      }));
  }, [couriersData]);

  const courierComboboxOptions = React.useMemo(
    () => [
      { value: 'all', label: 'Tất cả bưu tá' },
      ...courierOptions.map((courier) => ({
        value: String(courier.id),
        label: `${courier.code} - ${courier.fullName}`,
      })),
    ],
    [courierOptions]
  );

  const selectedCourierNumericId = React.useMemo(
    () => parseOptionalPositiveInteger(selectedCourierStaffId),
    [selectedCourierStaffId]
  );

  const shouldLoadTrips =
    canAccess && (isCourierScope || Boolean(selectedPostOfficeNumericId));

  const {
    data: deliveryOverview,
    isLoading: isLoadingOverview,
    isFetching: isFetchingOverview,
    refetch: refetchOverview,
    error: deliveryOverviewError,
  } = useGetDeliveryDispatchTripsQuery(
    {
      ...(isCourierScope
        ? {}
        : selectedPostOfficeNumericId
          ? { postOfficeId: selectedPostOfficeNumericId }
          : {}),
      ...(selectedShift !== 'all' ? { shift: selectedShift } : {}),
      tripDate,
    },
    { skip: !shouldLoadTrips }
  );

  React.useEffect(() => {
    if (!deliveryOverviewError) {
      return;
    }

    notification.error('Không thể tải dữ liệu theo dõi giao hàng.', {
      description: getErrorMessage(deliveryOverviewError),
    });
  }, [deliveryOverviewError, notification]);

  const trips = React.useMemo(
    () => resolveTrips(deliveryOverview),
    [deliveryOverview]
  );

  const orderCodes = React.useMemo(
    () =>
      Array.from(
        new Set(
          trips.flatMap((trip) =>
            (trip.stops ?? []).flatMap((stop) =>
              stop.orderCode ? [stop.orderCode] : []
            )
          )
        )
      ),
    [trips]
  );

  const { data: ordersData } = useGetOrdersQuery(
    {
      page: 0,
      size: 200,
      statuses: [
        'READY_FOR_DELIVERY',
        'OUT_FOR_DELIVERY',
        'DELIVERY_FAILED',
        'DELIVERED',
      ],
      ...(selectedPostOffice?.code
        ? { destinationPostOfficeCode: selectedPostOffice.code }
        : {}),
    },
    { skip: !shouldLoadTrips || orderCodes.length === 0 }
  );

  const orderLookup = React.useMemo(
    () => buildOrderLookup(ordersData?.items ?? []),
    [ordersData]
  );

  const allOrders = React.useMemo(
    () => flattenOrders(trips, orderLookup),
    [orderLookup, trips]
  );

  const filteredTrips = React.useMemo(() => {
    if (!selectedCourierNumericId) {
      return trips;
    }

    return trips.filter(
      (trip) => trip.courierStaffId === selectedCourierNumericId
    );
  }, [selectedCourierNumericId, trips]);

  const orders = React.useMemo(() => {
    if (!selectedCourierNumericId) {
      return allOrders;
    }

    return allOrders.filter(
      (order) => order.courierStaffId === selectedCourierNumericId
    );
  }, [allOrders, selectedCourierNumericId]);

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

  const checkedInOrders = React.useMemo(
    () => orders.filter((order) => Boolean(order.deliveryCheckinTime)).length,
    [orders]
  );

  const receiverPaymentOrders = React.useMemo(
    () =>
      orders.filter((order) => getRequiredReceiverPayment(order) > 0).length,
    [orders]
  );

  const [confirmTripDelivered, { isLoading: isSubmittingCheckin }] =
    useConfirmTripDeliveredMutation();

  const applyValidCheckinCoordinates = React.useCallback(
    (order: DeliveryTrackingOrder) => {
      const coordinates = resolveValidDeliveryCheckinCoordinates(order);
      if (!coordinates) {
        notification.error('Đơn hàng chưa có vị trí người nhận.', {
          description:
            'Vui lòng đảm bảo đơn có tọa độ người nhận trước khi check-in.',
        });
        return false;
      }

      setCheckinLatitude(String(coordinates.latitude));
      setCheckinLongitude(String(coordinates.longitude));
      return true;
    },
    [notification]
  );

  const handleDevCheckinModeChange = (enabled: boolean) => {
    setDevCheckinMode(enabled);
    writeDeliveryDevCheckinModePreference(enabled);

    if (enabled && checkinOrder) {
      applyValidCheckinCoordinates(checkinOrder);
    }
  };

  const handleOpenCheckinDialog = (order: DeliveryTrackingOrder) => {
    setCheckinOrder(order);
    setCheckinPhoto(null);
    setCheckinLatitude('');
    setCheckinLongitude('');
    setCheckinNote('');

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

    if (typeof window === 'undefined' || !('geolocation' in navigator)) {
      notification.error('Trình duyệt không hỗ trợ lấy vị trí hiện tại.');
      return;
    }

    setIsResolvingLocation(true);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCheckinLatitude(String(position.coords.latitude));
        setCheckinLongitude(String(position.coords.longitude));
        setIsResolvingLocation(false);
      },
      (error) => {
        notification.error('Không thể lấy vị trí hiện tại.', {
          description: error.message,
        });
        setIsResolvingLocation(false);
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  };

  const handleSubmitCheckin = async () => {
    if (!checkinOrder?.tripId || !checkinOrder.orderCode) {
      notification.error('Vui lòng chọn đơn để check-in.');
      return;
    }

    if (!checkinPhoto) {
      notification.error('Vui lòng chọn ảnh check-in giao hàng.');
      return;
    }

    const latitude = Number(checkinLatitude);
    const longitude = Number(checkinLongitude);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      notification.error('Vui lòng cung cấp vị trí check-in giao hàng hợp lệ.');
      return;
    }

    try {
      await confirmTripDelivered({
        tripId: checkinOrder.tripId,
        orderCode: checkinOrder.orderCode,
        body: {
          codCollected: Math.max(0, checkinOrder.codAmount ?? 0),
          shippingFeeCollected:
            checkinOrder.feePayer === RECEIVER_FEE_PAYER
              ? Math.max(0, checkinOrder.totalShippingFee ?? 0)
              : 0,
          latitude,
          longitude,
          photo: checkinPhoto,
          note: checkinNote || undefined,
        },
      }).unwrap();
      notification.success('Đã check-in giao hàng thành công.');
      setCheckinOrder(null);
      setCheckinPhoto(null);
      setCheckinLatitude('');
      setCheckinLongitude('');
      setCheckinNote('');
      void refetchOverview();
    } catch (error) {
      notification.error('Không thể check-in giao hàng.', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-2'>
        <h1 className='text-2xl font-bold tracking-tight'>
          Check-in giao hàng
        </h1>
        <p className='text-muted-foreground'>
          Theo dõi chuyến giao theo trip đã điều phối và xử lý từng điểm giao
          của bưu tá.
        </p>
      </div>

      <Card className='gap-3 py-5'>
        <CardHeader className='flex flex-col gap-2 px-5 py-0 sm:flex-row sm:items-center sm:justify-between'>
          <div>
            <CardTitle className='text-base'>Phạm vi truy cập</CardTitle>
            <CardDescription>
              {isCourierScope
                ? 'Bạn đang xem các chuyến giao được phân công cho hồ sơ bưu tá của mình.'
                : 'Chọn bưu cục, bưu tá, ngày và ca để theo dõi các chuyến giao.'}
            </CardDescription>
          </div>
          <Badge variant={canAccess ? 'default' : 'secondary'}>
            {getScopeLabel(accessScope)}
          </Badge>
        </CardHeader>
      </Card>

      {!canAccess ? (
        <Card className='p-12 text-center text-muted-foreground'>
          <p className='font-medium'>Không có quyền truy cập</p>
          <p className='mt-1 text-sm'>
            Tài khoản của bạn không có quyền truy cập màn hình giao hàng.
          </p>
        </Card>
      ) : (
        <>
          <Card className='gap-3 py-5'>
            <CardHeader className='px-5 py-0'>
              <CardTitle className='text-base'>Bộ lọc theo dõi</CardTitle>
              <CardDescription>
                Dữ liệu được lấy từ các trip giao hàng đã tạo bởi điều phối.
              </CardDescription>
            </CardHeader>
            <CardContent className='grid gap-4 px-5 py-0 md:grid-cols-4'>
              {isCourierScope ? (
                <div className='rounded-md border bg-muted/30 p-3 text-sm text-muted-foreground md:col-span-2'>
                  Hệ thống tự giới hạn dữ liệu theo bưu tá đang đăng nhập.
                </div>
              ) : (
                <>
                  <div className='space-y-1'>
                    <Label>Tỉnh/Thành phố</Label>
                    <TmsCombobox
                      id='delivery-province'
                      value={selectedProvinceCode}
                      onValueChange={setSelectedProvinceCode}
                      options={provinceComboboxOptions}
                      placeholder='Chọn tỉnh/thành phố'
                      emptyText='Không tìm thấy tỉnh/thành phố'
                      disabled={isLoadingProvinces}
                      loading={isLoadingProvinces}
                    />
                  </div>

                  <div className='space-y-1'>
                    <Label>Phường/Xã</Label>
                    <TmsCombobox
                      id='delivery-ward'
                      value={selectedWardCode || 'ALL'}
                      onValueChange={(value) =>
                        setSelectedWardCode(value === 'ALL' ? '' : value)
                      }
                      options={wardComboboxOptions}
                      placeholder='Chọn phường/xã'
                      emptyText='Không tìm thấy phường/xã'
                      disabled={!selectedProvinceCode || isLoadingWards}
                      loading={isLoadingWards}
                    />
                  </div>

                  <div className='space-y-1'>
                    <Label>Bưu cục</Label>
                    <TmsCombobox
                      id='delivery-post-office'
                      value={selectedPostOfficeId}
                      onValueChange={setSelectedPostOfficeId}
                      options={postOfficeComboboxOptions}
                      placeholder='Chọn bưu cục'
                      emptyText='Không tìm thấy bưu cục'
                      disabled={isLoadingPostOffices}
                      loading={isLoadingPostOffices}
                    />
                  </div>

                  <div className='space-y-1'>
                    <Label>Bưu tá</Label>
                    <TmsCombobox
                      id='delivery-courier'
                      value={selectedCourierStaffId}
                      onValueChange={setSelectedCourierStaffId}
                      options={courierComboboxOptions}
                      placeholder='Tất cả bưu tá'
                      emptyText='Không tìm thấy bưu tá'
                      disabled={
                        !selectedPostOfficeNumericId || isLoadingCouriers
                      }
                      loading={isLoadingCouriers}
                    />
                  </div>
                </>
              )}

              <div className='space-y-1'>
                <Label>Ngày giao</Label>
                <Input
                  type='date'
                  value={tripDate}
                  onChange={(event) => setTripDate(event.target.value)}
                />
              </div>

              <div className='space-y-1'>
                <Label>Ca giao</Label>
                <select
                  className='h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm'
                  value={selectedShift}
                  onChange={(event) =>
                    setSelectedShift(event.target.value as PickupShift | 'all')
                  }
                >
                  <option value='all'>Tất cả ca</option>
                  <option value='MORNING'>Ca sáng</option>
                  <option value='AFTERNOON'>Ca chiều</option>
                  <option value='EVENING'>Ca tối</option>
                </select>
              </div>

              <div className='flex items-end'>
                <Button
                  variant='outline'
                  className='w-full'
                  onClick={() => void refetchOverview()}
                  disabled={!shouldLoadTrips || isFetchingOverview}
                >
                  {isFetchingOverview ? 'Đang làm mới...' : 'Làm mới'}
                </Button>
              </div>
            </CardContent>
          </Card>

          <div className='grid gap-3 sm:grid-cols-2 lg:grid-cols-4'>
            <SummaryCard label='Chuyến giao' value={filteredTrips.length} />
            <SummaryCard label='Đơn được phân công' value={orders.length} />
            <SummaryCard label='Đã check-in' value={checkedInOrders} />
            <SummaryCard
              label='Cần thu trước giao'
              value={receiverPaymentOrders}
            />
          </div>

          <Card className='gap-3 py-5'>
            <CardHeader className='px-5 py-0'>
              <CardTitle className='text-base'>Chuyến giao</CardTitle>
              <CardDescription>
                {isLoadingOverview
                  ? 'Đang tải chuyến giao...'
                  : `${filteredTrips.length} chuyến trong ngày đã chọn.`}
              </CardDescription>
            </CardHeader>
            <CardContent className='px-5 py-0'>
              {!shouldLoadTrips ? (
                <div className='rounded-md border border-dashed p-4 text-center text-sm text-muted-foreground'>
                  Chọn bưu cục để xem các chuyến giao.
                </div>
              ) : filteredTrips.length === 0 ? (
                <div className='rounded-md border border-dashed p-4 text-center text-sm text-muted-foreground'>
                  Không có chuyến giao theo bộ lọc đã chọn.
                </div>
              ) : (
                <div className='overflow-x-auto rounded-md border'>
                  <table className='w-full min-w-[840px] text-sm'>
                    <thead className='bg-muted/40 text-left'>
                      <tr>
                        <th className='px-3 py-2 font-medium'>Chuyến</th>
                        <th className='px-3 py-2 font-medium'>Bưu tá</th>
                        <th className='px-3 py-2 font-medium'>Kế hoạch</th>
                        <th className='px-3 py-2 font-medium'>Điểm giao</th>
                        <th className='px-3 py-2 font-medium'>Quãng đường</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredTrips.map((trip) => (
                        <tr
                          key={trip.tripId ?? trip.tripCode}
                          className='border-t'
                        >
                          <td className='px-3 py-2'>
                            <div className='font-medium'>
                              {trip.tripCode || `#${trip.tripId}`}
                            </div>
                            <div className='text-xs text-muted-foreground'>
                              {selectedShift === 'all'
                                ? 'Tất cả ca'
                                : SHIFT_LABELS[selectedShift]}
                            </div>
                          </td>
                          <td className='px-3 py-2'>
                            <div>{trip.courierName || '--'}</div>
                            <div className='text-xs text-muted-foreground'>
                              {trip.courierCode || '--'}
                            </div>
                          </td>
                          <td className='px-3 py-2'>
                            <div>{formatDateTime(trip.plannedStartTime)}</div>
                            <div className='text-xs text-muted-foreground'>
                              {formatDateTime(trip.plannedEndTime)}
                            </div>
                          </td>
                          <td className='px-3 py-2'>
                            {formatNumber(trip.totalStops)}
                          </td>
                          <td className='px-3 py-2'>
                            {trip.totalDistanceKm?.toFixed(2) ?? '--'} km
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
            <CardHeader className='flex flex-col gap-2 px-5 py-0 sm:flex-row sm:items-center sm:justify-between'>
              <div>
                <CardTitle className='text-base'>Đơn được phân công</CardTitle>
                <CardDescription>
                  {isLoadingOverview
                    ? 'Đang tải đơn giao hàng...'
                    : `${orders.length} đơn trong ${filteredTrips.length} chuyến.`}
                </CardDescription>
              </div>
              <Button
                variant='outline'
                size='sm'
                onClick={() => void refetchOverview()}
                disabled={!shouldLoadTrips || isFetchingOverview}
              >
                {isFetchingOverview ? 'Đang làm mới...' : 'Làm mới'}
              </Button>
            </CardHeader>
            <CardContent className='px-5 py-0'>
              {orders.length === 0 ? (
                <div className='rounded-md border border-dashed p-4 text-center text-sm text-muted-foreground'>
                  Không có đơn giao hàng theo bộ lọc đã chọn.
                </div>
              ) : (
                <div className='overflow-x-auto rounded-md border'>
                  <table className='w-full min-w-[980px] text-sm'>
                    <thead className='bg-muted/40 text-left'>
                      <tr>
                        <th className='px-3 py-2 font-medium'>Đơn</th>
                        <th className='px-3 py-2 font-medium'>Bưu tá</th>
                        <th className='px-3 py-2 font-medium'>Chuyến</th>
                        <th className='px-3 py-2 font-medium'>Người nhận</th>
                        <th className='px-3 py-2 font-medium'>Thanh toán</th>
                        <th className='px-3 py-2 font-medium'>Check-in</th>
                        <th className='px-3 py-2 font-medium'>Thao tác</th>
                      </tr>
                    </thead>
                    <tbody>
                      {orders.map((order) => {
                        const isSelected = selectedOrderId === order.orderId;
                        const requiredPayment =
                          getRequiredReceiverPayment(order);

                        return (
                          <tr
                            key={`${order.tripId}-${order.sequence}-${order.orderId}`}
                            className={`border-t ${isSelected ? 'bg-accent/30' : ''}`}
                            onClick={() => setSelectedOrderId(order.orderId)}
                          >
                            <td className='px-3 py-2'>
                              <div className='font-medium'>
                                {order.orderCode || `#${order.orderId}`}
                              </div>
                              <div className='text-xs text-muted-foreground'>
                                {formatTrackingStatus(order)}
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
                                Điểm #{order.sequence}
                              </div>
                            </td>
                            <td className='px-3 py-2'>
                              <div>{order.receiverName || '--'}</div>
                              <div className='text-xs text-muted-foreground'>
                                {order.receiverPhone || '--'}
                              </div>
                            </td>
                            <td className='px-3 py-2'>
                              {requiredPayment > 0 ? (
                                <Badge variant='secondary'>
                                  Cần thu {formatCurrency(requiredPayment)}
                                </Badge>
                              ) : (
                                <Badge variant='outline'>Không thu thêm</Badge>
                              )}
                            </td>
                            <td className='px-3 py-2'>
                              {order.deliveryCheckinTime ? (
                                <Badge variant='default'>
                                  Đã check-in{' '}
                                  {formatDateTime(order.deliveryCheckinTime)}
                                </Badge>
                              ) : (
                                <Badge variant='secondary'>Đang chờ</Badge>
                              )}
                            </td>
                            <td className='px-3 py-2'>
                              <div className='flex flex-wrap gap-2'>
                                {canCheckinDeliveryOrder(
                                  order,
                                  isCourierScope
                                ) ? (
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
                  Chuyến {selectedOrder.tripCode || '--'} | Bưu tá{' '}
                  {selectedOrder.courierName || '--'}
                </CardDescription>
              </CardHeader>
              <CardContent className='grid gap-3 px-5 py-0 sm:grid-cols-3'>
                <div>
                  <div className='text-xs text-muted-foreground'>
                    Người nhận
                  </div>
                  <div className='font-medium'>
                    {selectedOrder.receiverName || '--'}
                  </div>
                  <div>{selectedOrder.receiverPhone || '--'}</div>
                  <div className='text-sm text-muted-foreground'>
                    {selectedOrder.receiverAddressDetail || '--'}
                  </div>
                </div>

                <div className='space-y-2'>
                  <div className='text-xs text-muted-foreground'>
                    Thanh toán trước khi giao
                  </div>
                  {getRequiredReceiverPayment(selectedOrder) > 0 ? (
                    <>
                      <Badge variant='secondary'>
                        Cần thu{' '}
                        {formatCurrency(
                          getRequiredReceiverPayment(selectedOrder)
                        )}
                      </Badge>
                      <div className='text-xs text-muted-foreground'>
                        Bao gồm COD và phí vận chuyển do người nhận thanh toán.
                      </div>
                    </>
                  ) : (
                    <Badge variant='outline'>Không cần thu thêm</Badge>
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

                <div className='space-y-2 sm:col-span-3'>
                  <div className='text-xs text-muted-foreground'>
                    Kết quả check-in
                  </div>
                  {selectedOrder.deliveryCheckinTime ? (
                    <div className='grid gap-3 rounded-md border bg-muted/30 p-3 text-sm sm:grid-cols-3'>
                      <div>
                        <div className='text-xs text-muted-foreground'>
                          Trạng thái giao
                        </div>
                        <Badge variant='default'>
                          {formatDeliveryStatus(selectedOrder.deliveryStatus)}
                        </Badge>
                      </div>
                      <div>
                        <div className='text-xs text-muted-foreground'>
                          Thời gian check-in
                        </div>
                        <div>
                          {formatDateTime(selectedOrder.deliveryCheckinTime)}
                        </div>
                      </div>
                      <div>
                        <div className='text-xs text-muted-foreground'>
                          Tọa độ check-in
                        </div>
                        <div>
                          {selectedOrder.deliveryCheckinLat !== undefined &&
                          selectedOrder.deliveryCheckinLng !== undefined
                            ? `${selectedOrder.deliveryCheckinLat.toFixed(6)}, ${selectedOrder.deliveryCheckinLng.toFixed(6)}`
                            : '--'}
                        </div>
                      </div>
                      <div>
                        <div className='text-xs text-muted-foreground'>
                          Khoảng cách
                        </div>
                        <div>
                          {selectedOrder.deliveryCheckinDistanceM !==
                            undefined &&
                          selectedOrder.deliveryCheckinDistanceM !== null
                            ? `${selectedOrder.deliveryCheckinDistanceM.toLocaleString('vi-VN')} m`
                            : '--'}
                        </div>
                      </div>
                      <div className='sm:col-span-2'>
                        <div className='text-xs text-muted-foreground'>
                          Ảnh bằng chứng
                        </div>
                        {selectedOrder.proofPhotoUrl ? (
                          <a
                            href={selectedOrder.proofPhotoUrl}
                            target='_blank'
                            rel='noopener noreferrer'
                            className='text-primary underline-offset-4 hover:underline'
                          >
                            Xem ảnh check-in
                          </a>
                        ) : (
                          <div>--</div>
                        )}
                      </div>
                    </div>
                  ) : (
                    <div className='rounded-md border border-dashed p-3 text-sm text-muted-foreground'>
                      Đơn này chưa có dữ liệu check-in giao hàng.
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          ) : null}
        </>
      )}

      <Dialog
        open={Boolean(checkinOrder)}
        onOpenChange={(open) => {
          if (!open) {
            setCheckinOrder(null);
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              Check-in giao hàng {checkinOrder?.orderCode || ''}
            </DialogTitle>
            <DialogDescription>
              Tải ảnh giao hàng lên và chia sẻ vị trí check-in tại điểm nhận.
            </DialogDescription>
          </DialogHeader>

          <div className='space-y-3 rounded-md border bg-muted/30 p-3 text-sm'>
            <div>
              <span className='text-muted-foreground'>Chuyến: </span>
              {checkinOrder?.tripCode || `#${checkinOrder?.tripId ?? '--'}`}
            </div>
            <div>
              <span className='text-muted-foreground'>Người nhận: </span>
              {checkinOrder?.receiverName || '--'} -{' '}
              {checkinOrder?.receiverPhone || '--'}
            </div>
            <div>
              <span className='text-muted-foreground'>Cần thu: </span>
              {formatCurrency(getRequiredReceiverPayment(checkinOrder))}
            </div>
          </div>

          <div className='space-y-4'>
            {isDevCheckinFeatureAvailable ? (
              <div className='flex items-center justify-between gap-4 rounded-md border border-dashed border-amber-500/50 bg-amber-500/5 px-3 py-2'>
                <div className='space-y-0.5'>
                  <Label htmlFor='delivery-dev-checkin-mode'>
                    Chế độ phát triển
                  </Label>
                  <p className='text-xs text-muted-foreground'>
                    Điền tọa độ check-in theo vị trí người nhận thay vì GPS hiện
                    tại.
                  </p>
                </div>
                <Switch
                  id='delivery-dev-checkin-mode'
                  checked={devCheckinMode}
                  onCheckedChange={handleDevCheckinModeChange}
                />
              </div>
            ) : null}

            <div className='space-y-2'>
              <Label htmlFor='delivery-checkin-photo'>Ảnh check-in</Label>
              <Input
                id='delivery-checkin-photo'
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
                <Label htmlFor='delivery-checkin-latitude'>Vĩ độ</Label>
                <Input
                  id='delivery-checkin-latitude'
                  value={checkinLatitude}
                  readOnly
                  placeholder={
                    devCheckinMode
                      ? 'Tự điền từ vị trí người nhận'
                      : 'Lấy vị trí hiện tại'
                  }
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='delivery-checkin-longitude'>Kinh độ</Label>
                <Input
                  id='delivery-checkin-longitude'
                  value={checkinLongitude}
                  readOnly
                  placeholder={
                    devCheckinMode
                      ? 'Tự điền từ vị trí người nhận'
                      : 'Lấy vị trí hiện tại'
                  }
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='delivery-checkin-note'>Ghi chú</Label>
              <Input
                id='delivery-checkin-note'
                value={checkinNote}
                onChange={(event) => setCheckinNote(event.target.value)}
                placeholder='Ghi chú không bắt buộc'
              />
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
                  ? 'Điền vị trí người nhận'
                  : 'Dùng vị trí hiện tại'}
            </Button>
          </div>

          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setCheckinOrder(null)}
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

interface SummaryCardProps {
  label: string;
  value: number;
}

const SummaryCard: React.FC<SummaryCardProps> = ({ label, value }) => (
  <Card className='p-4'>
    <div className='text-xs text-muted-foreground'>{label}</div>
    <div className='mt-1 text-2xl font-semibold'>{value}</div>
  </Card>
);
