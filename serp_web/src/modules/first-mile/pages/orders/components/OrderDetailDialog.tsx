/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order detail dialog
 */

import React from 'react';
import {
  Badge,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/shared/components/ui';
import {
  AlertTriangle,
  CircleDot,
  ClipboardCheck,
  Clock3,
  Loader2,
  MapPin,
  PackageCheck,
  Truck,
  UserRound,
  Warehouse,
  XCircle,
} from 'lucide-react';
import type {
  FirstMileOrderDetail,
  FirstMileOrderStatus,
  FirstMileOrderTimelineItem,
} from '../../../types';
import { OrderRoutePreviewMap } from './OrderRoutePreviewMap';

interface OrderDetailDialogProps {
  open: boolean;
  detailOrder: FirstMileOrderDetail | null;
  timeline: FirstMileOrderTimelineItem[];
  isLoadingTimeline: boolean;
  onOpenChange: (open: boolean) => void;
  formatStatusLabel: (status: FirstMileOrderStatus) => string;
  formatPickupMethodLabel: (
    pickupMethod?: FirstMileOrderDetail['pickupMethod']
  ) => string;
  buildOrderAddressLabel: (
    name?: string,
    phone?: string,
    addressDetail?: string
  ) => string;
  getProvinceLabel: (provinceCode?: string) => string;
  getWardLabel: (provinceCode?: string, wardCode?: string) => string;
  formatDateTime: (value?: string) => string;
}

export const OrderDetailDialog: React.FC<OrderDetailDialogProps> = ({
  open,
  detailOrder,
  timeline,
  isLoadingTimeline,
  onOpenChange,
  formatStatusLabel,
  formatPickupMethodLabel,
  buildOrderAddressLabel,
  getProvinceLabel,
  getWardLabel,
  formatDateTime,
}) => {
  const hasRouteCoordinates = React.useMemo(() => {
    if (!detailOrder) {
      return false;
    }

    return (
      typeof detailOrder.senderLatitude === 'number' &&
      Number.isFinite(detailOrder.senderLatitude) &&
      typeof detailOrder.senderLongitude === 'number' &&
      Number.isFinite(detailOrder.senderLongitude) &&
      typeof detailOrder.receiverLatitude === 'number' &&
      Number.isFinite(detailOrder.receiverLatitude) &&
      typeof detailOrder.receiverLongitude === 'number' &&
      Number.isFinite(detailOrder.receiverLongitude)
    );
  }, [detailOrder]);

  const formatCoordinate = (value?: number): string => {
    if (value === undefined || value === null || !Number.isFinite(value)) {
      return '--';
    }

    return value.toFixed(6);
  };

  const getTimelineVisual = (status?: FirstMileOrderStatus) => {
    switch (status) {
      case 'CREATED':
        return {
          Icon: Clock3,
          circleClass: 'bg-sky-600',
          lineClass: 'bg-sky-600',
        };
      case 'ASSIGNED_TO_PICKUP':
      case 'PICKING_UP':
        return {
          Icon: Truck,
          circleClass: 'bg-amber-500',
          lineClass: 'bg-amber-500',
        };
      case 'PICKUP_FAILED':
        return {
          Icon: AlertTriangle,
          circleClass: 'bg-orange-500',
          lineClass: 'bg-orange-500',
        };
      case 'PICKED_UP':
        return {
          Icon: PackageCheck,
          circleClass: 'bg-teal-500',
          lineClass: 'bg-teal-500',
        };
      case 'AT_ORIGIN_POST_OFFICE':
        return {
          Icon: Warehouse,
          circleClass: 'bg-emerald-600',
          lineClass: 'bg-emerald-600',
        };
      case 'OUTBOUND_READY_FROM_PO':
      case 'INBOUND_AT_ORIGIN_HUB':
      case 'BAGGING_IN_PROGRESS':
      case 'BAGGED':
      case 'BAG_SEALED':
        return {
          Icon: ClipboardCheck,
          circleClass: 'bg-indigo-600',
          lineClass: 'bg-indigo-600',
        };
      case 'CANCELLED':
      case 'LOST_OR_DAMAGED':
        return {
          Icon: XCircle,
          circleClass: 'bg-destructive',
          lineClass: 'bg-destructive',
        };
      default:
        return {
          Icon: CircleDot,
          circleClass: 'bg-muted-foreground',
          lineClass: 'bg-muted-foreground',
        };
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='max-h-[90vh] overflow-y-auto sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>Order Details</DialogTitle>
          <DialogDescription>
            View full information of a first-mile order.
          </DialogDescription>
        </DialogHeader>

        {!detailOrder ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading order details...
          </div>
        ) : (
          <div className='space-y-4 text-sm'>
            <div className='grid gap-3 md:grid-cols-2'>
              <div>
                <p className='text-muted-foreground'>Order code</p>
                <p className='font-medium'>{detailOrder.orderCode}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Customer order code</p>
                <p className='font-medium'>
                  {detailOrder.customerOrderCode || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Status</p>
                <p className='font-medium'>
                  {formatStatusLabel(detailOrder.status)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Confirmation</p>
                <p className='font-medium'>
                  {detailOrder.isConfirm ? 'Confirmed' : 'Pending confirm'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Pickup method</p>
                <p className='font-medium'>
                  {formatPickupMethodLabel(detailOrder.pickupMethod)}
                </p>
              </div>
            </div>

            <div className='space-y-3 rounded-md border p-3'>
              <p className='font-semibold'>Status timeline</p>
              {isLoadingTimeline ? (
                <div className='flex items-center gap-2 text-muted-foreground'>
                  <Loader2 className='h-4 w-4 animate-spin' />
                  Loading timeline...
                </div>
              ) : timeline.length === 0 ? (
                <p className='text-muted-foreground'>No timeline data yet.</p>
              ) : (
                <TooltipProvider delayDuration={120}>
                  <div className='space-y-3'>
                    <p className='text-xs text-muted-foreground'>
                      Hover a timeline event to view full metadata. Scroll
                      horizontally if the history has many events.
                    </p>
                    <div className='overflow-x-auto pb-2'>
                      <div className='flex min-w-max items-start px-2 pt-2'>
                        {timeline.map((item, index) => {
                          const itemKey = `${item.id ?? index}-${item.eventTime ?? index}`;
                          const statusLabel = item.orderStatus
                            ? formatStatusLabel(item.orderStatus)
                            : '--';
                          const timelineVisual = getTimelineVisual(
                            item.orderStatus
                          );
                          const TimelineIcon = timelineVisual.Icon;
                          const isLastItem = index === timeline.length - 1;
                          const locationText =
                            item.locationLabel ||
                            `${item.postOfficeCode || '--'}${item.postOfficeName ? ` - ${item.postOfficeName}` : ''}`;
                          const courierText =
                            item.courierName || item.courierCode
                              ? `${item.courierName || '--'}${item.courierCode ? ` (${item.courierCode})` : ''}`
                              : '--';
                          const vehicleText = item.vehicleLicensePlate || '--';
                          const tripText = item.tripCode || '--';

                          return (
                            <div
                              key={itemKey}
                              className='relative flex w-44 shrink-0 flex-col items-center text-center'
                            >
                              {!isLastItem ? (
                                <span
                                  className={`absolute left-[calc(50%+2.75rem)] right-[calc(-50%+2.75rem)] top-10 h-1 rounded-full ${timelineVisual.lineClass}`}
                                />
                              ) : null}
                              <Tooltip>
                                <TooltipTrigger asChild>
                                  <button
                                    type='button'
                                    className='group relative z-10 flex w-full flex-col items-center rounded-lg px-2 pb-3 pt-1 outline-none transition-transform hover:-translate-y-0.5 focus-visible:ring-2 focus-visible:ring-ring'
                                  >
                                    <div
                                      className={`flex h-20 w-20 items-center justify-center rounded-full border-4 border-background text-white shadow-sm transition-shadow group-hover:shadow-md ${timelineVisual.circleClass}`}
                                    >
                                      <TimelineIcon className='h-9 w-9' />
                                    </div>
                                    <p className='mt-3 text-xs font-semibold text-primary'>
                                      {formatDateTime(item.eventTime)}
                                    </p>
                                    <Badge
                                      variant='outline'
                                      className='mt-2 max-w-full justify-center text-center'
                                    >
                                      {statusLabel}
                                    </Badge>
                                    <p className='mt-2 line-clamp-2 text-xs text-muted-foreground'>
                                      {item.description || 'No description'}
                                    </p>
                                  </button>
                                </TooltipTrigger>
                                <TooltipContent
                                  side='bottom'
                                  className='max-w-sm p-3'
                                >
                                  <div className='space-y-2 text-xs'>
                                    <div className='font-medium text-foreground'>
                                      {statusLabel} -{' '}
                                      {formatDateTime(item.eventTime)}
                                    </div>
                                    {item.description ? (
                                      <p className='text-foreground/90'>
                                        {item.description}
                                      </p>
                                    ) : null}
                                    <div className='space-y-1 text-muted-foreground'>
                                      <p className='flex items-center gap-1'>
                                        <MapPin className='h-3.5 w-3.5' />
                                        {locationText}
                                      </p>
                                      <p className='flex items-center gap-1'>
                                        <UserRound className='h-3.5 w-3.5' />
                                        {courierText}
                                      </p>
                                      <p className='flex items-center gap-1'>
                                        <Truck className='h-3.5 w-3.5' />
                                        {vehicleText} | Trip {tripText}
                                      </p>
                                      <p>
                                        Coordinates:{' '}
                                        {formatCoordinate(item.latitude)},{' '}
                                        {formatCoordinate(item.longitude)}
                                      </p>
                                      {item.recordedBy ? (
                                        <p>Recorded by: {item.recordedBy}</p>
                                      ) : null}
                                    </div>
                                  </div>
                                </TooltipContent>
                              </Tooltip>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  </div>
                </TooltipProvider>
              )}
            </div>

            <div className='space-y-2 rounded-md border p-3'>
              <p className='font-semibold'>Sender</p>
              <p>
                {buildOrderAddressLabel(
                  detailOrder.senderName,
                  detailOrder.senderPhone,
                  detailOrder.senderAddressDetail
                )}
              </p>
              <p className='text-muted-foreground'>
                {getProvinceLabel(detailOrder.senderProvinceCode)} /{' '}
                {getWardLabel(
                  detailOrder.senderProvinceCode,
                  detailOrder.senderWardCode
                )}
              </p>
              <p className='text-xs text-muted-foreground'>
                Code: {detailOrder.senderProvinceCode || '--'} /{' '}
                {detailOrder.senderWardCode || '--'}
              </p>
              <p className='text-muted-foreground'>
                Coordinates: {detailOrder.senderLatitude ?? '--'},
                {detailOrder.senderLongitude ?? '--'}
              </p>
            </div>

            <div className='space-y-2 rounded-md border p-3'>
              <p className='font-semibold'>Receiver</p>
              <p>
                {buildOrderAddressLabel(
                  detailOrder.receiverName,
                  detailOrder.receiverPhone,
                  detailOrder.receiverAddressDetail
                )}
              </p>
              <p className='text-muted-foreground'>
                {getProvinceLabel(detailOrder.receiverProvinceCode)} /{' '}
                {getWardLabel(
                  detailOrder.receiverProvinceCode,
                  detailOrder.receiverWardCode
                )}
              </p>
              <p className='text-xs text-muted-foreground'>
                Code: {detailOrder.receiverProvinceCode || '--'} /{' '}
                {detailOrder.receiverWardCode || '--'}
              </p>
              <p className='text-muted-foreground'>
                Coordinates: {detailOrder.receiverLatitude ?? '--'},
                {detailOrder.receiverLongitude ?? '--'}
              </p>
            </div>

            <div className='space-y-2 rounded-md border p-3'>
              <p className='font-semibold'>Pickup to destination map</p>
              {hasRouteCoordinates ? (
                <>
                  <OrderRoutePreviewMap
                    senderLatitude={detailOrder.senderLatitude as number}
                    senderLongitude={detailOrder.senderLongitude as number}
                    receiverLatitude={detailOrder.receiverLatitude as number}
                    receiverLongitude={detailOrder.receiverLongitude as number}
                    className='h-72'
                  />
                  <p className='text-xs text-muted-foreground'>
                    Blue marker: pickup point. Red marker: destination point.
                  </p>
                </>
              ) : (
                <p className='text-muted-foreground'>
                  Route map is unavailable because order coordinates are
                  incomplete.
                </p>
              )}
            </div>

            <div className='grid gap-3 md:grid-cols-2'>
              <div>
                <p className='text-muted-foreground'>Pickup start</p>
                <p className='font-medium'>
                  {formatDateTime(detailOrder.pickupTimeStart)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Pickup end</p>
                <p className='font-medium'>
                  {formatDateTime(detailOrder.pickupTimeEnd)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Delivery request time</p>
                <p className='font-medium'>
                  {detailOrder.deliveryRequestTime || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Order type</p>
                <p className='font-medium'>{detailOrder.orderType || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Fee payer</p>
                <p className='font-medium'>{detailOrder.feePayer || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Payment status</p>
                <p className='font-medium'>
                  {detailOrder.paymentStatus || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Total weight</p>
                <p className='font-medium'>{detailOrder.totalWeight ?? '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Total value</p>
                <p className='font-medium'>{detailOrder.totalValue ?? '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Total volume</p>
                <p className='font-medium'>{detailOrder.totalVolume ?? '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>COD amount</p>
                <p className='font-medium'>{detailOrder.codAmount ?? '--'}</p>
              </div>
            </div>

            <div className='space-y-2'>
              <p className='text-muted-foreground'>Products</p>
              {detailOrder.products && detailOrder.products.length > 0 ? (
                <div className='space-y-2'>
                  {detailOrder.products.map((product, index) => (
                    <div
                      key={`${detailOrder.id}-${product.id ?? index}`}
                      className='rounded-md border p-2'
                    >
                      <p className='font-medium'>{product.name || '--'}</p>
                      <p className='text-xs text-muted-foreground'>
                        Qty: {product.quantity ?? '--'} | Weight:{' '}
                        {product.weight ?? '--'}g | Value:{' '}
                        {product.value ?? '--'}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <p className='text-muted-foreground'>No product details.</p>
              )}
            </div>

            {detailOrder.note ? (
              <div>
                <p className='text-muted-foreground'>Note</p>
                <p>{detailOrder.note}</p>
              </div>
            ) : null}

            <div className='text-xs text-muted-foreground'>
              Created: {formatDateTime(detailOrder.createdAt)} | Updated:{' '}
              {formatDateTime(detailOrder.updatedAt)} | Updated by:{' '}
              {detailOrder.updatedBy || '--'}
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};
