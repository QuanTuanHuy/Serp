/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order detail dialog
 */

import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type {
  FirstMileOrderDetail,
  FirstMileOrderStatus,
} from '../../../types';
import { OrderRoutePreviewMap } from './OrderRoutePreviewMap';

interface OrderDetailDialogProps {
  open: boolean;
  detailOrder: FirstMileOrderDetail | null;
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
