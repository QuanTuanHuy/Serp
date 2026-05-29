/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order confirm and payment dialog
 */

import React from 'react';
import {
  Badge,
  Button,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/ui';
import { Loader2 } from 'lucide-react';
import type {
  CalculateShippingFeeResponse,
  FirstMileOrderDetail,
  OrderPaymentInitResponse,
} from '../../../types';

interface OrderConfirmDialogProps {
  open: boolean;
  order: FirstMileOrderDetail | null;
  shippingFee: CalculateShippingFeeResponse | null;
  paymentInitResult: OrderPaymentInitResponse | null;
  isCalculatingFee: boolean;
  isConfirmingOrder: boolean;
  isInitiatingPayment: boolean;
  isConfirmingPayment: boolean;
  onOpenChange: (open: boolean) => void;
  onRecalculateFee: () => void;
  onConfirmOrder: () => void;
  onInitiatePayment: () => void;
  onConfirmPayment: () => void;
}

const currencyFormatter = new Intl.NumberFormat('en-US');

const formatCurrency = (value?: number | null): string => {
  if (value === undefined || value === null || !Number.isFinite(value)) {
    return '--';
  }
  return `${currencyFormatter.format(value)} VND`;
};

export const OrderConfirmDialog: React.FC<OrderConfirmDialogProps> = ({
  open,
  order,
  shippingFee,
  paymentInitResult,
  isCalculatingFee,
  isConfirmingOrder,
  isInitiatingPayment,
  isConfirmingPayment,
  onOpenChange,
  onRecalculateFee,
  onConfirmOrder,
  onInitiatePayment,
  onConfirmPayment,
}) => {
  const isSenderPayer = order?.feePayer === 'SENDER';
  const isShippingPaid = order?.paymentStatus === 'PAID';
  const paymentRequired = Boolean(isSenderPayer && !isShippingPaid);
  const canConfirmOrder = Boolean(order && (!paymentRequired || isShippingPaid));
  const busy =
    isCalculatingFee ||
    isConfirmingOrder ||
    isInitiatingPayment ||
    isConfirmingPayment;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Confirm Order</DialogTitle>
          <DialogDescription>
            Review order details and shipping fee before confirmation.
          </DialogDescription>
        </DialogHeader>

        {!order ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Loading order details...
          </div>
        ) : (
          <div className='space-y-4 text-sm'>
            <div className='grid gap-3 rounded-md border p-3 md:grid-cols-2'>
              <div>
                <p className='text-muted-foreground'>Order code</p>
                <p className='font-medium'>{order.orderCode}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Status</p>
                <p className='font-medium'>{order.status}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Fee payer</p>
                <p className='font-medium'>{order.feePayer || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Shipping payment status</p>
                <p className='font-medium'>{order.paymentStatus || '--'}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Sender</p>
                <p className='font-medium'>
                  {order.senderName || '--'} - {order.senderPhone || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Receiver</p>
                <p className='font-medium'>
                  {order.receiverName || '--'} - {order.receiverPhone || '--'}
                </p>
              </div>
            </div>

            <div className='rounded-md border p-3'>
              <div className='mb-3 flex items-center justify-between gap-2'>
                <p className='font-semibold'>Shipping fee from billing service</p>
                <Button
                  type='button'
                  variant='outline'
                  size='sm'
                  onClick={onRecalculateFee}
                  disabled={busy}
                >
                  {isCalculatingFee ? (
                    <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                  ) : null}
                  Recalculate
                </Button>
              </div>

              {isCalculatingFee ? (
                <div className='flex items-center gap-2 text-muted-foreground'>
                  <Loader2 className='h-4 w-4 animate-spin' />
                  Calculating shipping fee...
                </div>
              ) : shippingFee ? (
                <div className='grid gap-2 md:grid-cols-2'>
                  <p>
                    Service: <strong>{shippingFee.serviceCode}</strong>
                  </p>
                  <p>
                    Route type: <strong>{shippingFee.routeType}</strong>
                  </p>
                  <p>Base fee: {formatCurrency(shippingFee.baseFee)}</p>
                  <p>Surcharge fee: {formatCurrency(shippingFee.surchargeFee)}</p>
                  <p>VAS fee: {formatCurrency(shippingFee.vasFee)}</p>
                  <p className='font-semibold'>
                    Total fee: {formatCurrency(shippingFee.totalFee)}
                  </p>
                </div>
              ) : (
                <p className='text-muted-foreground'>
                  Unable to calculate shipping fee for this order. Please check
                  order dimensions and weight.
                </p>
              )}
            </div>

            <div className='rounded-md border p-3'>
              <div className='flex flex-wrap items-center gap-2'>
                <Badge variant={isSenderPayer ? 'default' : 'secondary'}>
                  {isSenderPayer ? 'Sender pays shipping fee' : 'Receiver pays shipping fee'}
                </Badge>
                {order.codAmount && order.codAmount > 0 ? (
                  <Badge variant='outline'>
                    COD: {formatCurrency(order.codAmount)}
                  </Badge>
                ) : null}
              </div>
              <p className='mt-2 text-xs text-muted-foreground'>
                COD amount is always collected from receiver and is separate from
                shipping fee.
              </p>
            </div>

            {paymentRequired ? (
              <div className='rounded-md border p-3'>
                <p className='font-semibold'>Sender payment flow</p>
                <p className='mt-1 text-xs text-muted-foreground'>
                  Initiate payment, complete it on payment service, then verify
                  payment to continue order confirmation.
                </p>

                {paymentInitResult ? (
                  <div className='mt-2 space-y-1 text-xs text-muted-foreground'>
                    <p>Transaction: {paymentInitResult.appTransId}</p>
                    <p>Status: {paymentInitResult.status || '--'}</p>
                    {paymentInitResult.message ? (
                      <p>Gateway message: {paymentInitResult.message}</p>
                    ) : null}
                  </div>
                ) : null}
              </div>
            ) : null}
          </div>
        )}

        <DialogFooter className='gap-2'>
          {paymentRequired ? (
            <>
              <Button
                type='button'
                variant='outline'
                onClick={onInitiatePayment}
                disabled={busy || !shippingFee}
              >
                {isInitiatingPayment ? (
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                ) : null}
                Pay shipping fee
              </Button>
              <Button
                type='button'
                variant='outline'
                onClick={onConfirmPayment}
                disabled={busy || !paymentInitResult?.appTransId}
              >
                {isConfirmingPayment ? (
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                ) : null}
                Verify payment
              </Button>
            </>
          ) : null}

          <Button
            type='button'
            onClick={onConfirmOrder}
            disabled={busy || !canConfirmOrder}
          >
            {isConfirmingOrder ? (
              <Loader2 className='mr-2 h-4 w-4 animate-spin' />
            ) : null}
            Confirm order
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
