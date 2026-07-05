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
} from '../../../../types';
import { formatStatusLabel } from '../orderPageModels';

interface OrderConfirmDialogProps {
  open: boolean;
  order: FirstMileOrderDetail | null;
  shippingFee: CalculateShippingFeeResponse | null;
  paymentInitResult: OrderPaymentInitResponse | null;
  isCalculatingFee: boolean;
  isConfirmingOrder: boolean;
  isInitiatingPayment: boolean;
  isAwaitingPaymentCompletion: boolean;
  isProcessingPaymentWebhook: boolean;
  onOpenChange: (open: boolean) => void;
  onRecalculateFee: () => void;
  onConfirmOrder: () => void;
  onInitiatePayment: () => void;
}

const currencyFormatter = new Intl.NumberFormat('vi-VN');

const formatCurrency = (value?: number | null): string => {
  if (value === undefined || value === null || !Number.isFinite(value)) {
    return '--';
  }
  return `${currencyFormatter.format(value)} VND`;
};

const formatFeePayerLabel = (value?: string | null): string => {
  switch (value) {
    case 'SENDER':
      return 'Người gửi';
    case 'RECEIVER':
      return 'Người nhận';
    default:
      return value || '--';
  }
};

const formatPaymentStatusLabel = (value?: string | null): string => {
  switch (value) {
    case 'PAID':
      return 'Đã thanh toán';
    case 'UNPAID':
      return 'Chưa thanh toán';
    case 'PENDING':
      return 'Đang chờ thanh toán';
    default:
      return value || '--';
  }
};

export const OrderConfirmDialog: React.FC<OrderConfirmDialogProps> = ({
  open,
  order,
  shippingFee,
  paymentInitResult,
  isCalculatingFee,
  isConfirmingOrder,
  isInitiatingPayment,
  isAwaitingPaymentCompletion,
  isProcessingPaymentWebhook,
  onOpenChange,
  onRecalculateFee,
  onConfirmOrder,
  onInitiatePayment,
}) => {
  const isSenderPayer = order?.feePayer === 'SENDER';
  const isShippingPaid = order?.paymentStatus === 'PAID';
  const paymentRequired = Boolean(isSenderPayer && !isShippingPaid);
  const canConfirmOrder = Boolean(
    order && (!paymentRequired || isShippingPaid)
  );
  const busy =
    isCalculatingFee ||
    isConfirmingOrder ||
    isInitiatingPayment ||
    isAwaitingPaymentCompletion ||
    isProcessingPaymentWebhook;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-2xl'>
        <DialogHeader>
          <DialogTitle>Xác nhận đơn hàng</DialogTitle>
          <DialogDescription>
            Kiểm tra chi tiết đơn hàng và phí vận chuyển trước khi xác nhận.
          </DialogDescription>
        </DialogHeader>

        {!order ? (
          <div className='flex items-center gap-2 text-muted-foreground'>
            <Loader2 className='h-4 w-4 animate-spin' />
            Đang tải chi tiết đơn hàng...
          </div>
        ) : (
          <div className='space-y-4 text-sm'>
            <div className='grid gap-3 rounded-md border p-3 md:grid-cols-2'>
              <div>
                <p className='text-muted-foreground'>Mã đơn hàng</p>
                <p className='font-medium'>{order.orderCode}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Trạng thái</p>
                <p className='font-medium'>{formatStatusLabel(order.status)}</p>
              </div>
              <div>
                <p className='text-muted-foreground'>Bên trả phí</p>
                <p className='font-medium'>
                  {formatFeePayerLabel(order.feePayer)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>
                  Trạng thái thanh toán phí vận chuyển
                </p>
                <p className='font-medium'>
                  {formatPaymentStatusLabel(order.paymentStatus)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Người gửi</p>
                <p className='font-medium'>
                  {order.senderName || '--'} - {order.senderPhone || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Người nhận</p>
                <p className='font-medium'>
                  {order.receiverName || '--'} - {order.receiverPhone || '--'}
                </p>
              </div>
            </div>

            <div className='rounded-md border p-3'>
              <div className='mb-3 flex items-center justify-between gap-2'>
                <p className='font-semibold'>
                  Phí vận chuyển từ dịch vụ billing
                </p>
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
                  Tính lại
                </Button>
              </div>

              {isCalculatingFee ? (
                <div className='flex items-center gap-2 text-muted-foreground'>
                  <Loader2 className='h-4 w-4 animate-spin' />
                  Đang tính phí vận chuyển...
                </div>
              ) : shippingFee ? (
                <div className='grid gap-2 md:grid-cols-2'>
                  <p>
                    Dịch vụ: <strong>{shippingFee.serviceCode}</strong>
                  </p>
                  <p>
                    Loại tuyến: <strong>{shippingFee.routeType}</strong>
                  </p>
                  <p>Phí cơ bản: {formatCurrency(shippingFee.baseFee)}</p>
                  <p>Phí phụ thu: {formatCurrency(shippingFee.surchargeFee)}</p>
                  <p className='font-semibold'>
                    Tổng phí: {formatCurrency(shippingFee.totalFee)}
                  </p>
                </div>
              ) : (
                <p className='text-muted-foreground'>
                  Không thể tính phí vận chuyển cho đơn hàng này. Vui lòng kiểm
                  tra kích thước và khối lượng đơn hàng.
                </p>
              )}
            </div>

            <div className='rounded-md border p-3'>
              <div className='flex flex-wrap items-center gap-2'>
                <Badge variant={isSenderPayer ? 'default' : 'secondary'}>
                  {isSenderPayer
                    ? 'Người gửi trả phí vận chuyển'
                    : 'Người nhận trả phí vận chuyển'}
                </Badge>
                {order.codAmount && order.codAmount > 0 ? (
                  <Badge variant='outline'>
                    COD: {formatCurrency(order.codAmount)}
                  </Badge>
                ) : null}
              </div>
              <p className='mt-2 text-xs text-muted-foreground'>
                Số tiền COD luôn được thu từ người nhận và tách biệt với phí vận
                chuyển.
              </p>
            </div>

            {paymentRequired ? (
              <div className='rounded-md border p-3'>
                <p className='font-semibold'>
                  Quy trình thanh toán của người gửi
                </p>
                <p className='mt-1 text-xs text-muted-foreground'>
                  Khởi tạo thanh toán và hoàn tất trên trang thanh toán. Hộp
                  thoại này sẽ tự nhận biết thanh toán thành công và xác nhận
                  đơn hàng tự động.
                </p>

                {isAwaitingPaymentCompletion ? (
                  <div className='mt-2 flex items-center gap-2 text-xs text-muted-foreground'>
                    <Loader2 className='h-4 w-4 animate-spin' />
                    Đang chờ xác nhận thanh toán...
                  </div>
                ) : null}
                {isProcessingPaymentWebhook ? (
                  <div className='mt-2 flex items-center gap-2 text-xs text-muted-foreground'>
                    <Loader2 className='h-4 w-4 animate-spin' />
                    Đã nhận thanh toán. Đang chờ xác nhận webhook...
                  </div>
                ) : null}

                {paymentInitResult ? (
                  <div className='mt-2 space-y-1 text-xs text-muted-foreground'>
                    <p>Giao dịch: {paymentInitResult.appTransId}</p>
                    <p>Trạng thái: {paymentInitResult.status || '--'}</p>
                    {paymentInitResult.message ? (
                      <p>
                        Thông báo từ cổng thanh toán:{' '}
                        {paymentInitResult.message}
                      </p>
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
                disabled={
                  busy ||
                  !shippingFee ||
                  isAwaitingPaymentCompletion ||
                  isProcessingPaymentWebhook
                }
              >
                {isInitiatingPayment ||
                isAwaitingPaymentCompletion ||
                isProcessingPaymentWebhook ? (
                  <Loader2 className='mr-2 h-4 w-4 animate-spin' />
                ) : null}
                {isAwaitingPaymentCompletion || isProcessingPaymentWebhook
                  ? 'Đang xử lý thanh toán...'
                  : 'Thanh toán phí vận chuyển'}
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
            Xác nhận đơn hàng
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
