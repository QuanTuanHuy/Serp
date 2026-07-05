/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order journey tracking page
 */

'use client';

import React from 'react';
import {
  AlertCircle,
  Building2,
  CheckCircle2,
  CircleDot,
  Loader2,
  MapPin,
  PackageCheck,
  Search,
  Truck,
  UserRound,
  XCircle,
} from 'lucide-react';
import { getErrorMessage } from '@/lib/store';
import {
  useGetOrdersQuery,
  useLazyGetOrderByIdQuery,
  useLazyGetOrderTimelineQuery,
} from '@/modules/first-mile/api';
import type {
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  FirstMileOrderTimelineItem,
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
  Separator,
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
  | 'transit'
  | 'delivery'
  | 'exception';

interface JourneyStage {
  key: JourneyStageKey;
  title: string;
  status: 'done' | 'active' | 'pending' | 'exception';
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
  'transit',
  'delivery',
  'exception',
];

const stageTitles: Record<JourneyStageKey, string> = {
  confirmed: 'Đơn hàng đã xác nhận',
  pickup: 'Lấy hàng',
  facility: 'Bưu cục / trung tâm',
  transit: 'Vận chuyển liên tuyến',
  delivery: 'Giao hàng',
  exception: 'Sự cố',
};

const stageDescriptions: Record<JourneyStageKey, string> = {
  confirmed: 'Đang chờ dữ liệu xác nhận.',
  pickup: 'Đang chờ dữ liệu lấy hàng.',
  facility: 'Đang chờ dữ liệu quét tại bưu cục.',
  transit: 'Đang chờ dữ liệu vận chuyển.',
  delivery: 'Đang chờ dữ liệu giao hàng.',
  exception: 'Chưa ghi nhận hủy hoặc thất bại.',
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

  if (
    status === 'CANCELLED' ||
    status === 'LOST_OR_DAMAGED' ||
    status === 'PICKUP_FAILED' ||
    description.includes('cancel') ||
    description.includes('failed') ||
    description.includes('failure') ||
    description.includes('lost') ||
    description.includes('damaged')
  ) {
    return 'exception';
  }

  if (
    description.includes('deliver') ||
    description.includes('last mile') ||
    description.includes('receiver')
  ) {
    return 'delivery';
  }

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
      return 'facility';
    case 'OUTBOUND_READY_FROM_PO':
    case 'BAGGING_IN_PROGRESS':
    case 'BAGGED':
    case 'BAG_SEALED':
      return 'transit';
    default:
      if (item.postOfficeCode || item.postOfficeName || item.locationLabel) {
        return 'facility';
      }

      if (item.tripCode || item.vehicleLicensePlate || item.courierName) {
        return 'transit';
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

    latestEventByStage.set(stage, item);
  });

  if (order?.isConfirm && !latestEventByStage.has('confirmed')) {
    latestEventByStage.set('confirmed', {
      orderStatus: 'CREATED',
      eventTime: order.updatedAt || order.createdAt,
      description: 'Đã ghi nhận xác nhận đơn hàng.',
      recordedBy: order.updatedBy || order.createdBy,
    });
  }

  const exceptionEvent = latestEventByStage.get('exception');

  return stageOrder.map((stageKey) => {
    const item = latestEventByStage.get(stageKey);
    const stageIndex = stageOrder.indexOf(stageKey);
    const furthestDoneIndex = Math.max(
      ...stageOrder
        .filter((key) => key !== 'exception' && latestEventByStage.has(key))
        .map((key) => stageOrder.indexOf(key)),
      -1
    );
    const status =
      stageKey === 'exception'
        ? exceptionEvent
          ? 'exception'
          : 'pending'
        : item
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
        stage === 'exception'
          ? 'exception'
          : stage === 'facility'
            ? 'facility'
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

const getStageIcon = (stage: JourneyStage) => {
  if (stage.status === 'exception') {
    return XCircle;
  }

  switch (stage.key) {
    case 'confirmed':
      return CheckCircle2;
    case 'pickup':
    case 'transit':
    case 'delivery':
      return Truck;
    case 'facility':
      return Building2;
    default:
      return CircleDot;
  }
};

const getStageClasses = (stage: JourneyStage): string => {
  if (stage.status === 'exception') {
    return 'border-destructive bg-destructive text-destructive-foreground';
  }

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
  const [loadOrderById] = useLazyGetOrderByIdQuery();
  const [loadOrderTimeline] = useLazyGetOrderTimelineQuery();

  const orders = orderListQuery.data?.items ?? [];

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

                  <div className='grid gap-4 lg:grid-cols-[1fr_300px]'>
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
                                  {stage.status === 'exception'
                                    ? 'Sự cố'
                                    : stage.status === 'done'
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

                    <div className='rounded-md border p-4'>
                      <div className='mb-4'>
                        <h2 className='text-base font-semibold'>
                          Các sự kiện theo dõi
                        </h2>
                      </div>
                      <div className='space-y-3'>
                        {timeline.length === 0 ? (
                          <p className='text-sm text-muted-foreground'>
                            Chưa có sự kiện theo dõi nào.
                          </p>
                        ) : (
                          timeline.map((item, index) => (
                            <div key={`${item.id ?? index}-${item.eventTime}`}>
                              {index > 0 ? (
                                <Separator className='mb-3' />
                              ) : null}
                              <div className='space-y-1 text-sm'>
                                <div className='flex items-start justify-between gap-3'>
                                  <p className='font-medium'>
                                    {getEventDescription(item)}
                                  </p>
                                  {item.orderStatus ? (
                                    <Badge variant='outline'>
                                      {formatStatusLabel(item.orderStatus)}
                                    </Badge>
                                  ) : null}
                                </div>
                                <p className='text-xs text-muted-foreground'>
                                  {formatDateTime(item.eventTime)}
                                </p>
                                <p className='text-xs text-muted-foreground'>
                                  {getLocationLabel(item) || '--'}
                                </p>
                              </div>
                            </div>
                          ))
                        )}
                      </div>
                    </div>
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
