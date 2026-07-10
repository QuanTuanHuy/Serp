/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order journey tracking page
 */

'use client';

import React from 'react';
import {
  AlertCircle,
  ArrowRight,
  Building2,
  CheckCircle2,
  CircleDot,
  Clock3,
  Loader2,
  MapPin,
  Milestone,
  PackageCheck,
  Route as RouteIcon,
  Search,
  Truck,
  UserRound,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store';
import {
  useGetHubsQuery,
  useGetOrdersQuery,
  useLazyGetOrderByIdQuery,
  useLazyGetOrderTimelineQuery,
} from '@/modules/first-mile/api';
import type {
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  FirstMileOrderTimelineItem,
  FirstMilePlannedOrderRoute,
  FirstMilePlannedOrderRouteLeg,
  Hub,
} from '@/modules/first-mile/types';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';
import { useNotification } from '@/shared/hooks';
import { cn } from '@/shared/utils';
import {
  buildOrderAddressLabel,
  formatDateTime,
  formatPickupMethodLabel,
  formatStatusLabel,
  getStatusBadgeVariant,
} from '../list/orderPageModels';
import { TrackingMap, type TrackingMapPoint } from './TrackingMap';

type JourneyStageKey =
  | 'confirmed'
  | 'pickup'
  | 'facility'
  | 'delivery'
  | 'delivered';

interface JourneyStage {
  key: JourneyStageKey;
  title: string;
  status: 'done' | 'active' | 'pending';
  time?: string;
  description: string;
  actor?: string;
  vehicle?: string;
  location?: string;
}

const TRACKING_PAGE_SIZE = 8;

const stageOrder: JourneyStageKey[] = [
  'confirmed',
  'pickup',
  'facility',
  'delivery',
  'delivered',
];

const stageTitles: Record<JourneyStageKey, string> = {
  confirmed: 'Đơn hàng đã xác nhận',
  pickup: 'Đơn hàng đang được lấy',
  facility: 'Đơn hàng đã đến Bưu cục / trung tâm khai thác',
  delivery: 'Đơn hàng đang được giao',
  delivered: 'Đơn hàng được giao thành công',
};

const stageDescriptions: Record<JourneyStageKey, string> = {
  confirmed: 'Đang chờ dữ liệu xác nhận đơn hàng.',
  pickup: 'Đang chờ dữ liệu lấy hàng.',
  facility: 'Đang chờ dữ liệu tại bưu cục hoặc trung tâm khai thác.',
  delivery: 'Đang chờ dữ liệu giao hàng.',
  delivered: 'Đang chờ dữ liệu giao hàng thành công.',
};

const getTimelineStatus = (
  item: FirstMileOrderTimelineItem
): string | undefined => item.orderStatus;

const normalizeText = (value?: string): string => value?.toLowerCase() ?? '';

const getEventStage = (
  item: FirstMileOrderTimelineItem
): JourneyStageKey | null => {
  const status = getTimelineStatus(item);
  const description = normalizeText(item.description);

  switch (status) {
    case 'CREATED':
      return 'confirmed';
    case 'ASSIGNED_TO_PICKUP':
    case 'PICKING_UP':
    case 'PICKED_UP':
      return 'pickup';
    case 'PENDING_ORIGIN_POST_OFFICE_INBOUND':
    case 'AT_ORIGIN_POST_OFFICE':
    case 'INBOUND_AT_ORIGIN_HUB':
    case 'INBOUND_AT_DESTINATION_HUB':
    case 'INBOUND_AT_DESTINATION_POST_OFFICE':
    case 'OUTBOUND_READY_FROM_PO':
    case 'BAGGING_IN_PROGRESS':
    case 'BAGGED':
    case 'BAG_SEALED':
    case 'BAG_IN_TRANSIT':
    case 'READY_FOR_DELIVERY':
      return 'facility';
    case 'OUT_FOR_DELIVERY':
      return 'delivery';
    case 'DELIVERED':
      return 'delivered';
    default:
      if (item.postOfficeCode || item.postOfficeName || item.locationLabel) {
        return 'facility';
      }

      if (
        description.includes('deliver') ||
        description.includes('last mile') ||
        description.includes('receiver')
      ) {
        return 'delivery';
      }

      return null;
  }
};

const getActorLabel = (
  item?: FirstMileOrderTimelineItem
): string | undefined => {
  if (!item) {
    return undefined;
  }

  if (item.courierName || item.courierCode) {
    return [item.courierName, item.courierCode ? `(${item.courierCode})` : '']
      .filter(Boolean)
      .join(' ');
  }

  return item.recordedBy;
};

const getVehicleLabel = (
  item?: FirstMileOrderTimelineItem
): string | undefined => {
  if (!item) {
    return undefined;
  }

  const labels = [
    item.vehicleLicensePlate,
    item.tripCode ? `Trip ${item.tripCode}` : undefined,
  ].filter(Boolean);

  return labels.length > 0 ? labels.join(' / ') : undefined;
};

const getLocationLabel = (
  item?: FirstMileOrderTimelineItem
): string | undefined => {
  if (!item) {
    return undefined;
  }

  if (item.locationLabel) {
    return item.locationLabel;
  }

  if (item.postOfficeCode || item.postOfficeName) {
    return [item.postOfficeCode, item.postOfficeName]
      .filter(Boolean)
      .join(' - ');
  }

  return undefined;
};

const getEventDescription = (item?: FirstMileOrderTimelineItem): string => {
  if (!item) {
    return '';
  }

  if (item.description) {
    return item.description;
  }

  const status = getTimelineStatus(item);
  return status
    ? formatStatusLabel(status as FirstMileOrderStatus)
    : 'Đã cập nhật';
};

const getEventTimeValue = (item?: FirstMileOrderTimelineItem): number => {
  if (!item?.eventTime) {
    return Number.NEGATIVE_INFINITY;
  }

  const eventTime = new Date(item.eventTime).getTime();
  return Number.isNaN(eventTime) ? Number.NEGATIVE_INFINITY : eventTime;
};

const isNewerTimelineEvent = (
  candidate: FirstMileOrderTimelineItem,
  current?: FirstMileOrderTimelineItem
): boolean => {
  if (!current) {
    return true;
  }

  const candidateTime = getEventTimeValue(candidate);
  const currentTime = getEventTimeValue(current);

  if (candidateTime !== currentTime) {
    return candidateTime > currentTime;
  }

  return (candidate.id ?? 0) > (current.id ?? 0);
};

const buildJourneyStages = (
  order: FirstMileOrderDetail | null,
  timeline: FirstMileOrderTimelineItem[]
): JourneyStage[] => {
  const latestEventByStage = new Map<
    JourneyStageKey,
    FirstMileOrderTimelineItem
  >();

  timeline.forEach((item) => {
    const stage = getEventStage(item);
    if (!stage) {
      return;
    }

    if (isNewerTimelineEvent(item, latestEventByStage.get(stage))) {
      latestEventByStage.set(stage, item);
    }
  });

  if (order?.isConfirm && !latestEventByStage.has('confirmed')) {
    latestEventByStage.set('confirmed', {
      orderStatus: 'CREATED',
      eventTime: order.updatedAt || order.createdAt,
      description: 'Đã ghi nhận xác nhận đơn hàng.',
      recordedBy: order.updatedBy || order.createdBy,
    });
  }

  return stageOrder.map((stageKey) => {
    const item = latestEventByStage.get(stageKey);
    const stageIndex = stageOrder.indexOf(stageKey);
    const furthestDoneIndex = Math.max(
      ...stageOrder
        .filter((key) => latestEventByStage.has(key))
        .map((key) => stageOrder.indexOf(key)),
      -1
    );
    const status =
      item || stageIndex < furthestDoneIndex
        ? 'done'
        : stageIndex === furthestDoneIndex + 1
          ? 'active'
          : 'pending';

    return {
      key: stageKey,
      title: stageTitles[stageKey],
      status,
      time: item?.eventTime,
      description: item
        ? getEventDescription(item)
        : stageDescriptions[stageKey],
      actor: getActorLabel(item),
      vehicle: getVehicleLabel(item),
      location: getLocationLabel(item),
    };
  });
};

const getValidCoordinate = (
  latitude?: number,
  longitude?: number
): [number, number] | null => {
  const isValid =
    typeof latitude === 'number' &&
    typeof longitude === 'number' &&
    Number.isFinite(latitude) &&
    Number.isFinite(longitude);

  return isValid ? [latitude, longitude] : null;
};

const formatEstimatedDistance = (distanceKm?: number): string => {
  if (typeof distanceKm !== 'number' || !Number.isFinite(distanceKm)) {
    return '--';
  }

  return `${distanceKm.toLocaleString('vi-VN', {
    maximumFractionDigits: 1,
  })} km`;
};

const formatEstimatedDuration = (durationMinutes?: number): string => {
  if (
    typeof durationMinutes !== 'number' ||
    !Number.isFinite(durationMinutes) ||
    durationMinutes < 0
  ) {
    return '--';
  }

  const hours = Math.floor(durationMinutes / 60);
  const minutes = Math.round(durationMinutes % 60);

  if (hours <= 0) {
    return `${minutes} phút`;
  }

  return minutes > 0 ? `${hours} giờ ${minutes} phút` : `${hours} giờ`;
};

const formatRouteEndpoint = (
  type?: string,
  hubId?: number,
  postOfficeCode?: string,
  hubById?: Map<number, Hub>
): string => {
  const normalizedType = type?.toUpperCase();

  if (normalizedType === 'HUB') {
    const hub = hubId ? hubById?.get(hubId) : undefined;

    if (hub) {
      return [hub.code, hub.name].filter(Boolean).join(' - ');
    }

    return hubId ? `Hub #${hubId}` : 'Hub';
  }

  if (normalizedType === 'POST_OFFICE') {
    return postOfficeCode ? `Bưu cục ${postOfficeCode}` : 'Bưu cục';
  }

  return postOfficeCode || (hubId ? `Hub #${hubId}` : 'Điểm xử lý');
};

const getPlannedRouteLegs = (
  plannedRoute?: FirstMilePlannedOrderRoute
): FirstMilePlannedOrderRouteLeg[] => {
  return [...(plannedRoute?.legs ?? [])].sort((first, second) => {
    const firstSequence = first.sequence ?? Number.MAX_SAFE_INTEGER;
    const secondSequence = second.sequence ?? Number.MAX_SAFE_INTEGER;

    return firstSequence - secondSequence;
  });
};

const buildTrackingMapPoints = (
  order: FirstMileOrderDetail | null,
  timeline: FirstMileOrderTimelineItem[]
): TrackingMapPoint[] => {
  const points: TrackingMapPoint[] = [];

  const senderCoordinate = order
    ? getValidCoordinate(order.senderLatitude, order.senderLongitude)
    : null;

  if (order && senderCoordinate) {
    points.push({
      id: 'sender',
      label: 'Người gửi',
      description: buildOrderAddressLabel(
        order.senderName,
        order.senderPhone,
        order.senderAddressDetail
      ),
      latitude: senderCoordinate[0],
      longitude: senderCoordinate[1],
      kind: 'sender',
    });
  }

  timeline.forEach((item, index) => {
    const eventCoordinate = getValidCoordinate(item.latitude, item.longitude);

    if (!eventCoordinate) {
      return;
    }

    const stage = getEventStage(item);
    points.push({
      id: `${item.id ?? index}`,
      label: stage ? stageTitles[stage] : 'Sự kiện theo dõi',
      description: [
        getEventDescription(item),
        getActorLabel(item),
        getVehicleLabel(item),
      ]
        .filter(Boolean)
        .join(' | '),
      latitude: eventCoordinate[0],
      longitude: eventCoordinate[1],
      kind:
        stage === 'facility'
          ? 'facility'
          : stage === 'confirmed'
            ? 'sender'
            : stage === 'delivered'
              ? 'receiver'
              : 'movement',
    });
  });

  const receiverCoordinate = order
    ? getValidCoordinate(order.receiverLatitude, order.receiverLongitude)
    : null;

  if (order && receiverCoordinate) {
    points.push({
      id: 'receiver',
      label: 'Người nhận',
      description: buildOrderAddressLabel(
        order.receiverName,
        order.receiverPhone,
        order.receiverAddressDetail
      ),
      latitude: receiverCoordinate[0],
      longitude: receiverCoordinate[1],
      kind: 'receiver',
    });
  }

  return points;
};

interface PlannedRoutePanelProps {
  plannedRoute?: FirstMilePlannedOrderRoute;
  hubById: Map<number, Hub>;
}

const PlannedRoutePanel: React.FC<PlannedRoutePanelProps> = ({
  plannedRoute,
  hubById,
}) => {
  const legs = React.useMemo(
    () => getPlannedRouteLegs(plannedRoute),
    [plannedRoute]
  );

  return (
    <div className='rounded-md border p-4'>
      <div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
        <div className='space-y-1'>
          <div className='flex items-center gap-2'>
            <RouteIcon className='h-4 w-4 text-primary' />
            <h2 className='text-base font-semibold'>Lộ trình dự kiến</h2>
          </div>
          <p className='text-sm text-muted-foreground'>
            Theo dõi các chặng đã được hoạch định từ đơn hàng.
          </p>
        </div>
        <div className='grid grid-cols-2 gap-2 text-sm md:min-w-[260px]'>
          <div className='rounded-md bg-muted px-3 py-2'>
            <p className='flex items-center gap-1.5 text-xs text-muted-foreground'>
              <Milestone className='h-3.5 w-3.5' />
              Quãng đường
            </p>
            <p className='font-medium'>
              {formatEstimatedDistance(plannedRoute?.totalEstimatedDistanceKm)}
            </p>
          </div>
          <div className='rounded-md bg-muted px-3 py-2'>
            <p className='flex items-center gap-1.5 text-xs text-muted-foreground'>
              <Clock3 className='h-3.5 w-3.5' />
              Thời gian
            </p>
            <p className='font-medium'>
              {formatEstimatedDuration(
                plannedRoute?.totalEstimatedDurationMinutes
              )}
            </p>
          </div>
        </div>
      </div>

      {legs.length === 0 ? (
        <div className='mt-4 rounded-md border border-dashed p-4 text-sm text-muted-foreground'>
          Chưa có lộ trình dự kiến cho đơn hàng này.
        </div>
      ) : (
        <div className='mt-4 space-y-3'>
          {legs.map((leg, index) => {
            const originLabel = formatRouteEndpoint(
              leg.originType,
              leg.originHubId,
              leg.originPostOfficeCode,
              hubById
            );
            const destinationLabel = formatRouteEndpoint(
              leg.destinationType,
              leg.destinationHubId,
              leg.destinationPostOfficeCode,
              hubById
            );

            return (
              <div
                key={`${leg.sequence ?? index}-${originLabel}-${destinationLabel}`}
                className='grid gap-3 rounded-md border p-3 lg:grid-cols-[1fr_auto]'
              >
                <div className='min-w-0'>
                  <div className='flex flex-wrap items-center gap-2 text-sm font-medium'>
                    <span>{originLabel}</span>
                    <ArrowRight className='h-4 w-4 text-muted-foreground' />
                    <span>{destinationLabel}</span>
                  </div>
                  <p className='mt-1 text-xs text-muted-foreground'>
                    {leg.routeCode || leg.routeName
                      ? [leg.routeCode, leg.routeName]
                          .filter(Boolean)
                          .join(' - ')
                      : 'Chưa gán tuyến vận chuyển'}
                  </p>
                </div>
                <div className='flex flex-wrap gap-2 text-xs text-muted-foreground lg:justify-end'>
                  <Badge variant='outline'>
                    Chặng {leg.sequence ?? index + 1}
                  </Badge>
                  <Badge variant='outline'>
                    {formatEstimatedDistance(leg.estimatedDistanceKm)}
                  </Badge>
                  <Badge variant='outline'>
                    {formatEstimatedDuration(leg.estimatedDurationMinutes)}
                  </Badge>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

const getStageIcon = (stage: JourneyStage) => {
  switch (stage.key) {
    case 'confirmed':
    case 'delivered':
      return CheckCircle2;
    case 'pickup':
    case 'delivery':
      return Truck;
    case 'facility':
      return Building2;
    default:
      return CircleDot;
  }
};

const getStageClasses = (stage: JourneyStage): string => {
  if (stage.status === 'done') {
    return 'border-emerald-600 bg-emerald-600 text-white';
  }

  if (stage.status === 'active') {
    return 'border-amber-500 bg-amber-500 text-white';
  }

  return 'border-muted bg-muted text-muted-foreground';
};

const findExactOrder = (
  orders: FirstMileOrderDetail[],
  code: string
): FirstMileOrderDetail | undefined => {
  const normalizedCode = code.trim().toLowerCase();

  return orders.find((order) => {
    return (
      order.orderCode?.toLowerCase() === normalizedCode ||
      order.customerOrderCode?.toLowerCase() === normalizedCode
    );
  });
};

export const OrderTrackingPage: React.FC = () => {
  const notification = useNotification();
  const [searchText, setSearchText] = React.useState('');
  const [appliedSearch, setAppliedSearch] = React.useState('');
  const [selectedOrderId, setSelectedOrderId] = React.useState<number | null>(
    null
  );
  const [selectedOrder, setSelectedOrder] =
    React.useState<FirstMileOrderDetail | null>(null);
  const [timeline, setTimeline] = React.useState<FirstMileOrderTimelineItem[]>(
    []
  );
  const [isLoadingSelection, setIsLoadingSelection] = React.useState(false);
  const [shouldSelectSearchResult, setShouldSelectSearchResult] =
    React.useState(false);

  const orderListQuery = useGetOrdersQuery({
    page: 0,
    size: TRACKING_PAGE_SIZE,
    keyword: appliedSearch || undefined,
  });
  const hubsQuery = useGetHubsQuery({
    page: 0,
    size: 500,
    status: 'ACTIVE',
  });
  const [loadOrderById] = useLazyGetOrderByIdQuery();
  const [loadOrderTimeline] = useLazyGetOrderTimelineQuery();

  const orders = orderListQuery.data?.items ?? [];
  const hubById = React.useMemo(() => {
    const nextHubById = new Map<number, Hub>();

    (hubsQuery.data?.items ?? []).forEach((hub) => {
      nextHubById.set(hub.id, hub);
    });

    return nextHubById;
  }, [hubsQuery.data?.items]);

  const loadTrackingData = React.useCallback(
    async (orderId: number) => {
      setSelectedOrderId(orderId);
      setIsLoadingSelection(true);
      setTimeline([]);

      try {
        const [orderDetail, orderTimeline] = await Promise.all([
          loadOrderById(orderId).unwrap(),
          loadOrderTimeline(orderId).unwrap(),
        ]);

        setSelectedOrder(orderDetail);
        setTimeline(orderTimeline);
      } catch (error) {
        notification.error('Không thể tải hành trình đơn hàng.', {
          description: getErrorMessage(error),
        });
        setSelectedOrder(null);
      } finally {
        setIsLoadingSelection(false);
      }
    },
    [loadOrderById, loadOrderTimeline, notification]
  );

  React.useEffect(() => {
    if (!shouldSelectSearchResult || orderListQuery.isFetching) {
      return;
    }

    setShouldSelectSearchResult(false);

    if (orders.length === 0) {
      notification.error('Không tìm thấy đơn hàng với mã này.');
      return;
    }

    const exactOrder = findExactOrder(orders, appliedSearch);
    void loadTrackingData((exactOrder ?? orders[0]).id);
  }, [
    appliedSearch,
    loadTrackingData,
    notification,
    orderListQuery.isFetching,
    orders,
    shouldSelectSearchResult,
  ]);

  const handleTrackOrder = (event: React.FormEvent) => {
    event.preventDefault();

    const nextSearch = searchText.trim();
    if (!nextSearch) {
      notification.error('Vui lòng nhập mã đơn hàng trước khi tra cứu.');
      return;
    }

    setAppliedSearch(nextSearch);
    setShouldSelectSearchResult(true);
  };

  const journeyStages = React.useMemo(
    () => buildJourneyStages(selectedOrder, timeline),
    [selectedOrder, timeline]
  );
  const mapPoints = React.useMemo(
    () => buildTrackingMapPoints(selectedOrder, timeline),
    [selectedOrder, timeline]
  );

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-3 md:flex-row md:items-end md:justify-between'>
        <div className='space-y-1'>
          <h1 className='text-2xl font-bold'>Tra cứu đơn hàng</h1>
          <p className='text-sm text-muted-foreground'>
            Theo dõi các mốc lấy hàng, bưu cục, vận chuyển và giao hàng.
          </p>
        </div>
        <form
          className='flex w-full flex-col gap-2 sm:flex-row md:max-w-xl'
          onSubmit={handleTrackOrder}
        >
          <div className='grid flex-1 gap-1.5'>
            <Label htmlFor='order-tracking-code'>Mã đơn hàng</Label>
            <Input
              id='order-tracking-code'
              value={searchText}
              placeholder='Mã đơn hàng hoặc mã khách hàng'
              onChange={(event) => setSearchText(event.target.value)}
            />
          </div>
          <Button
            type='submit'
            className='sm:self-end'
            disabled={orderListQuery.isFetching || isLoadingSelection}
          >
            {orderListQuery.isFetching && shouldSelectSearchResult ? (
              <Loader2 className='h-4 w-4 animate-spin' />
            ) : (
              <Search className='h-4 w-4' />
            )}
            Tra cứu
          </Button>
        </form>
      </div>

      <div className='grid gap-4 xl:grid-cols-[minmax(320px,420px)_1fr]'>
        <Card>
          <CardHeader>
            <CardTitle>Đơn hàng</CardTitle>
            <CardDescription>
              Chọn một đơn hàng để xem hành trình hiện tại.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            {orderListQuery.isLoading ? (
              <div className='flex items-center gap-2 text-sm text-muted-foreground'>
                <Loader2 className='h-4 w-4 animate-spin' />
                Đang tải đơn hàng...
              </div>
            ) : orderListQuery.isError ? (
              <div className='flex items-center gap-2 text-sm text-destructive'>
                <AlertCircle className='h-4 w-4' />
                Không thể tải danh sách đơn hàng.
              </div>
            ) : orders.length === 0 ? (
              <p className='text-sm text-muted-foreground'>
                Không có đơn hàng nào khớp với tìm kiếm hiện tại.
              </p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Đơn hàng</TableHead>
                    <TableHead>Trạng thái</TableHead>
                    <TableHead className='text-right'>Thao tác</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {orders.map((order) => (
                    <TableRow
                      key={order.id}
                      data-state={
                        selectedOrderId === order.id ? 'selected' : undefined
                      }
                    >
                      <TableCell className='align-top'>
                        <div className='space-y-1'>
                          <p className='font-medium'>{order.orderCode}</p>
                          <p className='text-xs text-muted-foreground'>
                            {order.customerOrderCode || '--'}
                          </p>
                        </div>
                      </TableCell>
                      <TableCell className='align-top'>
                        <Badge variant={getStatusBadgeVariant(order.status)}>
                          {formatStatusLabel(order.status)}
                        </Badge>
                      </TableCell>
                      <TableCell className='text-right align-top'>
                        <Button
                          type='button'
                          variant='outline'
                          size='sm'
                          disabled={isLoadingSelection}
                          onClick={() => void loadTrackingData(order.id)}
                        >
                          Xem
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>

        <div className='space-y-4'>
          <Card>
            <CardHeader>
              <div className='flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between'>
                <div className='space-y-1'>
                  <CardTitle>
                    {selectedOrder
                      ? selectedOrder.orderCode
                      : 'Tổng quan hành trình'}
                  </CardTitle>
                  <CardDescription>
                    {selectedOrder
                      ? buildOrderAddressLabel(
                          selectedOrder.senderName,
                          selectedOrder.senderPhone,
                          selectedOrder.senderAddressDetail
                        )
                      : 'Chưa chọn đơn hàng.'}
                  </CardDescription>
                </div>
                {selectedOrder ? (
                  <Badge variant={getStatusBadgeVariant(selectedOrder.status)}>
                    {formatStatusLabel(selectedOrder.status)}
                  </Badge>
                ) : null}
              </div>
            </CardHeader>
            <CardContent>
              {isLoadingSelection ? (
                <div className='flex min-h-[240px] items-center justify-center gap-2 text-muted-foreground'>
                  <Loader2 className='h-4 w-4 animate-spin' />
                  Đang tải hành trình...
                </div>
              ) : !selectedOrder ? (
                <div className='flex min-h-[240px] items-center justify-center rounded-md border border-dashed text-sm text-muted-foreground'>
                  Chọn một đơn hàng để hiển thị dữ liệu theo dõi.
                </div>
              ) : (
                <div className='space-y-4'>
                  <div className='grid gap-3 md:grid-cols-3'>
                    <div className='rounded-md border p-3'>
                      <p className='text-xs text-muted-foreground'>
                        Mã khách hàng
                      </p>
                      <p className='font-medium'>
                        {selectedOrder.customerOrderCode || '--'}
                      </p>
                    </div>
                    <div className='rounded-md border p-3'>
                      <p className='text-xs text-muted-foreground'>
                        Phương thức lấy hàng
                      </p>
                      <p className='font-medium'>
                        {formatPickupMethodLabel(selectedOrder.pickupMethod)}
                      </p>
                    </div>
                    <div className='rounded-md border p-3'>
                      <p className='text-xs text-muted-foreground'>Cập nhật</p>
                      <p className='font-medium'>
                        {formatDateTime(selectedOrder.updatedAt)}
                      </p>
                    </div>
                  </div>

                  <TrackingMap points={mapPoints} />

                  <PlannedRoutePanel
                    plannedRoute={selectedOrder.plannedRoute}
                    hubById={hubById}
                  />

                  <div className='space-y-4'>
                    {journeyStages.map((stage, index) => {
                      const StageIcon = getStageIcon(stage);
                      const isLastStage = index === journeyStages.length - 1;

                      return (
                        <div key={stage.key} className='relative flex gap-3'>
                          {!isLastStage ? (
                            <span className='absolute left-5 top-11 h-[calc(100%-1.75rem)] w-px bg-border' />
                          ) : null}
                          <div
                            className={cn(
                              'relative z-10 flex h-10 w-10 shrink-0 items-center justify-center rounded-full border',
                              getStageClasses(stage)
                            )}
                          >
                            <StageIcon className='h-5 w-5' />
                          </div>
                          <div className='min-w-0 flex-1 rounded-md border p-3'>
                            <div className='flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between'>
                              <div>
                                <p className='font-semibold'>{stage.title}</p>
                                <p className='text-sm text-muted-foreground'>
                                  {stage.description}
                                </p>
                              </div>
                              <Badge variant='outline'>
                                {stage.status === 'done'
                                  ? 'Hoàn tất'
                                  : stage.status === 'active'
                                    ? 'Đang diễn ra'
                                    : 'Chờ xử lý'}
                              </Badge>
                            </div>
                            <div className='mt-3 grid gap-2 text-xs text-muted-foreground sm:grid-cols-2'>
                              <p className='flex items-center gap-1.5'>
                                <MapPin className='h-3.5 w-3.5' />
                                {stage.location || '--'}
                              </p>
                              <p className='flex items-center gap-1.5'>
                                <UserRound className='h-3.5 w-3.5' />
                                {stage.actor || '--'}
                              </p>
                              <p className='flex items-center gap-1.5'>
                                <Truck className='h-3.5 w-3.5' />
                                {stage.vehicle || '--'}
                              </p>
                              <p className='flex items-center gap-1.5'>
                                <PackageCheck className='h-3.5 w-3.5' />
                                {formatDateTime(stage.time)}
                              </p>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
};
